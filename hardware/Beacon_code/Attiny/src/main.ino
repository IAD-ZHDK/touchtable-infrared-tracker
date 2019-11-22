#include <Arduino.h>

#define pinIRLED1  0 // 
#define pinIRLED2  1 // com IR LED
#define pinLED  4 // indicator LED

int bitCount = 5; 
uint8_t bitPattern = 0b00001010; // only the last 5 bits 
int inverval = 70;
int idleTime = 0;

void setup() {
  pinMode(pinIRLED1, OUTPUT);
  pinMode(pinIRLED2, OUTPUT);
  pinMode(pinLED, OUTPUT);

  digitalWrite(pinLED, LOW);
  digitalWrite(pinIRLED1,HIGH);
  digitalWrite(pinIRLED2,HIGH);
}

void loop() {
//todo: implement manchester encoding 
//***** start bit
digitalWrite(pinLED, HIGH);
digitalWrite(pinIRLED2,HIGH);
digitalWrite(pinIRLED1,HIGH);
delay(inverval);
digitalWrite(pinIRLED1,LOW);
digitalWrite(pinLED, LOW);
//***** signal
for(int i=bitCount- 1;i>=0;i--) {
    boolean bit = (bitPattern >> i) & 0x1;
    if (bit == 1) {
      digitalWrite(pinIRLED2,HIGH);
    } else {
      digitalWrite(pinIRLED2,LOW);  
    }
    delay(inverval);
} 
  //***** parity bit
  if (getParity(bitPattern) == 0) {
      //even
      digitalWrite(pinIRLED2,HIGH);
      } else {
      //odd
      digitalWrite(pinIRLED2,LOW);
  }
  delay(inverval);
  //***** stop bit
  //digitalWrite(pinIRLED2, LOW);
  //delay(inverval);
  
  //***** idle 
  //digitalWrite(pinIRLED2,HIGH);
  //delay(idleTime);
}

bool getParity(uint8_t n) 
{ 
  n = n ^ (n >> 1);
  n = n ^ (n >> 2);
  n = n ^ (n >> 4);
  return (n & 1);
} 