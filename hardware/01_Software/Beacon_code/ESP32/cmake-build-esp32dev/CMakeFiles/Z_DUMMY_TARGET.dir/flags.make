# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.17

# compile C with /Users/lfranzke/.platformio/packages/toolchain-xtensa32/bin/xtensa-esp32-elf-gcc
# compile CXX with /Users/lfranzke/.platformio/packages/toolchain-xtensa32/bin/xtensa-esp32-elf-g++
C_FLAGS = -std=gnu99 -Wno-old-style-declaration -Os -g3 -Wall -nostdlib -Wpointer-arith -Wno-error=unused-but-set-variable -Wno-error=unused-variable -mlongcalls -ffunction-sections -fdata-sections -fstrict-volatile-bitfields -Wno-error=deprecated-declarations -Wno-error=unused-function -Wno-unused-parameter -Wno-sign-compare -fstack-protector -fexceptions -Werror=reorder   -D'PLATFORMIO=40304' -D'ARDUINO_ESP32_DEV' -D'ESP32' -D'ESP_PLATFORM' -D'F_CPU=240000000L' -D'HAVE_CONFIG_H' -D'MBEDTLS_CONFIG_FILE="mbedtls/esp_config.h"' -D'ARDUINO=10805' -D'ARDUINO_ARCH_ESP32' -D'ARDUINO_VARIANT="esp32"' -D'ARDUINO_BOARD="Espressif ESP32 Dev Module"' -std=gnu99

C_DEFINES = 

C_INCLUDES = -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/include" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/src" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/NeoPixelBus/src" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/Bolder Flight Systems MPU9250/src" -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/Wire/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/SPI/src -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/ESP32 BLE Arduino/src" -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/config -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/app_trace -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/app_update -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/asio -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/bootloader_support -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/bt -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/coap -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/console -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/driver -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp-tls -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp32 -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_adc_cal -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_event -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_http_client -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_http_server -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_https_ota -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_ringbuf -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/ethernet -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/expat -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/fatfs -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/freemodbus -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/freertos -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/heap -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/idf_test -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/jsmn -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/json -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/libsodium -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/log -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/lwip -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/mbedtls -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/mdns -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/micro-ecc -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/mqtt -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/newlib -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/nghttp -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/nvs_flash -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/openssl -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/protobuf-c -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/protocomm -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/pthread -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/sdmmc -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/smartconfig_ack -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/soc -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/spi_flash -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/spiffs -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/tcp_transport -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/tcpip_adapter -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/ulp -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/vfs -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/wear_levelling -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/wifi_provisioning -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/wpa_supplicant -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/xtensa-debug-module -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp-face -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp32-camera -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/fb_gfx -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/cores/esp32 -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/variants/esp32 -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/Adafruit DRV2605 Library" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/Adafruit NeoPixel" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/DmxSimple" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/FastLED" -I"/Users/lfranzke/.platformio/lib/Adafruit ADS1X15" -I"/Users/lfranzke/.platformio/lib/Adafruit BusIO" -I"/Users/lfranzke/.platformio/lib/Adafruit DRV2605 Library" -I"/Users/lfranzke/.platformio/lib/Adafruit GFX Library" -I"/Users/lfranzke/.platformio/lib/Adafruit NeoPixel" -I"/Users/lfranzke/.platformio/lib/Adafruit SSD1306" -I"/Users/lfranzke/.platformio/lib/Bolder Flight Systems MPU9250/src" -I/Users/lfranzke/.platformio/lib/CapacitiveSensor_ID910 -I/Users/lfranzke/.platformio/lib/DmxSimple_ID898 -I"/Users/lfranzke/.platformio/lib/ESP32 BLE Arduino/src" -I/Users/lfranzke/.platformio/lib/FastLED -I/Users/lfranzke/.platformio/lib/NeoPixelBus/src -I/Users/lfranzke/.platformio/lib/QC3Control/src -I"/Users/lfranzke/.platformio/lib/SparkFun MPU-9250 9 DOF IMU Breakout/src" -I"/Users/lfranzke/.platformio/lib/SparkFun MPU-9250 Digital Motion Processing _DMP_ Arduino Library/src" -I/Users/lfranzke/.platformio/lib/TimerOne_ID131 -I/Users/lfranzke/.platformio/lib/U8g2/src -I/Users/lfranzke/.platformio/lib/ssd1306/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/ArduinoOTA/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/AsyncUDP/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/AzureIoT/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/BLE/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/BluetoothSerial/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/DNSServer/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/EEPROM/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/ESP32/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/ESPmDNS/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/FFat/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/FS/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/HTTPClient/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/HTTPUpdate/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/NetBIOS/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/Preferences/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/SD/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/SD_MMC/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/SPIFFS/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/SimpleBLE/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/Ticker/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/Update/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/WebServer/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/WiFi/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/WiFiClientSecure/src -I/Users/lfranzke/.platformio/packages/toolchain-xtensa32/xtensa-esp32-elf/include/c++/5.2.0 -I/Users/lfranzke/.platformio/packages/toolchain-xtensa32/xtensa-esp32-elf/include/c++/5.2.0/xtensa-esp32-elf -I/Users/lfranzke/.platformio/packages/toolchain-xtensa32/lib/gcc/xtensa-esp32-elf/5.2.0/include -I/Users/lfranzke/.platformio/packages/toolchain-xtensa32/lib/gcc/xtensa-esp32-elf/5.2.0/include-fixed -I/Users/lfranzke/.platformio/packages/toolchain-xtensa32/xtensa-esp32-elf/include -I/Users/lfranzke/.platformio/packages/tool-unity 

