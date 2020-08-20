package ch.zhdk.tracking.pipeline.identification

import ch.bildspur.timer.ElapsedTimer
import ch.broox.ble.BLEDriver
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.list.update
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.model.ble.BLEMatch
import kotlin.concurrent.thread

class BLEIdentifier(config: PipelineConfig = PipelineConfig()) : ObjectIdentifier(config) {
    private val SERVICE_ID = "846123f6-ccf1-11ea-87d0-0242ac130003"
    private val NEOPIXEL_COLOR_ID = "fc3affa6-5020-47ce-93db-2e9dc45c9b55"
    private val IMU_ID = "98f09e34-73ab-4f2a-a5eb-a95e7e7ab733"

    private val SCAN_INTERVAL = 100
    private val SCAN_WINDOW = 99

    private val driver = BLEDriver()
    private lateinit var scanThread : Thread
    @Volatile private var running = false

    private val matchings = mutableSetOf<BLEMatch>()

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

        matchings.forEach { it.bleDevice.disconnect() }
        driver.close()
    }

    fun scanBLEDevices() {
        println("start ble scan...")
        val devices = driver.scan(SCAN_INTERVAL, SCAN_WINDOW, config.bleConfig.scanTime.value, SERVICE_ID)

        val potentialMatches = devices.map { BLEMatch(it) }
        matchings.update(potentialMatches,
            onAdd = { it.bleDevice.connect() },
            onUpdate =  { it.lastUpdateTimestamp = System.currentTimeMillis() }
        )

        println("Current Devices (${matchings.size}):")
        matchings.forEach {
            println(it.bleDevice.id)
        }
    }

    fun mapBLEDevicesToTactiles() {

    }
}