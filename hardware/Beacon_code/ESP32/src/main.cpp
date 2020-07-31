#include <Arduino.h>
#include <Wire.h>
#include <MPU9250.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <NeoPixelBus.h>
#include "blink.h"

#define pinIRLED1  4 // com IR LED
// #define pinIRLED2  4 // com IR LED
#define pinLED  16 // indicator LED
#define IMUInterupt  18 // indicator LED

// functions
void setNeoPixels(int r, int g, int b);
void readIMU();
void BLEroutine();

// neopixel
const uint16_t PixelCount = 16; 
const uint8_t PixelPin = 14;  
NeoPixelBus<NeoGrbFeature, Neo800KbpsMethod> strip(PixelCount, PixelPin);

// handy summary of BLE roles: https://embedded.fm/blog/ble-roles#:~:text=A%20client%20sends%20read%20and,using%20indicate%20and%20notify%20operations.
// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic1 = NULL;
BLECharacteristic* pCharacteristic2 = NULL;
BLECharacteristic* pCharacteristic3 = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;
uint32_t value = 0;

#define SERVICE_UUID        "846123f6-ccf1-11ea-87d0-0242ac130003"
#define CHARACTERISTIC_UUID1 "98f09e34-73ab-4f2a-a5eb-a95e7e7ab733" // IMU
#define CHARACTERISTIC_UUID2 "fc3affa6-5020-47ce-93db-2e9dc45c9b55" // NEOPIXEL
#define CHARACTERISTIC_UUID3 "fc9a2e54-a7f2-4bad-aebc-9879e896f1b9" // IR LED

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
};

class MyCallbackNeoPixel: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
      // if (value.length() > 0) {
      //for (int i = 0; i < value.length(); i++) {
      //  uint32_t hexValue = value[i];
      //   Serial.print(hexValue);
      // }   
      //}
      // set neopixelColors
      if (value.length()>=3) {
            setNeoPixels(value[0], value[1], value[2]);
      }     
    }
};

class MyCallbackIRLED: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string value = pCharacteristic->getValue();
      if (value.length() > 0) {
          digitalWrite(pinIRLED1,value[0]);
      }
    }
};
// IMU
float lastIMUSum = 0; 
bool deviceMoved = false;
MPU9250 IMU(Wire,0x68);
// States
enum State{Stationary, Moving};
State DeviceStates = Stationary;
State DeviceLastState = DeviceStates;

void setup() {
  pinMode(16, OUTPUT);
  Serial.begin(115200);
   while(!Serial) {}
// IMU 
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
  // setting DLPF bandwidth to 20 Hz
  //IMU.setDlpfBandwidth(MPU9250::DLPF_BANDWIDTH_20HZ);
  // setting SRD to 19 for a 50 Hz update rate
  //IMU.setSrd(19);
  //IMU.enableDataReadyInterrupt();
  //attachInterrupt(IMUInterupt, readIMU, RISING);
// LED
  pinMode(pinIRLED1, OUTPUT);
  pinMode(pinLED, OUTPUT);

  digitalWrite(pinLED, LOW);
  digitalWrite(pinIRLED1,HIGH);
// NeoPixels
    // this resets all the neopixels to an off state
  strip.Begin();
  setNeoPixels(50, 50, 0);

  // Create the BLE Device
  BLEDevice::init("TREE_TABLE_01");
  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  // charecterstic for movement from IMU
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

  pCharacteristic3 = pService->createCharacteristic(
                      CHARACTERISTIC_UUID3,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  
                    );

  // Create a BLE Descriptor
  pCharacteristic1->addDescriptor(new BLE2902());
  pCharacteristic2->addDescriptor(new BLE2902());
  pCharacteristic3->addDescriptor(new BLE2902());

  // set Callbacks
  pCharacteristic2->setCallbacks(new MyCallbackNeoPixel());
  pCharacteristic2->setValue("NEOPIXEL COLOR");

  pCharacteristic3->setCallbacks(new MyCallbackIRLED());
  pCharacteristic3->setValue("IR LED");

  // Start the service
  pService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();
  Serial.println("Waiting a client connection to notify...");

}
void loop() {
  ledBlink(pinLED);
  BLEroutine();
  readIMU();
}
uint32_t value2 = 0;
void BLEroutine() {
    // notify changed value
    if (deviceConnected && DeviceLastState != DeviceStates) {
        pCharacteristic1->setValue((uint8_t*)&DeviceStates, 1);
        pCharacteristic1->notify();
        value2++;
        DeviceLastState = DeviceStates;
        delay(15); // bluetooth stack will go into congestion, if too many packets are sent,
    }
    // disconnecting
    if (!deviceConnected && oldDeviceConnected) {
        delay(500); // give the bluetooth stack the chance to get things ready
        pServer->startAdvertising(); // restart advertising
        Serial.println("start advertising");
        oldDeviceConnected = deviceConnected;
        setNeoPixels(50, 0, 0);
    }
    // connecting
    if (deviceConnected && !oldDeviceConnected) {
        // do stuff here on connecting
        oldDeviceConnected = deviceConnected;
        setNeoPixels(0, 100, 0);
    }
}


