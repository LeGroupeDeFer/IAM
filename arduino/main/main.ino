// Imports  
#include <SPI.h>
#include <WiFiNINA.h>
#include <WIFIUdp.h>
#include <NTPClient.h>
#include <Crypto.h>
#include <Ed25519.h>
#include <base64.hpp>
// In this file you can put your WIFI id and password
#include "arduino_secrets.h"

//Keys for signature
uint8_t privateKey[32] = { 0, 138, 111, 202, 144, 111, 169, 80, 51, 166, 8, 31, 91, 135, 157, 146, 40, 90, 77, 138, 38, 196, 225, 172, 31, 198, 75, 202, 177, 5, 33, 201 };
uint8_t publicKey[32] =  { 191, 72, 151, 219, 116, 19, 157, 91, 157, 107, 61, 193, 53, 238, 146, 68, 142, 19, 113, 236, 173, 5, 82, 47, 210, 156, 50, 174, 198, 36, 108, 66 };

// 64 bytes signature (for Ed25519) 
uint8_t signature[64];

//Bin size used to compute the filling rate
const float BIN_SIZE = 600; // in mm

// Constant for pins
const byte TRIGGER_PIN = 3; 
const byte ECHO_PIN = 2; 

const byte ECHO_PIN_2 = 4;
const byte TRIGGER_PIN_2 = 5;

const byte MOVE_PIN = 6;

// Constant for timeout
const unsigned long MEASURE_TIMEOUT = 25000UL; // 25ms = ~8m à 340m/s

// Sound speed in the air (mm/us)
const float SOUND_SPEED = 340.0 / 1000;


///////please enter your sensitive data in the Secret tab/arduino_secrets.h
char ssid[] = SECRET_SSID;        // your network SSID (name)
char pass[] = SECRET_PASS;    // your network password (use for WPA, or use as key for WEP)

int status = WL_IDLE_STATUS;


char server[] = "unanimity.be";    // name address
char codeResponse;
String postData;

// Initialize the WIFI client library
WiFiClient client;


// Inits for ntp packets
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP);
// Variables to save date and time
String formattedDate;


void setup() {

  //Initialize serial and wait for port to open:
  Serial.begin(9600);

  // Initialize pins
  pinMode(TRIGGER_PIN, OUTPUT);
  digitalWrite(TRIGGER_PIN, LOW); // Trigger pin set to low
  pinMode(ECHO_PIN, INPUT);

  pinMode(TRIGGER_PIN_2, OUTPUT);
  digitalWrite(TRIGGER_PIN_2, LOW); // Trigger pin set to low
  pinMode(ECHO_PIN_2, INPUT);
  

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

  // Start Ntp client
  timeClient.begin();
  timeClient.setTimeOffset(3600); // UTC+1

}





void loop() {

  // If move sensor detect a movement
  if(digitalRead(MOVE_PIN)==HIGH){
    Serial.println("Movement detected"); 
    
    // Start a measurement with a HIGH impulse of 10µs on TRIGGER pin
    digitalWrite(TRIGGER_PIN, HIGH);
    delayMicroseconds(10);
    digitalWrite(TRIGGER_PIN, LOW);

    // Compute time
    long measure = pulseIn(ECHO_PIN, HIGH, MEASURE_TIMEOUT);

    // Start a measurement with a HIGH impulse of 10µs on TRIGGER pin
    digitalWrite(TRIGGER_PIN_2, HIGH);
    delayMicroseconds(10);
    digitalWrite(TRIGGER_PIN_2, LOW);
    
    //Compute time
    long measure_2 = pulseIn(ECHO_PIN_2, HIGH, MEASURE_TIMEOUT);

    // Compute distance
    float distance_mm = measure / 2.0 * SOUND_SPEED;
    float distance_mm_2 = measure_2 / 2.0 * SOUND_SPEED;
    Serial.println(distance_mm);
    Serial.println(distance_mm_2);

    //Compute filling rate
    float filling_rate = (100/BIN_SIZE) * (BIN_SIZE - (min(distance_mm, distance_mm_2) + (max(distance_mm, distance_mm_2) - min(distance_mm, distance_mm_2))/2));

    // Get formatted date with the ntp client
    timeClient.update();
    formattedDate = timeClient.getFormattedDate();

    // Cleaning the date
    formattedDate[10] = ' ';
    formattedDate[19] = ' ';
    formattedDate.trim();
    
    // Construct payload and make a post request
    setPayload(filling_rate, formattedDate);
    sendPostRequest();
    // Wait 10 seconds
    delay(10000);
  }
}


void setPayload(float distance, String date) {
  // String containing the data that will be in the payload
  String data = "{\"time\":\"" + date + "\",\"fillingRate\":" + String(distance) + "}";

  // Signing the data with Ed25519 algorithm
  Ed25519::sign(signature, privateKey, publicKey, (uint8_t*)data.c_str(), data.length());

  // Base64 encoding the signature
  uint8_t buf[90]; 
  unsigned int base64_length = encode_base64(signature, 64, buf);

  // Final payload that will be send
  postData = "{\"signature\":\"" + String((char*)buf) + "\",\"data\":" + data + "}";
  Serial.println(postData);

}

void sendPostRequest() {
  
  client.stop();
  int cpt=0;

  Serial.println("\nStarting connection to server...");
  // if you get a connection, report back via serial:
  // Connect to the server on port 443 (https)
  if (client.connectSSL(server, 443)) {

    Serial.println("connected to server");
    // Make post request containing the payload
    client.print   // any spaces are important
    (
      String("POST ") + "/api/can/sadyhome/sync" + " HTTP/1.1\r\n" +
      "Host: " + "unanimity.be" + "\r\n" + // it works with an other Host ip... and without this line
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
