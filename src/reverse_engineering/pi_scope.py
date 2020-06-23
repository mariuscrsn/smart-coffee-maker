#!/usr/bin/env python2.7  
# script by Alex Eames https://raspi.tv  
# https://raspi.tv/how-to-use-interrupts-with-python-on-the-raspberry-pi-and-rpi-gpio-part-3  
import RPi.GPIO as GPIO  
import time 
from datetime import datetime, timedelta
import pandas as pd
import matplotlib.pyplot as plt
import statistics as st
from heapq import merge 

class SWOscillo:
  def __init__(self, pinout, measure_dur=0.5):
    '''
     Take measure on clk edge while duration_measure
    '''
    self.measure_dur = measure_dur*1000*1000
    self.clk = [];
    self.ts = []; self.serial_in = []; self.storbe = []
    self.stop = False
    self.df = pd.DataFrame()
    self.pinout = pinout
    self.cnt = 0
    GPIO.setmode(GPIO.BCM)    
    GPIO.setup(2, GPIO.IN, pull_up_down=GPIO.PUD_UP)  
    GPIO.setup(3, GPIO.IN, pull_up_down=GPIO.PUD_UP)  
    GPIO.setup(4, GPIO.IN, pull_up_down=GPIO.PUD_UP) 
    
  def ISR_clk(self, channel):  
    diff =  (datetime.now() - self.epoch)/timedelta(microseconds=1)
    if diff < self.measure_dur:
      self.cnt += 1
      '''
      #measures = {'ts' : diff, 'clk': GPIO.input(4)}      
      
      #for m in self.pinout.keys():
      #  measures[m] = GPIO.input(self.pinout[m])
      #self.df = self.df.append({'ts' : diff, 'clk': GPIO.input(4)}, ignore_index=True)
    
      '''
      value_clk = GPIO.input(4)
      self.clk.append(value_clk)
      self.ts.append(diff)
      self.serial_in.append(GPIO.input(3))
      self.storbe.append(GPIO.input(2))
      #'''
    else: 
      self.stop = True
      GPIO.remove_event_detect(channel)
      
  def start(self):
    self.epoch = datetime.now()
    GPIO.add_event_detect(4, GPIO.BOTH, callback=self.ISR_clk)
    
    while not self.stop:
        print('measuring...')
        time.sleep(1)


def plotGraf(df: pd.DataFrame):
  # style
  plt.style.use('seaborn-darkgrid') 
  # create a color palette
  palette = plt.get_cmap('Set1')
  # multiple line plot
  ts = df['ts']
  num=0
  for column in df.drop(['clk', 'ts'], axis=1):
    num+=1
    plt.plot(ts, df[column], marker='', color=palette(num), linewidth=1, alpha=0.9, label=column)
  num+=1
  squared_clk = [x*(1-i)+(1-x)*i for x in df['clk'] for i in [0,1]] # duplicate clk to show it as square
  plt.plot(list(merge(ts,ts)), squared_clk, marker='', color=palette(num), linewidth=1, alpha=0.9, label=column)
   
  # Add legend
  plt.legend(loc=2, ncol=2)
   
  # Add titles
  plt.title("Delonghi ESAM 4000 - Control Panel - Protocol", loc='left', fontsize=12, fontweight=0, color='orange')
  plt.xlabel("Time [us]")
  plt.ylabel("Value [V]")
  plt.legend(loc='best')
  plt.show()


def main():

  try:
    
    # Take measures
    pinout = {'clk': 4, 'serial_in': 3, 'storbe': 2}
    #osc = SWOscillo(pinout, 1)
    #osc.start()
    #print(osc.cnt)
    '''
    df = pd.DataFrame({'ts': osc.ts, 'clk' : osc.clk, 'storbe': osc.storbe})
    diffs_ts = [df['ts'][i+1] - x for i, x in enumerate(df['ts'][:-1])]
    print('Nº Measures: ' + str(len(df['clk'])) + '\nMin: ' + str(min(diffs_ts)) + '\tMax: ' + str(max(diffs_ts)) + '\tMed: ' + str(st.median(diffs_ts)) + '\tAvg: ' + str(st.mean(diffs_ts)))
    '''
    '''
    # Plot data
    diffs_ts = [osc.df['ts'][i+1] - x for i, x in enumerate(osc.df['ts'][:-1])]
    print('Nº Measures: ' + str(len(osc.df['clk'])) + '\nMin: ' + str(min(diffs_ts)) + '\tMax: ' + str(max(diffs_ts)) + '\tMed: ' + str(st.median(diffs_ts)) + '\tAvg: ' + str(st.mean(diffs_ts)))
    plotGraf(osc.df)
    '''
    i = 0
    clk = []
    ts = []
    GPIO.setmode(GPIO.BCM)    
    GPIO.setup(2, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)  
    GPIO.setup(3, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)  
    GPIO.setup(4, GPIO.IN, pull_up_down=GPIO.PUD_UP)
    epoch = datetime.now()
    while i < 1000000:
        #ts.append( (datetime.now() - epoch)/timedelta(microseconds=1)36)
        clk.append(GPIO.input(4))
        #print(GPIO.input(17))
        i+=1
  
    print((datetime.now() - epoch)/timedelta(seconds=1))
    new_clk = []
    new_clk.append(clk[0])
    #for i, x in enumerate(clk[1:-1]):
    '''
    for i, x in enumerate(clk[1:50000]):
      if x != new_clk[-1]:
         new_clk.append(x)
      elif x!= clk[i+1]:
         new_clk.append(x)          
    print(len(new_clk))
    '''
    plt.plot([x for x in range(500000)], clk[:500000], marker='', color='red', linewidth=1, alpha=0.9, label='clk')
    plt.show()
  except KeyboardInterrupt:  
      GPIO.cleanup()       # clean up GPIO on CTRL+C exit  
  GPIO.cleanup()           # clean up GPIO on normal exit  


if __name__ == "__main__":
  main()