CXX_FLAGS = -fno-rtti -fno-exceptions -std=gnu++11 -Os -g3 -Wall -nostdlib -Wpointer-arith -Wno-error=unused-but-set-variable -Wno-error=unused-variable -mlongcalls -ffunction-sections -fdata-sections -fstrict-volatile-bitfields -Wno-error=deprecated-declarations -Wno-error=unused-function -Wno-unused-parameter -Wno-sign-compare -fstack-protector -fexceptions -Werror=reorder   -D'PLATFORMIO=40304' -D'ARDUINO_ESP32_DEV' -D'ESP32' -D'ESP_PLATFORM' -D'F_CPU=240000000L' -D'HAVE_CONFIG_H' -D'MBEDTLS_CONFIG_FILE="mbedtls/esp_config.h"' -D'ARDUINO=10805' -D'ARDUINO_ARCH_ESP32' -D'ARDUINO_VARIANT="esp32"' -D'ARDUINO_BOARD="Espressif ESP32 Dev Module"' -std=gnu++11

CXX_DEFINES = 

CXX_INCLUDES = -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/include" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/src" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/NeoPixelBus/src" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/Bolder Flight Systems MPU9250/src" -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/Wire/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/SPI/src -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/ESP32 BLE Arduino/src" -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/config -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/app_trace -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/app_update -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/asio -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/bootloader_support -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/bt -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/coap -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/console -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/driver -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp-tls -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp32 -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_adc_cal -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_event -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_http_client -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_http_server -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_https_ota -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp_ringbuf -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/ethernet -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/expat -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/fatfs -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/freemodbus -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/freertos -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/heap -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/idf_test -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/jsmn -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/json -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/libsodium -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/log -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/lwip -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/mbedtls -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/mdns -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/micro-ecc -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/mqtt -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/newlib -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/nghttp -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/nvs_flash -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/openssl -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/protobuf-c -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/protocomm -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/pthread -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/sdmmc -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/smartconfig_ack -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/soc -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/spi_flash -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/spiffs -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/tcp_transport -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/tcpip_adapter -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/ulp -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/vfs -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/wear_levelling -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/wifi_provisioning -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/wpa_supplicant -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/xtensa-debug-module -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp-face -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/esp32-camera -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/tools/sdk/include/fb_gfx -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/cores/esp32 -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/variants/esp32 -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/Adafruit DRV2605 Library" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/Adafruit NeoPixel" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/DmxSimple" -I"/Users/lfranzke/Documents/ZHdK/11_Physical Computing Lab/Technology/IR_Touch_table/IR_tracking/hardware/01_Software/Beacon_code/ESP32/.pio/libdeps/esp32dev/FastLED" -I"/Users/lfranzke/.platformio/lib/Adafruit ADS1X15" -I"/Users/lfranzke/.platformio/lib/Adafruit BusIO" -I"/Users/lfranzke/.platformio/lib/Adafruit DRV2605 Library" -I"/Users/lfranzke/.platformio/lib/Adafruit GFX Library" -I"/Users/lfranzke/.platformio/lib/Adafruit NeoPixel" -I"/Users/lfranzke/.platformio/lib/Adafruit SSD1306" -I"/Users/lfranzke/.platformio/lib/Bolder Flight Systems MPU9250/src" -I/Users/lfranzke/.platformio/lib/CapacitiveSensor_ID910 -I/Users/lfranzke/.platformio/lib/DmxSimple_ID898 -I"/Users/lfranzke/.platformio/lib/ESP32 BLE Arduino/src" -I/Users/lfranzke/.platformio/lib/FastLED -I/Users/lfranzke/.platformio/lib/NeoPixelBus/src -I/Users/lfranzke/.platformio/lib/QC3Control/src -I"/Users/lfranzke/.platformio/lib/SparkFun MPU-9250 9 DOF IMU Breakout/src" -I"/Users/lfranzke/.platformio/lib/SparkFun MPU-9250 Digital Motion Processing _DMP_ Arduino Library/src" -I/Users/lfranzke/.platformio/lib/TimerOne_ID131 -I/Users/lfranzke/.platformio/lib/U8g2/src -I/Users/lfranzke/.platformio/lib/ssd1306/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/ArduinoOTA/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/AsyncUDP/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/AzureIoT/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/BLE/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/BluetoothSerial/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/DNSServer/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/EEPROM/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/ESP32/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/ESPmDNS/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/FFat/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/FS/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/HTTPClient/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/HTTPUpdate/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/NetBIOS/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/Preferences/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/SD/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/SD_MMC/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/SPIFFS/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/SimpleBLE/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/Ticker/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/Update/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/WebServer/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/WiFi/src -I/Users/lfranzke/.platformio/packages/framework-arduinoespressif32/libraries/WiFiClientSecure/src -I/Users/lfranzke/.platformio/packages/toolchain-xtensa32/xtensa-esp32-elf/include/c++/5.2.0 -I/Users/lfranzke/.platformio/packages/toolchain-xtensa32/xtensa-esp32-elf/include/c++/5.2.0/xtensa-esp32-elf -I/Users/lfranzke/.platformio/packages/toolchain-xtensa32/lib/gcc/xtensa-esp32-elf/5.2.0/include -I/Users/lfranzke/.platformio/packages/toolchain-xtensa32/lib/gcc/xtensa-esp32-elf/5.2.0/include-fixed -I/Users/lfranzke/.platformio/packages/toolchain-xtensa32/xtensa-esp32-elf/include -I/Users/lfranzke/.platformio/packages/tool-unity 
