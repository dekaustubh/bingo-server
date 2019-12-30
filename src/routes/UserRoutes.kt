package com.dekaustubh.routes

import com.dekaustubh.interceptors.userInterceptor
import com.dekaustubh.models.Error
import com.dekaustubh.models.Success
import com.dekaustubh.models.User.User
import com.dekaustubh.models.User.UserResult
import com.dekaustubh.repositories.UserRepository
import com.dekaustubh.repositories.UserRepositoryImpl
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.AttributeKey

/**
 * All users related routes.
 */
fun Routing.userRoutes(userRepository: UserRepository) {
    route("/api/v1") {
        route("/user") {

            intercept(ApplicationCallPipeline.Call) {
                if (call.request.path() != "/api/v1/user/register") {
                    userInterceptor(userRepository)
                }
            }

            post("/register") {
                val user = call.receive<User>()
                val addedUser = userRepository.addUser(user.name, user.email)
                addedUser?.let {
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

            get("/{id}") {
                val id = call.parameters["id"]
                val user = userRepository.getUserById(id?.toLong() ?: 0L)
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