#include <ESP8266WiFi.h>
#include <PubSubClient.h>

#define PIN_CLK D3
#define PIN_SERIAL_IN D6
#define PIN_STROBE D5
#define PIN_ON_VAP D0
#define PIN_T1_DECAL D1
#define PIN_T2_MAC D2
#define N_PINS_SHIFTER 8
#define N_STATES 11
#define INTER_POLL_PERIODS 1000 // 10 times/s
#define POLL_PERIODS 20
#define STATE_DUR 10000 // 30s 
#define INTER_DUR 300 // 0.5s intermitent leds

// gloabl data
const char* ssid = "*";
const char* password = "*";

const char* mqtt_server = "node02.myqtthub.com";

const char *mqttUser = "*";
const char *mqttPass = "*";
const char *clientId = "*";


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
volatile unsigned int on_off, taz2, taz1, vap, decal; 

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
  bool found = false;
  for(int i = 0; i < N_STATES; ++i) {
    if (strcmp(buf, states_names[i]) == 0) {
        found = true;
        targetState = (STATES) i;
        break;
    }
  }
  if(!found){ 
    if(strcmp(buf, "GET_STATUS") == 0){ // User request status
      client.publish("STATUS/caf1", states_names[sim_state]);
      Serial.println((String) "State: " + states_names[sim_state]);
    } else{
      // Prepare coffee
      int n = sscanf(buf, "N_coffes:%hhu,Aroma:%hhu,Water:%hhu,Temp:%hhu", &n_coff, &aroma, &water, &temp);
      if(n == 4){
        Serial.printf("[Request]\tNum_coffes: %d, Aroma: %d, Water: %d, Temp: %d\n", n_coff, aroma, water, temp);  
        targetState = (STATES) (n_coff+2);
      } else {
        targetState = FAILURE;
        Serial.println("Error: bad format request");
      }
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


void ICACHE_RAM_ATTR timerHandler() { // PROTOCOL
  if (n_cycles == 0) { // Backup
    // Backup codes
    serial_in_temp = serial_in_codes[sim_state]; 
//    on_off = 0; taz1 = 0, taz2 = 0, vap = 0, decal = 0; 
  }
  if (isRising) { // clk RISING edge
//    if( n_cycles < POLL_PERIODS){ // polling buttons
//      if(n_cycles > N_PINS_SHIFTER){
//        digitalWrite(PIN_SERIAL_IN, n_cycles%2);          
//        if(n_cycles%2 == 0){ // Reading Q7
//          on_off += digitalRead(PIN_ON_VAP);
//          taz1 += digitalRead(PIN_T1_DECAL);
//          taz2 += digitalRead(PIN_T2_MAC);
//        }else { // Reading Q8
//          vap += digitalRead(PIN_ON_VAP);
//          decal += digitalRead(PIN_T1_DECAL);
//        }
//      }
//    } 
//    else if (n_cycles < POLL_PERIODS + N_PINS_SHIFTER) {
    if (n_cycles < N_PINS_SHIFTER) {
      digitalWrite(PIN_SERIAL_IN, (serial_in_temp >> n_cycles) & 0b1); // Load SERIAL_IN data
      digitalWrite(PIN_STROBE, HIGH);
    } else {
      digitalWrite(PIN_STROBE, LOW);
    }
    digitalWrite(PIN_CLK, HIGH);
    n_cycles = ++n_cycles;
  }
  else { // clk FALLING edge
//    if (n_cycles < POLL_PERIODS + N_PINS_SHIFTER - 1) { // SHIFT DATA
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
        epoch = millis();
      }
      break;
    case TURNING_ON:
      now = millis();
      if (now - epoch >= STATE_DUR) {
        sim_state = READY;
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
        epoch = millis();      
      }
      break;
    case MAKING_ONE:
      if (millis() - epoch >= STATE_DUR) {
        sim_state = READY;
        targetState = READY;
      }
      break;
    case MAKING_TWO:
      if (millis() - epoch >= STATE_DUR) {  
        sim_state = READY;
        targetState = READY;
      }
      break;
    case STEAM:
      if (millis() - epoch >= STATE_DUR) {
        sim_state = READY;
        targetState = READY;
        epoch = millis();
      }
      break;
    case TURNING_OFF:
      now = millis();
      if (now - epoch >= STATE_DUR) {
        epoch = now;
        sim_state = OFF;
        n_period_inter = 0;
      } else if ((now - epoch) > INTER_DUR * n_period_inter) { // Intermitent leds sync
        if (n_period_inter % 2 == 0) serial_in_codes[TURNING_OFF] = serial_in_codes[MAKING_ONE];
        else serial_in_codes[TURNING_OFF] = serial_in_codes[MAKING_TWO];
        n_period_inter++;
      }
      break;
    default: // Warning cases
      if(targetState != sim_state &&(targetState == TURNING_OFF || targetState == NO_WATER || targetState == CONTAINER_FULL || targetState == FAILURE || targetState == DECALCIF || targetState == READY)){
        sim_state = targetState;
        epoch = millis();
      }
      if (millis() - epoch >= STATE_DUR) {
        sim_state = READY;
        targetState = READY;
      }
  }
}


void configHwProtocol(){
  pinMode(PIN_CLK, OUTPUT);
  pinMode(PIN_SERIAL_IN, OUTPUT);
  pinMode(PIN_STROBE, OUTPUT);
//  pinMode(PIN_ON_VAP, INPUT_PULLUP);
//  pinMode(PIN_T1_DECAL, INPUT);
//  pinMode(PIN_T2_MAC, INPUT);
  sim_state = OFF;
  targetState = OFF;  
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

//   Wifi connection
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callbackMQTT);
  connectMQTT();

  // Coffee machine protocol
  configHwProtocol();
  epoch = millis();
  client.publish("STATUS/caf1", states_names[sim_state]);
  Serial.println((String) "State: " + states_names[sim_state]);
//  Serial.println("on_off taz1 taz2 vap decal");
}


void loop() {
  STATES oldState = sim_state;
  simulateCoffeeMachine();

  if (!client.loop()) {
    Serial.print("MQTT connection lost: rc=");
    Serial.print(client.state());
    Serial.println(" Reconnecting...");
    connectMQTT();
  }
  if(sim_state != oldState){
    client.publish("STATUS/caf1", states_names[sim_state]);
    Serial.println((String) "State: " + states_names[sim_state]);
  }
  delay(1500);
}