void readIMU() {
// read the sensor
  IMU.readSensor();
  // display the data
  float sum = (IMU.getAccelX_mss()+IMU.getAccelY_mss()+IMU.getAccelZ_mss()+IMU.getGyroX_rads()+IMU.getGyroY_rads()+IMU.getGyroZ_rads())/6;
  if (sum<lastIMUSum-0.05 || sum>lastIMUSum+0.05) {
    DeviceStates = Moving;
    // float heading = (atan2(IMU.getMagY_uT(),IMU.getMagX_uT()) * 180) / PI;
    // Serial.println(heading);
  } else {
    DeviceStates = Stationary;
  }
  delay(1);
  lastIMUSum = sum;
 
if (DeviceStates == Moving) {
  float accelX = IMU.getAccelX_mss();
  float accelY = IMU.getAccelY_mss();
  float accelZ = IMU.getAccelZ_mss();
  float gyroX = IMU.getGyroX_rads();
  float gyroY = IMU.getGyroY_rads();
  float gyroZ = IMU.getGyroZ_rads();
  float magX = IMU.getMagX_uT();
  float magY = IMU.getMagY_uT();
  float magZ = IMU.getMagZ_uT();

  Serial.println("Accel: " + String(accelX) + ", " + String(accelY) + ", " + String(accelZ) + " g");
  Serial.println("Gyro: " + String(gyroX) + ", " + String(gyroY) + ", " + String(gyroZ) + " dps");
  Serial.println("Mag: " + String(magX) + ", " + String(magY) + ", " + String(magZ) + " uT");

//Euler angle from accel

 
   float pitch = atan2 (accelY ,( sqrt ((accelX * accelX) + (accelZ * accelZ))));
   float roll = atan2(-accelX ,( sqrt((accelY * accelY) + (accelZ * accelZ))));

   // yaw from mag

   float Yh = (magY * cos(roll)) - (magZ * sin(roll));
   float Xh = (magX * cos(pitch))+(magY * sin(roll)*sin(pitch)) + (magZ * cos(roll) * sin(pitch));

   float yaw =  atan2(Yh, Xh);


  roll = roll*57.3;
  pitch = pitch*57.3;
  yaw = yaw*57.3;
   
  Serial.println("pitch"  + String( pitch));
   Serial.println("roll" + String( roll));
   Serial.println("yaw" + String( yaw ));
   }
}

void setNeoPixels(int r, int g, int b) {
   // pixels.clear(); // Set all pixel colors to 'off'
   /*
  for(int i=0; i<NUM_LEDS; i++) { // For each pixel...
    leds[i] = CRGB(r,g,b);
  }
  FastLED.show(); 
  */
  RgbColor color(r,g,b);
    for(int i=0; i<PixelCount; i++) { 
        strip.SetPixelColor(i, color);
    }
    strip.Show();
}
