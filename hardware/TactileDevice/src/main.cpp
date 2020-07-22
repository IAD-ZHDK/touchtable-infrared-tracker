#include <HardwareSerial.h>
#include "Arduino.h"

// clion ugly import
#include "../.pio/libdeps/esp32dev/log4arduino/src/log4arduino.h"

#define ENABLE_LOG4ARDUINO

void setup() {
#ifdef ENABLE_LOG4ARDUINO
    // setup serial
    Serial.begin(115200);
#endif
    LOG_INIT(&Serial);
    LOG("hello world!");
}

void loop() {
    delay(1000);
    LOG("test");
}