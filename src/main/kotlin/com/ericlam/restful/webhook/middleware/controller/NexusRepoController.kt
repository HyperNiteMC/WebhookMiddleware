package com.ericlam.restful.webhook.middleware.controller

import com.ericlam.restful.webhook.middleware.Storage
import com.ericlam.restful.webhook.middleware.manager.DiscordManager
import io.javalin.http.Context
import io.javalin.http.UnauthorizedResponse
import org.apache.commons.codec.binary.Hex
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object NexusRepoController {

    private val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

    fun handleComponentUpdate(ctx: Context){
        val signature = ctx.header("X-Nexus-Webhook-Signature")
        val signatureServer = generateSignature(ctx.body(), Storage.settings.secrets["nexus"] ?: "")
        if (signature != signatureServer){
            logger.warn("Signature mismatch, skipped.")
            throw UnauthorizedResponse("signature mismatch")
        }
        val event = ctx.bodyAsClass(ComponentEvent::class.java)
        logger.info("received: $event")
        val repo = event.repositoryName
        if (event.action == ACTION.CREATED) {
            val webhook = DiscordManager.WebHookParser.fromNexusComponent(repo, event.component)
            DiscordManager.sendWebHookMessage(webhook, Storage.settings.webhooks["nexus_discord"])
        }
        ctx.json(mapOf("result" to "ok"))
    }

    enum class ACTION {
        CREATED, UPDATED, DELETED
    }



    private fun generateSignature(body: String, secret: String): String {
        val key = SecretKeySpec(secret.toByteArray(), "HmacSHA1")
        val hmac = Mac.getInstance(key.algorithm)
        hmac.init(key)
        val hex = hmac.doFinal(body.toByteArray())
        return Hex.encodeHexString(hex)
    }

    data class ComponentEvent(
            val timestamp: LocalDateTime,
            val nodeId: String,
            val initiator: String,
            val repositoryName: String,
            val action: ACTION,
            val component: Component
    ){
        data class Component(
                val id: String,
                val componentId: String,
                val format: String,
                val name: String,
                val group: String,
                val version: String
        )
    }
}