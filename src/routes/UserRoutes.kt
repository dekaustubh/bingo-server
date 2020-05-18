package com.dekaustubh.routes

import com.dekaustubh.constants.Request
import com.dekaustubh.constants.Response
import com.dekaustubh.interceptors.userInterceptor
import com.dekaustubh.models.Error
import com.dekaustubh.models.Success
import com.dekaustubh.models.User.LoginRequest
import com.dekaustubh.models.User.UserResult
import com.dekaustubh.repositories.UserRepository
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

/**
 * All users related routes.
 */
fun Routing.userRoutes(userRepository: UserRepository) {
    route("/api/v1") {
        route("/user") {

            intercept(ApplicationCallPipeline.Call) {
                if (call.request.path() != "/api/v1/user/register" &&
                    call.request.path() != "/api/v1/user/login") {
                    userInterceptor(userRepository)
                }
            }

            post("/register") {
                val loginRequest = call.receive<LoginRequest>()
                val user = userRepository.register(userName = loginRequest.name, userId = loginRequest.id, deviceId = loginRequest.deviceId)

                user?.let {
                    call.respond(
                        HttpStatusCode.Created,
                        UserResult(
                            success = Success(success = "User created"),
                            user = it
                        )
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        UserResult(
                            error = Error(error = "Could not create user"),
                            user = null
                        )
                    )
                }
            }

            put("/device") {
                val deviceId = call.request.header(Request.DEVICE_ID)
                if (deviceId.isNullOrEmpty()) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        UserResult(
                            error = Error(error = "User not present"),
                            user = null
                        )
                    )
                    return@put
                }
                val user = call.attributes[Response.USER_ATTR]
                val updatedUser = userRepository.updateUserDevice(user, deviceId)
                call.respond(
                    HttpStatusCode.OK,
                    UserResult(
                        success = Success(success = "User fetched"),
                        user = updatedUser
                    )
                )
            }

            get("/{id}") {
                val id = call.parameters["id"]
                val user = userRepository.getUserById(id ?: "")
                user?.let {
                    call.respond(
                        HttpStatusCode.OK,
                        UserResult(
                            success = Success(success = "User fetched"),
                            user = it
                        )
                    )
                } ?: run {
                    call.respond(
                        HttpStatusCode.NotFound,
                        UserResult(
                            error = Error(error = "User not present"),
                            user = null
                        )
                    )
                }
            }
        }
    }
}