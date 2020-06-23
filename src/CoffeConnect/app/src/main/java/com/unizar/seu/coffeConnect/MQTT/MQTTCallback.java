package com.unizar.seu.coffeConnect.MQTT;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTCallback implements MqttCallbackExtended {

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        Log.i("MQTT", serverURI);
    }

    @Override
    public void connectionLost(Throwable cause) {}

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i("MQTT", "topic: " + topic + ", msg: " + new String(message.getPayload()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) { }
}
