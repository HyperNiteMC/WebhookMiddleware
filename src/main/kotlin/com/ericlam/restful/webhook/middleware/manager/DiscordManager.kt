package com.ericlam.restful.webhook.middleware.manager

import com.ericlam.restful.webhook.middleware.Storage
import com.ericlam.restful.webhook.middleware.controller.NexusRepoController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture


object DiscordManager {

    private val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

    object WebHookParser {

        fun fromNexusComponent(repo: String, comp: NexusRepoController.ComponentEvent.Component): WebhookMessage {
            return WebhookMessage(
                    content = "有新的 API 版本已在 $repo 資源庫釋出。",
                    embeds = listOf(
                            EmbedMessage(
                                    fields = listOf(
                                            Field(
                                                    name = "groupId",
                                                    value = comp.group,
                                                    inline = true
                                            ),
                                            Field(
                                                    name = "artifactId",
                                                    value = comp.name,
                                                    inline = true
                                            ),
                                            Field(
                                                    name = "version",
                                                    value = comp.version,
                                                    inline = true
                                            ),
                                            Field(
                                                    name = "Maven 使用方式",
                                                    value = """
                                                    ```xml
                                                    <dependency>
                                                      <groupId>${comp.group}</groupId>
                                                      <artifactId>${comp.name}</artifactId>
                                                      <version>${comp.version}</version>
                                                    </dependency>
                                                    ```
                                                """.trimIndent(),
                                                    inline = false
                                            )
                                    ),
                                    author = Author(
                                            name = "> 進入 $repo 資源庫",
                                            url = "https://nexus.chu77.xyz/#browse/browse:$repo"
                                    ),
                                    footer = Footer(
                                            text = "webhook.chu77.xyz"
                                    ),
                                    timestamp = LocalDateTime.now()
                            )
                    )
            )
        }
    }

    fun sendWebHookMessage(webhookMessage: WebhookMessage, webhook: String?) {
        CompletableFuture.runAsync {
            val client = HttpClient.newHttpClient()
            val body = Storage.GSON.toJson(webhookMessage)
            logger.info("send to discord: $body")
            val request = HttpRequest.newBuilder()
                    .uri(URI.create(Storage.settings.webhooks[webhook] ?: "cannot find webhook for $webhook"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build()
            val con = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (con.statusCode() == 404) {
                logger.warn("404 Not found for webhook url ${Storage.settings.webhooks[webhook]}")
                return@runAsync
            }
            logger.info("Successfully sent and get response code ${con.statusCode()}")
            logger.info("response body: ${con.body()}")
            // logger.info("response headers:")
            // con.headers().map().forEach{ (key, value) -> logger.info("$key -> $value")}
        }.whenComplete { _, ex -> ex?.also { logger.error(it.message, it.stackTrace) } }
    }


    data class WebhookMessage(
            val content: String,
            val embeds: List<EmbedMessage>
    )

    data class EmbedMessage(
            val fields: List<Field>,
            val author: Author,
            val footer: Footer,
            val timestamp: LocalDateTime
    )

    data class Field(
            val name: String,
            val value: String,
            val inline: Boolean
    )

    data class Author(
            val name: String,
            val url: String
    )

    data class Footer(
            val text: String
    )
}