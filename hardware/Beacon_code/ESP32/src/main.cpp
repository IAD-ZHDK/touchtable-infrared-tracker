#include <Arduino.h>
#include <Wire.h>
#include <MPU9250.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <NeoPixelBus.h>

#define pinIRLED1  0 // com IR LED
// #define pinIRLED2  4 // com IR LED
#define pinLED  16 // indicator LED


// functions
void ledBlink();
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
    // this resets all the neopixels to an off state
  strip.Begin();
  setNeoPixels(50, 0, 0);

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
  readIMU();
  ledBlink();
  BLEroutine();
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
    float heading = (atan2(IMU.getMagY_uT(),IMU.getMagX_uT()) * 180) / PI;
    Serial.println(heading);
  } else {
    DeviceStates = Stationary;
  }
  delay(1);
  lastIMUSum = sum;
  // Serial.print("AccelX: ");
  // Serial.print(IMU.getAccelX_mss(),3);
  // Serial.print("\t");
  // Serial.print("AccelY: ");
  // Serial.print(IMU.getAccelY_mss(),3);
  // Serial.print("\t");
  // Serial.print("AccelZ: ");
  // Serial.print(IMU.getAccelZ_mss(),3);
  // Serial.print("\t");
  // Serial.print("GyroX: ");
  // Serial.print(IMU.getGyroX_rads(),3);
  // Serial.print("\t");
  // Serial.print("GyroY: ");
  // Serial.print(IMU.getGyroY_rads(),3);
  // Serial.print("\t");
  // Serial.print("GyroZ: ");
  // Serial.println(IMU.getGyroZ_rads(),3);
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


long lastLedBlink = 0;
uint8_t LEDState = LOW;
void ledBlink() {
  int interval = 250;
  long currentMillis =  millis();
  if (currentMillis >= lastLedBlink + interval) {
    lastLedBlink = currentMillis;
    LEDState = !LEDState;
  }
    digitalWrite(pinLED, LEDState);
}
