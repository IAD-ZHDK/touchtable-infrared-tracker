#include <Arduino.h>
#include <Wire.h>
#include "ICM_20948.h"
#include <BLEDevice.h>
#include "SensorFusion.h"
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <NeoPixelBus.h>
#include "blink.h"
#include <EEPROM.h>

//
// Power saving considerations
//
// NEOPixel idle ≈ 7 mA 
// NEOPixel full power ≈ 250 mA 
// IMU idle ≈ 3 mA
// Pisition In-LED ≈ 40 mA
// Signal In-LED ≈ 20 mA
// ESP idle @ full cpu clockrate ≈ 58 mA
// ESP idle 80 MHz cpu clockrate ≈ 30 mA
// ESP sleep ≈ 3 mA
// some tips on ESP power saving https://www.savjee.be/2019/12/esp32-tips-to-increase-battery-life/#:~:text=Well%2C%20a%20regular%20ESP32%20will,coming%20in%20at%2027mA%2D34mA.


#define pinIRLED1  12 // com IR LED
#define pinIRLED2  27 // com IR LED
#define pinIRLED3  33 // com IR LED
#define IMUinterup 32 // com IR LED
#define pinLED  13 // power indicator LED

// IMU
#define IMU
#define WIRE_PORT Wire
#define I2C_ADDR ICM_20948_I2C_ADDR_AD1
ICM_20948_I2C myICM;  // 
#define AD0_VAL   1  
float lastIMUSum = 0;
bool deviceMoved = false;
SF fusion;
float gx, gy, gz, ax, ay, az, mx, my, mz, temp;
float pitch, roll, yaw;
float deltat;
// functions
void ledBlink(uint8_t pin);
void setNeoPixels(int r, int g, int b);
void NeoPixelSweep(int r, int g, int b);
void readIMU();
void BLEroutine();
void NeoPixelsOff();

// neopixel

const uint16_t PixelCount = 16; 
const uint8_t PixelPin = 15;  
NeoPixelBus<NeoGrbFeature, Neo800KbpsMethod> strip(PixelCount, PixelPin);

// handy summary of BLE roles: https://embedded.fm/blog/ble-roles#:~:text=A%20client%20sends%20read%20and,using%20indicate%20and%20notify%20operations.
// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

BLEServer* pServer = NULL;
BLECharacteristic* pCharacteristic1 = NULL;
BLECharacteristic* pCharacteristic2 = NULL;
BLECharacteristic* pCharacteristic3 = NULL;
BLECharacteristic* pCharacteristic4 = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;
uint8_t deviceID = 1;

// https://www.uuidgenerator.net/version4
// device 1: 846123f6-ccf1-11ea-87d0-0242ac130003
// device 2:f492305c-ec32-4a71-94c4-303b97a99bb2 
#define SERVICE_UUID        "846123f6-ccf1-11ea-87d0-0242ac130003"
#define CHARACTERISTIC_UUID1 "98f09e34-73ab-4f2a-a5eb-a95e7e7ab733" // IMU
#define CHARACTERISTIC_UUID2 "fc3affa6-5020-47ce-93db-2e9dc45c9b55" // NEOPIXEL
#define CHARACTERISTIC_UUID3 "fc9a2e54-a7f2-4bad-aebc-9879e896f1b9" // IR LED
#define CHARACTERISTIC_UUID4 "58b8dbc8-045e-4a20-a52d-f181f01e12fe" // Device ID

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
        auto data = pCharacteristic->getData();
        //uint8_t a = data[3] & 0xFF;
        uint8_t r = data[2] & 0xFF;
        uint8_t g = data[1] & 0xFF;
        uint8_t b = data[0] & 0xFF;
        setNeoPixels(r, g, b);
    }
};

class MyCallbackIRLED: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
       auto data = pCharacteristic->getData();
       int value = (data[0] & 0xFF);
         //Serial.println("value:"+String(value));
       if (value > 0) {
         digitalWrite(pinIRLED2, HIGH); 
         //Serial.println("IR 2 ON");
       } else {
        digitalWrite(pinIRLED2, LOW); 
          //Serial.println("IR 2 OFF");
       }
    }
};

class MyCallbackID: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
       auto data = pCharacteristic->getData();
       int value = (data[0] & 0xFF);
       //Serial.println("ID value:"+String(value));
       EEPROM.put(0, value);
    }
};


// States
enum State{Stationary, Moving};
State DeviceStates = Stationary;
State DeviceLastState = DeviceStates;

