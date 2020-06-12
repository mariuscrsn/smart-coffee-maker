#define PIN_CLK D3
#define PIN_SERIAL_IN D6
#define PIN_STROBE D5
#define N_PINS_SHIFTER 8
#define N_STATES 10
#define INTER_POLL_PERIODS 100 // 10 times/s
#define STATE_DUR 3000 // 3s 
#define INTER_DUR 500 // 0.5s intermitent leds

enum STATES {
  OFF = 0,
  TURNNING_ON = 1,
  READY = 2,
  MAKING_ONE = 3,
  MAKING_TWO = 4,
  STEAM = 5,
  TURNNING_OFF = 6,
  NO_WATER = 7,
  CONTAINER_FULL = 8,
  FAILURE = 9,
  DECALCIF = 10,
} sim_state;

// ISR variables
volatile unsigned long n_cycles = 0;
volatile bool isRising = true;
volatile unsigned long serial_in_codes[] = {0b00000010, // OFF
                                            0b11000001, // TURNNING_ON -- INTER_SYNC READY, OFF
                                            0b11000001, // READY 
                                            0b10000001, // MAKING_ONE
                                            0b01000001, // MAKING_TWO
                                            0b00100001, // STEAM
                                            0b11000001, // TURNNING_OFF -- INTER_ASYNC ONE, TWO
                                            0b10000010, // NO_WATER 
                                            0b01000010, // CONTAINER_FULL  
                                            0b00100010, // FAILURE  
                                            0b00010010 // DECALCIF
                                            };
volatile unsigned long serial_in_temp;

// gloabl data
unsigned long epoch, now;
unsigned char n_period_inter = 0;

void ICACHE_RAM_ATTR timerHandler() { // PROTOCOL // TODO --> add poll tweak btw Q1 & Q2
  if(isRising){ // clk RISING edge
    if(n_cycles < N_PINS_SHIFTER){
      if(n_cycles == 0) serial_in_temp = serial_in_codes[sim_state]; // Backup codes
      digitalWrite(PIN_SERIAL_IN, (serial_in_temp>>n_cycles)&0b1); // Load SERIAL_IN data 
      digitalWrite(PIN_STROBE, HIGH);
    }else{
      digitalWrite(PIN_STROBE, LOW);
    }
    digitalWrite(PIN_CLK, HIGH);
    n_cycles = ++n_cycles;  
  } 
  else{// clk FALLING edge
    if(n_cycles < N_PINS_SHIFTER-1){ // SHIFT DATA
      digitalWrite(PIN_STROBE, HIGH);
    }else{// Freeze data
      digitalWrite(PIN_STROBE, LOW);
    }
    digitalWrite(PIN_CLK, LOW);    
    if(n_cycles >= INTER_POLL_PERIODS){ // Poll again
      n_cycles = 0;
    }
  }
  isRising = !isRising;
}

void setup() {
  Serial.begin(115200);
  pinMode(2, OUTPUT);
  digitalWrite(2, HIGH);
  pinMode(PIN_CLK, OUTPUT);
  pinMode(PIN_SERIAL_IN, OUTPUT);
  pinMode(PIN_STROBE, OUTPUT);
  sim_state = TURNNING_ON;  
  timer1_isr_init();
  timer1_attachInterrupt(timerHandler);
  timer1_enable(TIM_DIV256, TIM_EDGE, TIM_LOOP); // interrupt on RISING EDGE, Tick_freq --> 80/256 = 312.5KHz, 1 tick = 3.2us
//  timer1_write(312);    // 313*3.2 = 1001.6us ~ 1ms  
  timer1_write(31);    // 31*3.2 = 99.2us
//  timer1_write(312000);    // 312000*3.2 = 1001.6ms ~ 1s  
  epoch = millis();
}


void loop() {
  
  switch(sim_state){
    case OFF :
      if(millis()-epoch >= STATE_DUR) {
        epoch = millis();
        sim_state = TURNNING_ON;
      }
      break;
   case TURNNING_ON:
      now = millis();
      if(now - epoch >= STATE_DUR){
        epoch = millis();
        sim_state = READY;
        Serial.println("READY");
        n_period_inter = 0;
      } 
      else if((now -epoch) > (INTER_DUR*n_period_inter)){ // Intermitent leds sync
          if(n_period_inter%2 == 0) serial_in_codes[TURNNING_ON] = serial_in_codes[READY];
          else serial_in_codes[TURNNING_ON] = serial_in_codes[OFF];
          n_period_inter++;
      }
      break;
   case READY:
      if(millis()-epoch >= STATE_DUR){
        epoch = millis();
        sim_state = MAKING_ONE;
        Serial.println("MAKING_ONE");
      }
      break;
   case MAKING_ONE:
      if(millis()-epoch >= STATE_DUR) {
        epoch = millis();
        sim_state = MAKING_TWO;
        Serial.println("MAKING_TWO");
      }
      break;
   case MAKING_TWO:
      if(millis()-epoch >= STATE_DUR){
        epoch = millis();
        sim_state = STEAM;
        Serial.println("STEAM");
      }
      break;
   case STEAM:
      if(millis()-epoch >= STATE_DUR){
        epoch = millis();
        sim_state = TURNNING_OFF;
        Serial.println("TURNNING_OFF");
      }
      break;
   case TURNNING_OFF:
      now = millis();
      if(now-epoch >= STATE_DUR){
        epoch = now;
        sim_state = NO_WATER;
        Serial.println("NO_WATER");
        n_period_inter = 0;
      } else if((now -epoch) > INTER_DUR*n_period_inter){ // Intermitent leds sync
          if(n_period_inter%2 == 0) serial_in_codes[TURNNING_OFF] = serial_in_codes[MAKING_ONE];
          else serial_in_codes[TURNNING_OFF] = serial_in_codes[MAKING_TWO];
          n_period_inter++;
      }
      break;
   case NO_WATER:
      if(millis()-epoch >= STATE_DUR){
        epoch = millis();
        sim_state = CONTAINER_FULL;
        Serial.println("CONTAINER_FULL");
      }
      break;
   case CONTAINER_FULL:
      if(millis()-epoch >= STATE_DUR){
        epoch = millis();
        sim_state = DECALCIF;
        Serial.println("DECALCIF");
      }
      break;
   case DECALCIF:
      if(millis()-epoch >= STATE_DUR){
        epoch = millis();
        sim_state = FAILURE;
        Serial.println("FAILURE");
      }
      break;
   case FAILURE:
      if(millis()-epoch >= STATE_DUR){
        epoch = millis();
        sim_state = OFF;
        Serial.println("OFF");
      }
      break;
  }
  
}
