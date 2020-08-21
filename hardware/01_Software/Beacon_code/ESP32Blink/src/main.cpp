#include <Arduino.h>
#include "driver/adc.h"
#include "blink.h"

#define pinLED  13 // indicator LED
#define DEEP_SLEEP_TIME 15
void goToDeepSleep();

void setup() {
  Serial.begin(115200);
  pinMode(pinLED, OUTPUT);
  // setCpuFrequencyMhz(80);
}

void loop() {
    ledBlink(pinLED);
    if (millis()>5000) {
      goToDeepSleep();
    }
}

void goToDeepSleep()
{
  Serial.println("sleeping...");
  // WiFi.disconnect(true);
  // WiFi.mode(WIFI_OFF);
  // btStop();

  adc_power_off();
  // esp_wifi_stop();
  // esp_bt_controller_disable();
  // Configure the timer to wake us up!
  esp_sleep_enable_timer_wakeup(DEEP_SLEEP_TIME * 1000000L);

  // Go to sleep! Zzzz
  esp_deep_sleep_start();
}