void setup() {
  // setCpuFrequencyMhz(80);
 // Serial.begin(115200);
  //(while(!Serial) {}
// IMU 
  WIRE_PORT.begin();
  WIRE_PORT.setClock(400000);
  bool initialized = false;
#ifdef IMU 
  while( !initialized ){
    myICM.begin( WIRE_PORT, AD0_VAL );
   // Serial.print( F("Initialization of the IMU returned: ") );
   // Serial.println( myICM.statusString() );
    if( myICM.status != ICM_20948_Stat_Ok ){
   // Serial.println( "Trying again..." );
      delay(1500);
    } else{
      initialized = true;
    }
  }
#endif
// LED
  pinMode(pinIRLED1, OUTPUT);
  pinMode(pinIRLED2, OUTPUT);
  digitalWrite(pinIRLED1, HIGH);
  digitalWrite(pinIRLED2, LOW);
  pinMode(pinLED, OUTPUT);
// NeoPixels
  strip.Begin();
  setNeoPixels(0, 0, 0);
  // Create the BLE Device
  BLEDevice::init("Tactile_Object");
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

  pCharacteristic4 = pService->createCharacteristic(
                      CHARACTERISTIC_UUID4,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  
                    );

  // Create a BLE Descriptor
  pCharacteristic1->addDescriptor(new BLE2902());
  pCharacteristic2->addDescriptor(new BLE2902());
  pCharacteristic3->addDescriptor(new BLE2902());
  pCharacteristic4->addDescriptor(new BLE2902());
  // set Callbacks
  pCharacteristic2->setCallbacks(new MyCallbackNeoPixel());
  pCharacteristic2->setValue("NEOPIXEL COLOR");

  pCharacteristic3->setCallbacks(new MyCallbackIRLED());
  pCharacteristic3->setValue("IR LED");

  pCharacteristic4->setCallbacks(new MyCallbackID());
  pCharacteristic4->setValue("ID");


  // Start the service
  pService->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();
  //Serial.println("Waiting a client connection to notify...");
  NeoPixelSweep(0,0,254);
  // get device ID from EEPROM 
  EEPROM.get(0, deviceID);
  pCharacteristic4->setValue((uint8_t*)&deviceID, 1);
}
void loop() {
  ledBlink(pinLED);
  BLEroutine();
  #ifdef IMU 
    readIMU();
  #endif
  delay(20);
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
        //Serial.println("start advertising");
        oldDeviceConnected = deviceConnected;
        delay(500); 
        NeoPixelSweep(50, 0, 0);
    }
    // connecting
    if (deviceConnected && !oldDeviceConnected) {
        // do stuff here on connecting
        oldDeviceConnected = deviceConnected;; 
        NeoPixelSweep(1, 100, 1);
    }
}


void readIMU() {
// read the sensor
  myICM.getAGMT();
  ax = (myICM.accX()/100) *  9.81 ; // milli g's to m/s2
  ay = (myICM.accY()/100) *  9.81; // milli g's to m/s2
  az = (myICM.accZ()/100) *  9.81; // milli g's to m/s2
  gx = radians(myICM.gyrX()); // degrees per second to rads per secon
  gy = radians(myICM.gyrY()); // degrees per second to rads per secon
  gz = radians(myICM.gyrZ()); // degrees per second to rads per secon
  mx = myICM.magX(); // micro teslas
  my = myICM.magY(); // micro teslas 
  mz = myICM.magZ();// micro teslas  
  deltat = fusion.deltatUpdate();
  fusion.MadgwickUpdate(gx, gy, gz, ax, ay, az, mx, my, mz, deltat);

  roll = fusion.getRollRadians();
  pitch = fusion.getPitchRadians();
  yaw = fusion.getYawRadians();

  float sum = (roll+pitch+yaw)/3;
  // if smm of all values above threshold, movement has happend.
  if (sum<lastIMUSum-0.001 || sum>lastIMUSum+0.001) {
      DeviceStates = Moving;
  } else {
      DeviceStates = Stationary;
  }
  lastIMUSum = sum;

  // Serial.println("Accel: " + String(ax) + ", " + String(ay) + ", " + String(az) + " g");
  // Serial.println("Gyro: " + String(gx) + ", " + String(gy) + ", " + String(gz) + " dps");
  // Serial.println("Mag: " + String(mx) + ", " + String(my) + ", " + String(mz) + " uT");
  if (DeviceStates == Moving) {
   // Serial.println("roll:" + String(fusion.getRoll()) + " pitch: "+ String(fusion.getPitch()) + " yaw:"+String(fusion.getYaw()));
  }
}

void setNeoPixels(int r, int g, int b) {
  RgbColor color(r,g,b);
  for(int i=0; i<PixelCount; i++) {
    strip.SetPixelColor(i, color);
  }
  strip.Show();
}

void NeoPixelSweep(int r, int g, int b) {
    RgbColor color = RgbColor(0, 0, 0);
    uint16_t cycles = 100;
    uint8_t brightness = 0;

    for(int i=0; i<cycles; i++) {
      float angleStep =  float(i)*0.1;
      brightness = ((sin((float(i)/float(cycles))*PI))) * 255; // fade from 0% to 100% then back down to 0%
      for(int t=0; t<PixelCount; t++) {
      float angle = float(t)/float(PixelCount) * TWO_PI;
      angle += angleStep;
      float red = r/2+(sin(angle)*r/2);
      float green = g/2+(sin(angle)*g/2);
      float blue = b/2+(sin(angle)*b/2);
      color = RgbColor(red, green, blue);
      strip.SetPixelColor(t, color.Dim(brightness));
      }
    strip.Show();
    delay(10);
  }
  NeoPixelsOff();
}

void NeoPixelsOff() {
  RgbColor color(0,0,0);
  for(int i=0; i<PixelCount; i++) {
    strip.SetPixelColor(i, color);
  }
  strip.Show();
}