#include <HardwareSerial.h>
#include "Arduino.h"
#include <log4arduino.h>

#define ENABLE_LOG4ARDUINO

void setup() {
#ifdef ENABLE_LOG4ARDUINO
    // setup serial
    Serial.begin(115200);
    LOG_INIT(&Serial);
    delay(2000);
#endif

    LOG("Tactile Device");
}

void loop() {
    delay(1000);
    LOG("test");
}