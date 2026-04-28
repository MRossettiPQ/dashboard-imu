package com.rot.core.context

import com.rot.user.models.User
import org.eclipse.microprofile.jwt.JsonWebToken
import java.util.*

object ApplicationContext {
    private val holder = ThreadLocal<UserContext>()

    var context: UserContext?
        get() = holder.get()
        set(context) {
            if (context == null) holder.remove()
            else holder.set(context)
        }

    val user: User?
        get() = context?.user

    fun clear(){
        holder.remove()
    }
}


data class UserContext(
    val id: UUID,
    val jwt: JsonWebToken
) {
    val user: User by lazy {
        User.findOrThrowById(id)
    }
}