package ch.zhdk.tracking.web

import io.ktor.application.call
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.SendChannel
import java.util.concurrent.CopyOnWriteArrayList


fun Route.webRoutes(channels: CopyOnWriteArrayList<SendChannel<Frame>>) {
    get("/") {
        call.respondText("IR Tracker App - use websocket ws://tracking to connect!")
    }

    // api routes
    get("tracking/active") {
        call.respond("Clients Connected: ${channels.size}")
    }

    webSocket() {
        try {
            channels.add(outgoing)

            while (true) {
                val message = (incoming.receive() as Frame.Text).readText()
                println("[MSG]: $message")
            }
        } catch (e: ClosedReceiveChannelException) {
            // Do nothing!
        } catch (e: Throwable) {
            println("Error in websocket publisher:")
            e.printStackTrace()
        } finally {
            channels.remove(outgoing)
        }
    }
}