#define MAX_INTS 300
int pin_clk = 2;
volatile int n_ints = 0;
volatile unsigned long meas[MAX_INTS];


void setup() {
  Serial.begin(115200);
//  Serial.println("Empezando000..");
  pinMode(pin_clk, INPUT);
//  pinMode(3, INPUT_PULLUP);
//  pinMode(4, INPUT_PULLUP);
//  pinMode(5, INPUT_PULLUP);
//  pinMode(3, OUTPUT);
//  analogWrite(3, 250);
//  attachInterrupt(digitalPinToInterrupt(pin_clk), get_clk, RISING);
  noInterrupts();
}

void loop() {
//  delay(3000);
//  detachInterrupt(digitalPinToInterrupt(pin_clk));
  int i = 0;
  
  while(i< MAX_INTS){ // pulse in
      meas[i]= pulseIn(pin_clk, LOW, 3000000);  
      i++;
  }
  i = 0;
  while(i< MAX_INTS-1){ // show diffs
//      Serial.println(meas[i+1] - meas[i]); // diff duration
//      Serial.println(meas[i]); // pulse_in duration
      i++;
  }
  
//  if(finished){
//    int i = 0;
//    while(i< MAX_INTS-1){
//      Serial.println(meas[i+1] - meas[i]);
//      i++;
//    }
//    delay(1000*1000);
//  }
//  delay(1000);
}


void get_clk() {
  meas[n_ints]= micros();
  n_ints = (++n_ints)%MAX_INTS;
  //  
  //  ts[n_ints] = micros();
  //  if ( !finishedInts && n_ints >= MAX_INTS-1)
  //    finishedInts = true;
//  Serial.print(digitalRead(2));
//  Serial.print(" ");
}
