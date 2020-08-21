package ch.zhdk.tracking.pipeline.identification

import ch.bildspur.timer.ElapsedTimer
import ch.broox.ble.BLEDevice
import ch.broox.ble.BLEDriver
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.list.update
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.model.ble.BLETactileDevice
import ch.zhdk.tracking.model.ble.BLE_SERVICE_ID
import java.lang.Exception
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

        disconnectAllDevices()

        running.set(true)
        scanThread = thread(isDaemon = true, start = true) {
            val lastScanTimer = ElapsedTimer(bleConfig.scanInterval.value * 1000L)
            val lastMapTimer = ElapsedTimer(bleConfig.mapInterval.value * 1000L)

            while (running.get()) {
                if(lastScanTimer.elapsed()) {
                    try {
                        scanBLEDevices()
                    }   catch (ex : Exception) {
                        println("BLE Scan Error: ${ex.message}")
                    }
                }

                if(lastMapTimer.elapsed()){
                    try {
                        mapBLEDevicesToTactiles()
                    }   catch (ex : Exception) {
                        println("BLE Map Error: ${ex.message}")
                    }
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
                println("multi marker recognized try to set")
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

        disconnectAllDevices()

        running.set(false)
        scanThread.join(1000 * 3)

        driver.close()
    }

    private fun scanBLEDevices() {
        println("start ble scan...")
        val scannedDevices = driver.scan(SCAN_INTERVAL, SCAN_WINDOW, config.bleConfig.scanTime.value, BLE_SERVICE_ID)
        val listDevices = driver.list()

        val devices = mutableSetOf<BLEDevice>()
        devices.addAll(scannedDevices)
        devices.addAll(listDevices)

        val potentialMatches = devices.map { BLETactileDevice(it) }
        matchings.update(potentialMatches,
            onAdd = { it.connect() },
            onUpdate =  { it.lastUpdateTimestamp = System.currentTimeMillis() }
        )

        // disable matches if td is not recognized anymore
        matchings.forEach {
            if(it.matched) {
                if(!it.tactileDevice!!.isActive)
                    it.disableMatch()
            }
            println(it.bleId)
        }
    }

    private fun mapBLEDevicesToTactiles() {
        matchings.filter { !it.matched }.forEach {
            // todo: check if device is still available!

            // turn LED on
            it.isIRLedOn = true
            Thread.sleep(200)

            // wait until device is found or timeout (letch)
            isTactileDeviceRequested.set(true)
            println("try match...")
            mutex.tryAcquire(1000, TimeUnit.MILLISECONDS)
            isTactileDeviceRequested.set(false)

            val device = foundDevice.get()

            if(device != null) {
                it.enableMatch(device)
            }

            // turn LED off
            it.isIRLedOn = false
            Thread.sleep(config.maxMissingTime.value + 50L)
        }
    }

    private fun disconnectAllDevices() {
        matchings.forEach {
            try {
                it.disconnect()
            } catch (ex : Exception) {
                println("error while disconnecting ${it.bleId}")
            }
        }
    }
}