 

#include <SPI.h>
#include <WiFiNINA.h>
#include <WIFIUdp.h>
#include <NTPClient.h>
#include <Crypto.h>
#include <Ed25519.h>
#include <base64.hpp>

#include "arduino_secrets.h"

//Keys for signature
uint8_t privateKey[32] = { 48, 155, 26, 148, 131, 72, 236, 190, 62, 34, 183, 69, 26, 220, 9, 110, 239, 207, 31, 226, 18, 183, 2, 242, 251, 171, 106, 64, 85, 233, 60, 114 };
uint8_t publicKey[32] = { 242, 130, 36, 56, 13, 244, 176, 84, 233, 92, 182, 134, 168, 73, 136, 163, 20, 69, 225, 236, 140, 138, 23, 106, 51, 51, 128, 55, 184, 183, 63, 102 };

uint8_t signature[64];

// Constant for pins
const byte TRIGGER_PIN = 3; 
const byte ECHO_PIN = 2; 
const byte MOVE_PIN = 6;

// Constant for timeout
const unsigned long MEASURE_TIMEOUT = 25000UL; // 25ms = ~8m à 340m/s

// Sound speed in the air (mm/us)
const float SOUND_SPEED = 340.0 / 1000;


///////please enter your sensitive data in the Secret tab/arduino_secrets.h
char ssid[] = SECRET_SSID;        // your network SSID (name)
char pass[] = SECRET_PASS;    // your network password (use for WPA, or use as key for WEP)

int status = WL_IDLE_STATUS;

char server[] = "192.168.0.42";    // name address
char codeResponse;
String postData;

// Initialize the Ethernet client library

WiFiClient client;


// Inits for ntp packets
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP);
// Variables to save date and time
String formattedDate;
String dayStamp;
String timeStamp;

void setup() {

  //Initialize serial and wait for port to open:
  Serial.begin(9600);

  // Initialize pins
  pinMode(TRIGGER_PIN, OUTPUT);
  digitalWrite(TRIGGER_PIN, LOW); // Trigger pin set to low
  pinMode(ECHO_PIN, INPUT);

  pinMode(MOVE_PIN, INPUT);

  while (!Serial) {

    ; // wait for serial port to connect. Needed for native USB port only

  }

  // check for the WiFi module:
  if (WiFi.status() == WL_NO_MODULE) {
    Serial.println("Communication with WiFi module failed!");
    // don't continue
    while (true);
  }

  String fv = WiFi.firmwareVersion();

  if (fv < WIFI_FIRMWARE_LATEST_VERSION) {

    Serial.println("Please upgrade the firmware");

  }

  // attempt to connect to Wifi network:
  while (status != WL_CONNECTED) {

    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);

    // Connect to WPA/WPA2 network. Change this line if using open or WEP network:
    status = WiFi.begin(ssid, pass);
    
    // wait 10 seconds for connection:
    delay(10000);
  }

  Serial.println("Connected to wifi");
  printWifiStatus();

  timeClient.begin();
  timeClient.setTimeOffset(3600); // UTC+1

}





void loop() {

  if(digitalRead(MOVE_PIN)==HIGH){
    Serial.println("mouvement detecte"); 
    
    // Start a measurement with a HIGH impulse of 10µs on TRIGGER pin
    digitalWrite(TRIGGER_PIN, HIGH);
    delayMicroseconds(10);
    digitalWrite(TRIGGER_PIN, LOW);
    
    // Compute time
    long measure = pulseIn(ECHO_PIN, HIGH, MEASURE_TIMEOUT);
     
    // Compute distance
    float distance_mm = measure / 2.0 * SOUND_SPEED;
  
    timeClient.update();
    formattedDate = timeClient.getFormattedDate();

    formattedDate[10] = ' ';
    formattedDate[19] = ' ';
    formattedDate.trim();
    
    // Construct payload and make a post request
    setPayload(distance_mm, formattedDate);
    sendPostRequest();
    
    delay(10000);
  }
}


void setPayload(float distance, String date) {
  String data = "{\"time\":\"" + date + "\",\"fillingRate\":\"" + String(distance) + "\"}";
  
  Ed25519::sign(signature, privateKey, publicKey, (uint8_t*)data.c_str(), data.length());

  uint8_t buf[90];
 
  unsigned int base64_length = encode_base64(signature, 64, buf);
  
  postData = "{\"signature\":\"" + String((char*)buf) + "\",\"data\":" + data + "}";
  Serial.println(postData);

}

void sendPostRequest() {
  
  client.stop();
  int cpt=0;

  Serial.println("\nStarting connection to server...");
  // if you get a connection, report back via serial:
  if (client.connect(server, 8000)) {

    Serial.println("connected to server");

    client.print   // any spaces are important
    (
      String("POST ") + "/api/can/test/sync" + " HTTP/1.1\r\n" +
      "Host: " + "192.168.0.42" + "\r\n" + // it works with an other Host ip... and without this line
      "Content-Type: application/json\r\n" +
      "Content-Length: " + postData.length() + "\r\n" + //this line is needed with the exact size value
      "\r\n" + // CR+LF pour signifier la fin du header
      postData
    );
    
  while(client.available()==0) //wait until data reception
    {
      cpt++;
      delay(100);
      if (cpt>49)
      {
        Serial.print("timeout... no answer");
        break;
      }
    }
    while(client.available()) //write all incoming data characters on serial monitor
    {
      codeResponse = client.read();
      Serial.write(codeResponse);      
    }
  }
  else 
  {
    // if you couldn't make a connection:
    Serial.println("Connection failed");
  }
  
}

void printWifiStatus() {
  
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print your board's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI):");
  Serial.print(rssi);
  Serial.println(" dBm");
}
