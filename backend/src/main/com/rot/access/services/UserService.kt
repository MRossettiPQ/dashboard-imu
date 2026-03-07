package com.rot.access.services

import com.rot.core.exceptions.ApplicationException
import com.rot.user.models.User
import io.quarkus.cache.CacheResult
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class UserService {

    @CacheResult(cacheName = "user-context-cache")
    fun getCachedUser(userId: UUID): User {
        // Esta chamada ao banco de dados será cacheada pelo Caffeine
        return User.findOrThrowById(userId)
    }

    @CacheResult(cacheName = "user-context-cache")
    fun getCachedActiveUser(userId: UUID): User {
        // Esta chamada ao banco de dados será cacheada pelo Caffeine
        return User.createQuery()
            .where(User.q.id.eq(userId))
            .where(User.q.active.isTrue)
            .fetchFirst() ?: throw ApplicationException("User not found or inactive", 401)
    }
}