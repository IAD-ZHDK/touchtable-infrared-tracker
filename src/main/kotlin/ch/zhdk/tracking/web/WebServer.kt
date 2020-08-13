package ch.zhdk.tracking.web

import ch.zhdk.tracking.config.WebSocketConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.Frame
import io.ktor.locations.Locations
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import kotlinx.coroutines.channels.SendChannel
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList

class WebServer {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .serializeSpecialFloatingPointValues()
        .create()

    val openChannels = CopyOnWriteArrayList<SendChannel<Frame>>()

    fun start(config : WebSocketConfig) {
        // setup server
        val server = embeddedServer(Netty, port = config.port.value) {
            install(DefaultHeaders)
            install(CallLogging)
            install(ConditionalHeaders)
            install(PartialContent) {
                maxRangeCount = 10
            }
            install(Compression) {
                default()
                excludeContentType(ContentType.Video.Any)
            }
            install(CORS)
            {
                anyHost()
            }
            install(ContentNegotiation) {
                register(ContentType.Application.Json, GsonConverter(gson))
            }
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(60) // Disabled (null) by default
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
                masking = false
            }

            routing {
                webRoutes(openChannels)
            }
        }
        server.start()
    }
}