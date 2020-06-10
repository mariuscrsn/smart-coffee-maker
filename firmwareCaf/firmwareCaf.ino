

void setup() {
  Serial.begin(9600);


  pinMode(2, INPUT);
  pinMode(3, INPUT);
  pinMode(4, INPUT);
  pinMode(5, INPUT);
  pinMode(6, INPUT);
  
}

// the loop routine runs over and over again forever:
void loop() {
  // read the input pin:
  int taz_2= digitalRead(2);
  int taz_1 = digitalRead(3);
  int clk   = digitalRead(4);
  int serial_in = digitalRead(5);
  int storbe = digitalRead(6);
  
  Serial.println(sprintf("taz_1: %d, taz_2: %d, clk: %d, serial_in: %d, storbe: %d\n", taz_1, taz_2, clk, serial_in, storbe));
  delay(100);        // delay in between reads for stability
}
