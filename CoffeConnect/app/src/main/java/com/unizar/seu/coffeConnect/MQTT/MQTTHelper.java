package com.unizar.seu.coffeConnect.MQTT;

import android.content.Context;
import android.util.Log;

import com.unizar.seu.coffeConnect.hardware.MqttInfo;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTHelper {
    public MqttAndroidClient mqttAndroidClient;
    private MqttInfo mqttInfo;
    public MQTTHelper(Context context, MqttInfo mqttInfo){
        // Create an MqttAndroidClient object and set a callback interface.
        this.mqttInfo = mqttInfo;
        mqttAndroidClient = new MqttAndroidClient(context, mqttInfo.getServerUri(), mqttInfo.getClientId());
        mqttAndroidClient.setCallback(new MQTTCallback());
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(mqttInfo.getUsername());
        mqttConnectOptions.setPassword(mqttInfo.getPassword().toCharArray());

        // Establish an MQTT connection
        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + mqttInfo.getServerUri() + exception.toString());
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }


    private void subscribeToTopic() {
        try {
            for (String topic: mqttInfo.getSubscriptionTopics()) {
                mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.w("Mqtt", "Subscribed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.w("Mqtt", "Subscribed fail!");
                    }
                });
            }

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(String payload) {
        try {
            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(mqttInfo.getPublishTopic(), message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("MQTT", "Publish succeed! ") ;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("MQTT", "Publish failed!") ;
                }
            });
        } catch (MqttException e) {
            Log.e("MQTT", e.toString());
            e.printStackTrace();
        }
    }
}
