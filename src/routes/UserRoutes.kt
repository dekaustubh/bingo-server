package com.dekaustubh.routes

import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Routing.userRoutes() {
    route("/api/v1") {
        route("/user") {
            post("/register") {

            }

            get("/{id}") {

            }
        }
    }
}