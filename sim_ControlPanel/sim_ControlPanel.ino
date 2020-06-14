#include <ESP8266WiFi.h>
#include <PubSubClient.h>

#define PIN_CLK D3
#define PIN_SERIAL_IN D6
#define PIN_STROBE D5
#define N_PINS_SHIFTER 8
#define N_STATES 10
#define INTER_POLL_PERIODS 100 // 10 times/s
#define STATE_DUR 10000 // 3s 
#define INTER_DUR 500 // 0.5s intermitent leds

// gloabl data
const char* ssid = "EMBOU";
const char* password = "AADD75C607C6";

const char* mqtt_server = "node02.myqtthub.com";

const char *mqttUser = "delonghi_esam_4000";
const char *mqttPass = "delonghi_esam_4000";
const char *clientId = "delonghi_esam_4000";


unsigned long epoch, now;
unsigned char n_period_inter = 0, n_coff, aroma, water, temp;;
const char* states_names[] = {"OFF", "TURNING_ON", "READY", "MAKING_ONE", "MAKING_TWO", "STEAM", "TURNING_OFF", "NO_WATER", "CONTAINER_FULL", "FAILURE", "DECALCIF"};

enum STATES {
  OFF = 0,
  TURNING_ON = 1,
  READY = 2,
  MAKING_ONE = 3,
  MAKING_TWO = 4,
  STEAM = 5,
  TURNING_OFF = 6,
  NO_WATER = 7,
  CONTAINER_FULL = 8,
  FAILURE = 9,
  DECALCIF = 10,
} sim_state;

STATES targetState;
 
// ISR variables
volatile unsigned long n_cycles = 0;
volatile bool isRising = true;
volatile unsigned long serial_in_codes[] = {0b00000010, // OFF
                                            0b11000001, // TURNING_ON -- INTER_SYNC READY, OFF
                                            0b11000001, // READY
                                            0b10000001, // MAKING_ONE
                                            0b01000001, // MAKING_TWO
                                            0b00100001, // STEAM
                                            0b11000001, // TURNING_OFF -- INTER_ASYNC ONE, TWO
                                            0b10000010, // NO_WATER
                                            0b01000010, // CONTAINER_FULL
                                            0b00100010, // FAILURE
                                            0b00010010 // DECALCIF
                                           };
volatile unsigned long serial_in_temp;

WiFiClient espClient;
PubSubClient client(espClient);


void setup_wifi() {
  delay(100);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}


void processRequest(char* buf){
//  char* buf = "N_coffes:2,Aroma:20,Water:180,Temp:1";
  Serial.printf("[Request] %s\n", buf);
  if(String(buf).equals(states_names[TURNING_ON])){
    Serial.printf("[Request] %s\n", buf);
    targetState = TURNING_ON;
  } else if(String(buf).equals(states_names[TURNING_OFF])){
    Serial.printf("[Request] %s\n", buf);
    targetState = TURNING_OFF;
  }else { // Prepare coffee
    int n = sscanf(buf, "N_coffes:%d,Aroma:%d,Water:%d,Temp:%d", &n_coff, &aroma, &water, &temp);
    if(n == 4){
      Serial.printf("[Request]\tNum_coffes: %d, Aroma: %d, Water: %d, Temp: %d\n", n_coff, aroma, water, temp);  
      targetState = (STATES) (n_coff+2);
      //TODO: change pwm values
    } else {
      Serial.println("Error: bad format request");
    }  
  }
}


void callbackMQTT(char* topic, byte* payload, unsigned int length) {
  payload[length] = '\0';
  Serial.printf("Message arrived from [%s]: %s, len: %d\n", topic, payload, length);
  processRequest((char*) payload);
}


void connectMQTT() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection... ");
    // Attempt to connect
    if (client.connect(clientId, mqttUser, mqttPass)) {
      Serial.println("connected");
      // Subscribe to user actions
      client.subscribe("ACT/caf1/+");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 3 seconds");
      // Wait 3 seconds before retrying
      delay(3000);
    }
  }
}


void ICACHE_RAM_ATTR timerHandler() { // PROTOCOL // TODO --> add poll tweak btw Q1 & Q2
  if (isRising) { // clk RISING edge
    if (n_cycles < N_PINS_SHIFTER) {
      if (n_cycles == 0) serial_in_temp = serial_in_codes[sim_state]; // Backup codes
      digitalWrite(PIN_SERIAL_IN, (serial_in_temp >> n_cycles) & 0b1); // Load SERIAL_IN data
      digitalWrite(PIN_STROBE, HIGH);
    } else {
      digitalWrite(PIN_STROBE, LOW);
    }
    digitalWrite(PIN_CLK, HIGH);
    n_cycles = ++n_cycles;
  }
  else { // clk FALLING edge
    if (n_cycles < N_PINS_SHIFTER - 1) { // SHIFT DATA
      digitalWrite(PIN_STROBE, HIGH);
    } else { // Freeze data
      digitalWrite(PIN_STROBE, LOW);
    }
    digitalWrite(PIN_CLK, LOW);
    if (n_cycles >= INTER_POLL_PERIODS) { // Poll again
      n_cycles = 0;
    }
  }
  isRising = !isRising;
}


