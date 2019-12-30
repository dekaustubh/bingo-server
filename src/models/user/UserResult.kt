package com.dekaustubh.models.User

import com.dekaustubh.models.Error
import com.dekaustubh.models.Success
import com.fasterxml.jackson.annotation.JsonClassDescription
import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Result class for fetching or updating single user.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("user")
data class UserResult(
    val error: Error? = null,
    val success: Success? = null,
    val user: User?
)

/**
 * Result class for fetching all the users.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonClassDescription("users")
data class UsersResult(
    val error: Error? = null,
    val success: Success? = null,
    val user: List<User>
)