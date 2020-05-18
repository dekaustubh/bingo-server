package com.dekaustubh.interceptors

import com.dekaustubh.constants.Request
import com.dekaustubh.constants.Response
import com.dekaustubh.constants.Response.USER
import com.dekaustubh.constants.Response.USER_ATTR
import com.dekaustubh.models.Error
import com.dekaustubh.models.User.User
import com.dekaustubh.repositories.UserRepository
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

suspend inline fun PipelineContext<*, ApplicationCall>.userInterceptor(userRepository: UserRepository) {
    val token = call.request.header(Request.TOKEN)
    if (token == null) {
        call.respond(
            HttpStatusCode.Forbidden,
            Error(error = "Auth token not present")
        )
        return finish()
    } else {
        val user = userRepository.getUserFromToken(token)
        if (user == null) {
            call.respond(
                HttpStatusCode.Forbidden,
                Error(error = "Not authorized")
            )
            return finish()
        } else {
            call.attributes.put(USER_ATTR, user)
        }
    }
}