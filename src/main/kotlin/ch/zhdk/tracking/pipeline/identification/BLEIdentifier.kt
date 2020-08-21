package ch.zhdk.tracking.pipeline.identification

import ch.bildspur.timer.ElapsedTimer
import ch.broox.ble.BLEDriver
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.list.update
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.model.ble.BLETactileDevice
import ch.zhdk.tracking.model.ble.BLE_SERVICE_ID
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class BLEIdentifier(config: PipelineConfig = PipelineConfig()) : ObjectIdentifier(config) {
    private val SCAN_INTERVAL = 100
    private val SCAN_WINDOW = 99

    private val driver = BLEDriver()
    private lateinit var scanThread : Thread
    private var running = AtomicBoolean()

    private val matchings = mutableSetOf<BLETactileDevice>()

    private var isTactileDeviceRequested = AtomicBoolean()
    private var foundDevice = AtomicReference<TactileDevice?>()
    private var mutex = Semaphore(0)

    override fun pipelineStartup() {
        super.pipelineStartup()
        val bleConfig = config.bleConfig

        if(!driver.open(bleConfig.port.value, bleConfig.baudRate.value.rate)) {
            println("could not start BLE service")
            return
        }

        running.set(true)
        scanThread = thread(isDaemon = true, start = true) {
            val lastScanTimer = ElapsedTimer(bleConfig.scanInterval.value * 1000L)
            while (running.get()) {
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
        // todo: look for 3 led devices and tell it the waiting mapping thread
        if(isTactileDeviceRequested.get()) {
            devices.filter { it.markers.size == 3 }.forEach {
                // tell the other thread
                // set atomic reference for found device
                foundDevice.set(it)
                mutex.release()
                return
            }

            foundDevice.set(null)
            mutex.release()
        }
    }

    override fun pipelineStop() {
        super.pipelineStop()
        if(!driver.isOpen) return

        println("shutting down ble...")

        running.set(false)
        scanThread.join(1000 * 3)

        matchings.forEach { it.disconnect() }
        driver.close()
    }

    private fun scanBLEDevices() {
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

    private fun mapBLEDevicesToTactiles() {
        // todo: implement routine for matching
        matchings.filter { !it.matched }.forEach {
            isTactileDeviceRequested.set(true)
            // turn LED on
            println("turing led on")
            it.isIRLedOn = true

            // wait until device is found or timeout (letch)
            // get atomic reference
            // todo: warning! this has no timeouts!
            println("waiting for a match")
            mutex.tryAcquire(500, TimeUnit.MILLISECONDS)
            val device = foundDevice.get()

            if(device != null) {
                println("matched with: ${device.uniqueId}")
                it.tactileDevice = device
            } else {
                println("did not match!")
            }

            // turn LED off
            println("turning led off")
            it.isIRLedOn = false
            isTactileDeviceRequested.set(false)
        }
    }
}