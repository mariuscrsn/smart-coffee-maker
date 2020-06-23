#!/usr/bin/env python2.7

import RPi.GPIO as GPIO  
import time 

def main():
	try:		
		GPIO.setmode(GPIO.BCM)
		GPIO.setup(18, GPIO.OUT)
		p = GPIO.PWM(18, 1) # GPIO on channel 18, freq = 1 GHz
		p.start(50) # dc = 50, 0 <= dc <= 100
		time.sleep(5000)
	except KeyboardInterrupt:  
		GPIO.cleanup()       # clean up GPIO on CTRL+C exit  
	GPIO.cleanup()           # clean up GPIO on normal exit  


if __name__ == "__main__":
  main()

