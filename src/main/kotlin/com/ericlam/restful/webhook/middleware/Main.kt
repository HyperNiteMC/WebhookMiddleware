package com.ericlam.restful.webhook.middleware

import com.ericlam.restful.webhook.middleware.Storage.GSON
import com.ericlam.restful.webhook.middleware.Storage.settings
import com.ericlam.restful.webhook.middleware.controller.NexusRepoController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.plugin.json.FromJsonMapper
import io.javalin.plugin.json.JavalinJson
import io.javalin.plugin.json.ToJsonMapper
import java.time.LocalDateTime


fun main() {
    val timenow = LocalDateTime.now()

    JavalinJson.fromJsonMapper = object : FromJsonMapper {
        override fun <T> map(json: String, targetClass: Class<T>): T = GSON.fromJson(json, targetClass)
    }

    JavalinJson.toJsonMapper = object : ToJsonMapper {
        override fun map(obj: Any): String = GSON.toJson(obj)
    }

    val app = Javalin.create { c ->
        // c.enableDevLogging()
        c.defaultContentType = "application/json"
        c.enableCorsForOrigin(*settings.origins.toTypedArray())

    }.start(8080)

    app.routes {

        path("") {

            get("") { ctx ->
                ctx.json(mapOf("status" to "running", "startAt" to timenow.toString()))
            }

        }

        path("nexus") {

            post(NexusRepoController::handleComponentUpdate)

        }


    }

}

