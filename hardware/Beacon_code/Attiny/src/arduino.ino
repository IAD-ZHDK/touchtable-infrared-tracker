#include <Arduino.h>

#define pinIRLED1  0 // 55 ohm
#define pinIRLED2  1 // 68 ohm
#define pinIRLED3  2 // 100 ohm
#define pinLED  4
#define high 1
#define low  0
#define start  2
uint8_t bitPattern = 0b01101110;
int inverval = 80;

void setup() {
  pinMode(pinIRLED1, OUTPUT);
  pinMode(pinIRLED2, OUTPUT);
  pinMode(pinIRLED3, OUTPUT);
  pinMode(pinLED, OUTPUT);
  digitalWrite(pinIRLED1,HIGH);
}

// the loop function runs over and over again forever
void loop() {
  //todo: implement manchester encoding 
  //start bit
  ledSet(start); 
  digitalWrite(pinLED, HIGH);
  delay(inverval);  
  digitalWrite(pinLED, LOW);
for(int i=8;i>0;i--) {
  boolean bit = (bitPattern >> i) & 0x1;
  if (bit == 1) {
  ledSet(high);   
  } else {
  ledSet(low);   
  }
  delay(inverval);  
  } 
  //endBit

}

void ledSet(int level) {
      switch (level) {
        case 0:
          digitalWrite(pinIRLED1,HIGH);
          digitalWrite(pinIRLED2,LOW);
          digitalWrite(pinIRLED3,LOW);
          break;
        case 1:
          digitalWrite(pinIRLED1,HIGH);
          digitalWrite(pinIRLED2,HIGH);
          digitalWrite(pinIRLED3,LOW);
        break;
       case 2:
          digitalWrite(pinIRLED1,HIGH);
          digitalWrite(pinIRLED2,HIGH);
          digitalWrite(pinIRLED3,HIGH);
        break;
        default:
          digitalWrite(pinIRLED1,HIGH);
          digitalWrite(pinIRLED2,LOW);
          digitalWrite(pinIRLED3,LOW);
          break;
      }
}