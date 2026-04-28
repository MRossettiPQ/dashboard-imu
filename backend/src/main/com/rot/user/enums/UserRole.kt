package com.rot.user.enums

enum class UserRole(val description: String) {
    ADMINISTRATOR("Administrator"),
    USER("User"),
    PHYSIOTHERAPIST("Physiotherapist");

    companion object {
    }
}

object UserRoleString {
    const val ADMINISTRATOR = "ADMINISTRATOR"
    const val USER = "USER"
    const val PHYSIOTHERAPIST = "PHYSIOTHERAPIST"
}