/**
 * Simulate the coffee machine. All temporized state have a lifetime of STATE_DUR 
 * and blynk leds every INTER_DUR. Every INTER_POLL_PERIODS buttons are scanned and 
 * leds updated
 */
void simulateCoffeeMachine(){
  
  switch (sim_state) {
    case OFF:
      if(targetState == TURNING_ON){
        sim_state = targetState;
        targetState = READY;
        Serial.println(states_names[sim_state]);
      }
      break;
    case TURNING_ON:
      now = millis();
      if (now - epoch >= STATE_DUR) {
        epoch = millis();
        sim_state = READY;
        Serial.println(states_names[sim_state]);
        n_period_inter = 0;
      }
      else if ((now - epoch) > (INTER_DUR * n_period_inter)) { // Intermitent leds sync
        if (n_period_inter % 2 == 0) serial_in_codes[TURNING_ON] = serial_in_codes[READY];
        else serial_in_codes[TURNING_ON] = serial_in_codes[OFF];
        n_period_inter++;
      }
      break;
    case READY:
      if(targetState != READY && targetState != OFF  && targetState != TURNING_ON){
        sim_state = targetState;
        targetState = READY;
        Serial.println(states_names[sim_state]);
      }
      break;
    case MAKING_ONE:
      if (millis() - epoch >= STATE_DUR) {
        epoch = millis();
        sim_state = READY;
        targetState = READY;
        Serial.println(states_names[sim_state]);
      }
      break;
    case MAKING_TWO:
      if (millis() - epoch >= STATE_DUR) {
        epoch = millis();        
        sim_state = READY;
        Serial.println(states_names[sim_state]);
      }
      break;
    case STEAM:
      if (millis() - epoch >= STATE_DUR) {
        epoch = millis();
        sim_state = TURNING_OFF;
        Serial.println(states_names[sim_state]);
      }
      break;
    case TURNING_OFF:
      now = millis();
      if (now - epoch >= STATE_DUR) {
        epoch = now;
        sim_state = NO_WATER;
        Serial.println(states_names[sim_state]);
        n_period_inter = 0;
      } else if ((now - epoch) > INTER_DUR * n_period_inter) { // Intermitent leds sync
        if (n_period_inter % 2 == 0) serial_in_codes[TURNING_OFF] = serial_in_codes[MAKING_ONE];
        else serial_in_codes[TURNING_OFF] = serial_in_codes[MAKING_TWO];
        n_period_inter++;
      }
      break;
    case (NO_WATER || CONTAINER_FULL):
      if (millis() - epoch >= STATE_DUR) {
        epoch = millis();
        sim_state = CONTAINER_FULL;
        Serial.println(states_names[sim_state]);
      }
      break;
    case CONTAINER_FULL:
      if (millis() - epoch >= STATE_DUR) {
        epoch = millis();
        sim_state = DECALCIF;
        Serial.println(states_names[sim_state]);
      }
      break;
    case DECALCIF:
      if (millis() - epoch >= STATE_DUR) {
        epoch = millis();
        sim_state = FAILURE;
        Serial.println(states_names[sim_state]);
      }
      break;
    case FAILURE:
      if (millis() - epoch >= STATE_DUR) {
        epoch = millis();
        sim_state = OFF;
        Serial.println(states_names[sim_state]);
      }
      break;
  }
}


void configHwProtocol(){
  pinMode(PIN_CLK, OUTPUT);
  pinMode(PIN_SERIAL_IN, OUTPUT);
  pinMode(PIN_STROBE, OUTPUT);
  sim_state = READY;
  targetState = READY;  
  timer1_isr_init();
  timer1_attachInterrupt(timerHandler);
  timer1_enable(TIM_DIV256, TIM_EDGE, TIM_LOOP); // interrupt on RISING EDGE, Tick_freq --> 80/256 = 312.5KHz, 1 tick = 3.2us
  //  timer1_write(312);    // 313*3.2 = 1001.6us ~ 1ms
  timer1_write(31);    // 31*3.2 = 99.2us
  //  timer1_write(312000);    // 312000*3.2 = 1001.6ms ~ 1s
}


void setup() {
  Serial.begin(115200);
  pinMode(BUILTIN_LED, OUTPUT);     // Initialize the BUILTIN_LED pin as an output
  digitalWrite(2, HIGH); // shutdown esp builtin led

  // Wifi connection
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callbackMQTT);
  connectMQTT();

  // Coffee machine protocol
  configHwProtocol();
  epoch = millis();
}


void loop() {
  STATES oldState = sim_state;
  Serial.println((String) "State prev: " + states_names[sim_state]);
  simulateCoffeeMachine();
  Serial.println((String) "State post: " + states_names[sim_state]);

  if (!client.loop()) {
    Serial.print("MQTT connection lost: rc=");
    Serial.print(client.state());
    Serial.println(" Reconnecting...");
    connectMQTT();
  }
  if(sim_state != oldState){
    client.publish("STATUS/caf1", states_names[sim_state]);
    Serial.println((String) "Notify new state: " + states_names[sim_state]);
  }
  delay(1500);
}
