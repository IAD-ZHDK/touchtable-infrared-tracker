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
        } else {
            println("BLE service started")
        }

        bleLog("disconnecting all devices...")
        disconnectAllDevices()

        bleLog("starting thread...")
        running.set(true)
        scanThread = thread(isDaemon = true, start = true) {
            val lastScanTimer = ElapsedTimer(bleConfig.scanInterval.value * 1000L, true)
            val lastMapTimer = ElapsedTimer(bleConfig.mapInterval.value)

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

                Thread.sleep(100)
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
        bleLog("start ble scan...")
        val scannedDevices = driver.scan(SCAN_INTERVAL, SCAN_WINDOW, config.bleConfig.scanTime.value, BLE_SERVICE_ID)
        val listDevices = driver.list()

        bleLog("found ${listDevices.size} devices!")

        val devices = mutableSetOf<BLEDevice>()
        devices.addAll(scannedDevices)
        devices.addAll(listDevices)

        bleLog("${devices.size} total devices connected")

        val potentialMatches = devices.map { BLETactileDevice(it) }
        matchings.update(potentialMatches,
            onAdd = { it.connect() },
            onUpdate =  { it.lastUpdateTimestamp = System.currentTimeMillis() }
        )

        // display devices for debug
        if(config.bleConfig.verboseLogging.value) {
            scannedDevices.forEach { bleLog("New: ${it.id}") }
            listDevices.forEach { bleLog("Old: ${it.id}") }
        }

        bleLog("scan ended")
    }

    private fun mapBLEDevicesToTactiles() {
        bleLog("starting BLE to device matching...")

        // disable matches if td is not recognized anymore
        matchings.forEach {
            if(it.matched) {
                if(!it.tactileDevice!!.isActive)
                    it.unmatch()
            }
        }

        // remove not active devices
        bleLog("un-matching inactive devices...")
        val activeBleTacs = driver.list().map { BLETactileDevice(it) }
        val inactiveBleTacs = matchings - activeBleTacs
        matchings.removeAll(inactiveBleTacs)
        inactiveBleTacs.forEach { it.unmatch() }

        // map devices
        matchings.filter { !it.matched }.forEach {
            // todo: check if device is still available!

            // turn LED on
            it.isIRLedOn = true
            Thread.sleep(200)

            // wait until device is found or timeout (letch)
            isTactileDeviceRequested.set(true)
            bleLog("try match ${it.bleId}...")
            mutex.tryAcquire(1000, TimeUnit.MILLISECONDS)
            isTactileDeviceRequested.set(false)

            val device = foundDevice.get()

            if(device != null) {
                bleLog("device ${it.bleId} matched with ${device.uniqueId}!")
                it.match(device)
            } else {
                bleLog("could not match ${it.bleId}!")
            }

            // turn LED off
            it.isIRLedOn = false
            Thread.sleep(config.maxMissingTime.value + 100L)
        }

        bleLog("ended BLE to device matching!")
    }

    private fun disconnectAllDevices() {
        matchings.forEach {
            try {
                bleLog("disconnecting ${it.bleId}...")
                it.disconnect()
            } catch (ex : Exception) {
                println("error while disconnecting ${it.bleId}")
            }
        }
    }

    private fun bleLog(message : Any) {
        if(config.bleConfig.verboseLogging.value)
            println("BLE: $message")
    }
}