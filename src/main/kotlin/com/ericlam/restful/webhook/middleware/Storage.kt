package com.ericlam.restful.webhook.middleware

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

object Storage {

    val GSON: Gson by lazy {
        GsonBuilder()
                .registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime>() {

                    private val dateFormat: DateFormat = SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'")

                    override fun write(p0: JsonWriter?, p1: LocalDateTime?) {
                        val date = Date.from(p1?.toInstant(ZoneOffset.ofHours(0)))
                        p0?.value(dateFormat.format(date))
                    }

                    override fun read(p0: JsonReader?): LocalDateTime {
                        val str = p0?.nextString()
                        return LocalDateTime.parse(str?.substring(0..18))
                    }

                })
                .disableHtmlEscaping()
                .create()
    }

    val settings: Settings by lazy {
        val stream = this::class.java.getResourceAsStream("/settings.json")
        val dst = File("settings.json")
        if (!dst.exists()) Files.copy(stream, dst.toPath(), StandardCopyOption.REPLACE_EXISTING)
        GSON.fromJson(FileReader(dst), Settings::class.java)
    }

    data class Settings(
            val origins: List<String>,
            val nexus_secret: String,
            val discord: Discord
    )

    data class Discord(
            val webhookUrl: String
    )
}