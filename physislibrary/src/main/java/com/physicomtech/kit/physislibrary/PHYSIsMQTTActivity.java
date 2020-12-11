package com.physicomtech.kit.physislibrary;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.physicomtech.kit.physislibrary.mqtt.MQTTClient;
import com.physicomtech.kit.physislibrary.mqtt.data.SubscribeData;

import java.lang.ref.WeakReference;

public class PHYSIsMQTTActivity extends AppCompatActivity {

    private static final String PHYSI_MQTT_IP = "physicomtech.com";
    private static final String PHYSI_MQTT_PORT = "1883";

    private MQTTClient mqttClient = null;

    private boolean mqttConnected = false;

    private static class MQTTHandle extends Handler {
        private final WeakReference<PHYSIsMQTTActivity> mActivity;
        MQTTHandle(PHYSIsMQTTActivity activity) {
            mActivity = new WeakReference<PHYSIsMQTTActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            PHYSIsMQTTActivity activity = mActivity.get();
            if (activity != null) {
                activity.handleMessage(msg);
            }
        }
    }
    protected final MQTTHandle physisHandle = new MQTTHandle(this);

    protected void handleMessage(Message msg){
        switch (msg.what) {
            case MQTTClient.MQTT_CONNECTED:
                onMQTTConnectedStatus((boolean) msg.obj);
                mqttConnected = true;
                break;
            case MQTTClient.MQTT_DISCONNECTED:
                onMQTTDisconnected();
                mqttConnected = false;
                break;
            case MQTTClient.MQTT_SUB_LISTEN:
                SubscribeData subData = (SubscribeData) msg.obj;
                String topic = subData.getTopic();
                int sepIndex = topic.indexOf("/");
                onSubscribeListener(topic.substring(0, sepIndex),
                        topic.substring(sepIndex + 1 , topic.length()), subData.getMessage());
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mqttClient = MQTTClient.getInstance(getApplicationContext());
        mqttClient.setHandler(physisHandle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttClient.disconnect();
    }

    protected void connectMQTT(){
        mqttClient.connect(PHYSI_MQTT_IP, PHYSI_MQTT_PORT);
    }

    protected void connectMQTT(String ip, String port){
        mqttClient.connect(ip, port);
    }

    protected void disconnectMQTT(){
        mqttClient.disconnect();
    }

    protected void startSubscribe(String serialNum, String topic){
        if(!mqttConnected)
            return;
        mqttClient.subscribe(serialNum + "/" + topic);
    }

    protected void stopSubscribe(String serialNum, String topic){
        if(!mqttConnected)
            return;
        mqttClient.unsubscribe(serialNum + "/" + topic);
    }

    protected void publish(String serialNum, String topic, String message){
        if(!mqttConnected)
            return;
        mqttClient.publish(serialNum + "/" + topic, message);
    }

    protected void onMQTTConnectedStatus(boolean result){

    }

    protected void onMQTTDisconnected(){

    }

    protected void onSubscribeListener(String serialNum, String topic, String data){

    }
}
