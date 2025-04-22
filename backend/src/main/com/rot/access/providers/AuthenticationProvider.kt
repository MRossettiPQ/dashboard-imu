package com.rot.access.providers

import com.rot.core.context.ApplicationContext
import com.rot.core.context.UserContext
import com.rot.core.exceptions.ApplicationException
import com.rot.core.utils.TransactionUtils.runInNewTransaction
import com.rot.user.models.User
import io.quarkus.security.Authenticated
import io.quarkus.security.PermissionsAllowed
import io.quarkus.security.identity.SecurityIdentity
import io.smallrye.jwt.auth.cdi.NullJsonWebToken
import jakarta.annotation.Priority
import jakarta.annotation.security.DeclareRoles
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.container.ResourceInfo
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.jwt.JsonWebToken
import java.util.*


@Provider
@RequestScoped
@Priority(Priorities.AUTHENTICATION)
class AuthenticationProvider(
    private var jwt: JsonWebToken,
    private var identity: SecurityIdentity,
    @Context private val resourceInfo: ResourceInfo,
) : ContainerRequestFilter {
    override fun filter(requestContext: ContainerRequestContext) {
        val annotationAccepted = listOf(
            DeclareRoles::class.java,
            PermitAll::class.java,
            RolesAllowed::class.java,
            Authenticated::class.java,
            PermissionsAllowed::class.java
        )

        if (!annotationAccepted.any { resourceInfo.resourceMethod.isAnnotationPresent(it) } && !annotationAccepted.any { resourceInfo.resourceClass.isAnnotationPresent(it) }) {
            return
        }

        if (jwt is NullJsonWebToken || jwt.rawToken == null) {
            throw ApplicationException("JWT is null or not properly injected.", 401)
        }

        val uuid = jwt.getClaim<String>("reference")
            ?: throw ApplicationException("JWT claim 'reference' is missing.", 401)

        runInNewTransaction().call {
            User.createQuery()
                .where(User.q.id.eq(UUID.fromString(uuid)))
                .where(User.q.active.isTrue)
                .fetchFirst() ?: throw ApplicationException("User not found or inactive", 401)
        }

        ApplicationContext.context = UserContext(UUID.fromString(uuid), jwt)
    }
}