package com.dekaustubh.models

import com.fasterxml.jackson.annotation.JsonClassDescription

@JsonClassDescription("user")
data class UserResult(
    val error: Error? = null,
    val success: Success? = null,
    val user: User?
)

@JsonClassDescription("users")
data class UsersResult(
    val error: Error? = null,
    val success: Success? = null,
    val user: List<User>
)