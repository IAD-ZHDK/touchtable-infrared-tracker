#include <Arduino.h>
#include <Wire.h>
#include "MPU9250.h"
#include <Adafruit_NeoPixel.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// functions
void ledBlink();
void setNeoPixels(int r, int g, int b);
void readIMU();
// Which pin on the Arduino is connected to the NeoPixels?
#define PIN  14 // On Trinket or Gemma, suggest changing this to 1

// How many NeoPixels are attached to the Arduino?
#define NUMPIXELS 16 

Adafruit_NeoPixel pixels(NUMPIXELS, PIN, NEO_RGB + NEO_KHZ800);

// handy summary of BLE roles: https://embedded.fm/blog/ble-roles#:~:text=A%20client%20sends%20read%20and,using%20indicate%20and%20notify%20operations.
// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic1 = NULL;
BLECharacteristic* pCharacteristic2 = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;
uint32_t value = 0;

#define SERVICE_UUID        "846123f6-ccf1-11ea-87d0-0242ac130003"
#define CHARACTERISTIC_UUID1 "98f09e34-73ab-4f2a-a5eb-a95e7e7ab733"
#define CHARACTERISTIC_UUID2 "fc3affa6-5020-47ce-93db-2e9dc45c9b55"

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      setNeoPixels(0, 100, 0);
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      setNeoPixels(50, 0, 100);
    }
};

class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
      if (value.length() > 0) {
        Serial.println("*********");
        Serial.print("New value: ");
        for (int i = 0; i < value.length(); i++) {
          uint32_t hexValue = value[i];
          Serial.print(hexValue);
          Serial.print("_");
        }   
        Serial.println();
        Serial.println("*********");
      }
      // set neopixelColors
      if (value.length()>=3) {
            setNeoPixels(value[0], value[1], value[2]);
      }     
    }
};



#define pinIRLED1  0 // com IR LED
// #define pinIRLED2  4 // com IR LED
#define pinLED  16 // indicator LED

MPU9250 IMU(Wire,0x68);
// States
enum DeviceStates{Stationary, Moving};
byte DeviceStates = Stationary;


void setup() {
  // Wire.begin();
  // put your setup code here, to run once:
  pinMode(16, OUTPUT);
  Serial.begin(115200);
   while(!Serial) {}

  // start communication with IMU 
  int status = IMU.begin();
  Serial.print("Setting up IMU");
  if (status == 1) {
    Serial.println("IMU initialization successful");
  } else {
     Serial.println("IMU initialization unsuccessful");
    Serial.println("Check IMU wiring or try cycling power");
    Serial.print("IMU initialization fail - Status:");
    Serial.println(status);
    while(status != 1) {}
  }
// LED
  pinMode(pinIRLED1, OUTPUT);
  pinMode(pinLED, OUTPUT);

  digitalWrite(pinLED, LOW);
  digitalWrite(pinIRLED1,HIGH);
// NeoPixels
 pixels.begin(); // INITIALIZE NeoPixel strip object (REQUIRED)

// BLE
  // Create the BLE Device
  BLEDevice::init("TREE_TABLE_01");
  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  pCharacteristic1 = pService->createCharacteristic(
                      CHARACTERISTIC_UUID1,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );

  pCharacteristic2 = pService->createCharacteristic(
                      CHARACTERISTIC_UUID2,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  
                    );

  // Create a BLE Descriptor
  pCharacteristic1->addDescriptor(new BLE2902());
  pCharacteristic2->addDescriptor(new BLE2902());

  // set Callbacks
  pCharacteristic2->setCallbacks(new MyCallbacks());
  pCharacteristic2->setValue("Hello World");

  // Start the service
  pService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();
  Serial.println("Waiting a client connection to notify...");
  setNeoPixels(50, 0, 0);
}
void loop() {
  // readIMU();
  ledBlink();
    // notify changed value
    if (deviceConnected) {
        pCharacteristic1->setValue((uint8_t*)&value, 4);
        pCharacteristic1->notify();
        value++;
        delay(5); // bluetooth stack will go into congestion, if too many packets are sent, in 6 hours test i was able to go as low as 3ms
    }
    // disconnecting
    if (!deviceConnected && oldDeviceConnected) {
        delay(500); // give the bluetooth stack the chance to get things ready
        pServer->startAdvertising(); // restart advertising
        Serial.println("start advertising");
        oldDeviceConnected = deviceConnected;
    }
    // connecting
    if (deviceConnected && !oldDeviceConnected) {
        // do stuff here on connecting
        oldDeviceConnected = deviceConnected;
    }
}

void readIMU() {
// read the sensor
  IMU.readSensor();
  // display the data
  
  // Serial.print("AccelX: ");
  Serial.print(IMU.getAccelX_mss(),3);
  Serial.print("\t");
  // Serial.print("AccelY: ");
  Serial.print(IMU.getAccelY_mss(),3);
  Serial.print("\t");
  // Serial.print("AccelZ: ");
  Serial.print(IMU.getAccelZ_mss(),3);
  Serial.print("\t");
  // Serial.print("GyroX: ");
  Serial.print(IMU.getGyroX_rads(),3);
  Serial.print("\t");
  // Serial.print("GyroY: ");
  Serial.print(IMU.getGyroY_rads(),3);
  Serial.print("\t");
  // Serial.print("GyroZ: ");
  Serial.println(IMU.getGyroZ_rads(),3);
  // Serial.print("MagX: ");
  // Serial.print(IMU.getMagX_uT(),3);
  // Serial.print("\t");
  // Serial.print("MagY: ");
  // Serial.print(IMU.getMagY_uT(),3);
  // Serial.print("\t");
  // Serial.print("MagZ: ");
  // Serial.print(IMU.getMagZ_uT(),3);
  // Serial.print("\t");
  // Serial.print("temp: ");
  // Serial.println(IMU.getTemperature_C(),3);
  // delay(100);
  // https://create.arduino.cc/projecthub/30503/using-the-mpu9250-to-get-real-time-motion-data-08f011
}

void setNeoPixels(int r, int g, int b) {
   pixels.clear(); // Set all pixel colors to 'off'
  for(int i=0; i<NUMPIXELS; i++) { // For each pixel...
    pixels.setPixelColor(i, pixels.Color(r, g, b));
  }
  delay(1); // workaround https://github.com/adafruit/Adafruit_NeoPixel/issues/139
  pixels.show();  
}

int interval = 250;
long lastBlink = 0;
int LEDState = LOW;
void ledBlink() {
  long currentMillis =  millis();
  if (currentMillis >= lastBlink + interval) {
    lastBlink = currentMillis;
    LEDState = !LEDState;
  }
    digitalWrite(pinLED, LEDState);
}
