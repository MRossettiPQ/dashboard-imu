package com.rot.access.providers

import com.rot.core.context.ApplicationContext
import jakarta.annotation.Priority
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import jakarta.ws.rs.ext.Provider


@Provider
@Priority(999999)
class AuthenticationCleanupProvider : ContainerResponseFilter {
    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        ApplicationContext.clear()
    }
}