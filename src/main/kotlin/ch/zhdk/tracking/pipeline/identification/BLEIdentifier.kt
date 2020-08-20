package ch.zhdk.tracking.pipeline.identification

import ch.bildspur.timer.ElapsedTimer
import ch.broox.ble.BLEDriver
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.list.update
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.model.ble.BLETactileDevice
import ch.zhdk.tracking.model.ble.BLE_SERVICE_ID
import kotlin.concurrent.thread

class BLEIdentifier(config: PipelineConfig = PipelineConfig()) : ObjectIdentifier(config) {
    private val SCAN_INTERVAL = 100
    private val SCAN_WINDOW = 99

    private val driver = BLEDriver()
    private lateinit var scanThread : Thread
    @Volatile private var running = false

    private val matchings = mutableSetOf<BLETactileDevice>()

    override fun pipelineStartup() {
        super.pipelineStartup()
        val bleConfig = config.bleConfig

        if(!driver.open(bleConfig.port.value, bleConfig.baudRate.value.rate)) {
            println("could not start BLE service")
            return
        }

        running = true
        scanThread = thread(isDaemon = true, start = true) {
            val lastScanTimer = ElapsedTimer(bleConfig.scanInterval.value * 1000L)
            while (running) {
                if(lastScanTimer.elapsed()) {
                    scanBLEDevices()
                    mapBLEDevicesToTactiles()
                }
                Thread.sleep(500)
            }
        }
    }

    override fun recognizeObjectId(devices: List<TactileDevice>) {
        // this method is called every loop
    }

    override fun pipelineStop() {
        super.pipelineStop()
        if(!driver.isOpen) return

        println("shutting down ble...")

        running = false
        scanThread.join(1000 * 3)

        matchings.forEach { it.disconnect() }
        driver.close()
    }

    fun scanBLEDevices() {
        println("start ble scan...")
        val devices = driver.scan(SCAN_INTERVAL, SCAN_WINDOW, config.bleConfig.scanTime.value, BLE_SERVICE_ID)

        val potentialMatches = devices.map { BLETactileDevice(it) }
        matchings.update(potentialMatches,
            onAdd = { it.connect() },
            onUpdate =  { it.lastUpdateTimestamp = System.currentTimeMillis() }
        )

        println("Current Devices (${matchings.size}):")
        matchings.forEach {
            println(it.id)
        }
    }

    fun mapBLEDevicesToTactiles() {
        // todo: implement routine for matching
        matchings.filter { !it.matched }.forEach {
            // turn LED on
            it.isIRLedOn = true

            // wait until device is found or timeout
            Thread.sleep(500)

            // turn LED of
            it.isIRLedOn = false
        }
    }
}