package com.rot.core.context

import java.util.*

object ApplicationContext {
    private val holder = ThreadLocal<UserContext>()

    var context: UserContext?
        get() = holder.get()
        set(context) {
            if (context == null) holder.remove()
            else holder.set(context)
        }

    fun clear(){
        holder.remove()
    }
}


data class UserContext(
    val id: UUID,
) {



}