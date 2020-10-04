import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.logging.SimpleFormatter

val GSON: Gson by lazy {
    GsonBuilder()
            .disableHtmlEscaping()
            .create()
}

fun main() {
    val date = Date.from(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)))
    println(SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'").format(date))


}

fun String.unicodeToString(): String {
    val string = StringBuffer()

    val hex = this.split("\\\\u");

    for (h in hex) {
        val data = Integer.parseInt(h, 16);


        string.append(data.toChar());
    }

    return string.toString();
}
