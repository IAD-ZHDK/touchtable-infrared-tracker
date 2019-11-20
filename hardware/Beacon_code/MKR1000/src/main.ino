#include <Arduino.h>
#define pinIRLED1  12
#define pinIRLED2  11 
#define pinLED  6

uint8_t bitPattern = 0b01101010;
int inverval = 80;

void setup() {
  pinMode(pinIRLED1, OUTPUT);
  pinMode(pinIRLED2, OUTPUT);
  pinMode(pinLED, OUTPUT);
  pinMode(7, OUTPUT);
  digitalWrite(7,LOW);
  digitalWrite(pinIRLED1,HIGH);
}

// the loop function runs over and over again forever
void loop() {
  //todo: implement manchester encoding 
  //start bit
  digitalWrite(pinLED,HIGH);
for(int i=8;i>0;i--) {
  boolean bit = (bitPattern >> i) & 0x1;
  if (bit == 1) {
    digitalWrite(pinIRLED2,HIGH);
  } else {
    digitalWrite(pinIRLED2,LOW);  
  }
  delay(inverval);
  digitalWrite(pinLED,LOW);  
  } 
  //endBit
}
