package ch.zhdk.tracking.osc

import ch.zhdk.tracking.config.OscConfig
import ch.zhdk.tracking.web.WebServer
import com.illposed.osc.OSCPacket
import com.illposed.osc.OSCSerializerAndParserBuilder
import com.illposed.osc.transport.udp.OSCPortIn
import io.ktor.http.cio.websocket.Frame
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class OscWebSocketChannel(private val webServer: WebServer, config : OscConfig) : OscChannel(config) {
    private val oscOutputBuffer = ByteBuffer.allocate(OSCPortIn.BUFFER_SIZE)
    private val oscSerializer = OSCSerializerAndParserBuilder().buildSerializer(oscOutputBuffer)

    override fun sendMessage(msg: OSCPacket) {
        GlobalScope.launch {
            // serialize message
            oscOutputBuffer.rewind()
            oscSerializer.write(msg)
            oscOutputBuffer.flip()

            // send message
            webServer.openChannels.forEach {
                it.send(Frame.Binary(true, oscOutputBuffer))
            }
        }
    }
}