#include <Arduino.h>

long lastLedBlink = 0;
uint8_t LEDState = LOW;

void ledBlink(uint8_t pin) {
  int interval = 150;
  long currentMillis =  millis();
  if (currentMillis >= lastLedBlink + interval) {
    lastLedBlink = currentMillis;
    LEDState = !LEDState;
  }
    digitalWrite(pin, LEDState);
}
