package com.physicomtech.kit.smartfarm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.physicomtech.kit.physislibrary.PHYSIsMQTTActivity;
import com.physicomtech.kit.smartfarm.customize.OnSingleClickListener;
import com.physicomtech.kit.smartfarm.customize.SerialNumberView;
import com.physicomtech.kit.smartfarm.dialog.LoadingDialog;
import com.physicomtech.kit.smartfarm.helper.PHYSIsPreferences;
import com.physicomtech.kit.smartfarm.wifi.WIFIManager;

public class MainActivity extends PHYSIsMQTTActivity {

    private static final String TAG = "MainActivity";

    SerialNumberView snvSetup;
    Button btnConnect, btnWiFiSetup, btnMonitoring;
    TextView tvTemperature, tvHumidity, tvIllumination;
    TextView tvFanSpeed, tvPumpStatus, tvLedStatus;
    Button btnFanSpeed1, btnFanSpeed2, btnFanSpeed3;
    Button btnPumpOn, btnPumpOff, btnLedOn, btnLedOff;

    private static final String SUB_SENSING_TOPIC = "Sensing";
    private static final String SUB_CONTROL_TOPIC = "Status";
    private static final String PUC_CONTROL_TOPIC = "Control";

    private PHYSIsPreferences preferences;

    private String serialNumber = null;
    private boolean isConnected = false;
    private boolean isMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        snvSetup.setSerialNumber("20C38F8EC64E");
    }

    @Override
    protected void onMQTTConnectedStatus(boolean result) {
        super.onMQTTConnectedStatus(result);
        setConnectedResult(result);
    }

    @Override
    protected void onMQTTDisconnected() {
        super.onMQTTDisconnected();
        isConnected = false;
        btnConnect.setText("Connect");
        setMonitoring(isMonitoring = false);
    }

    @Override
    protected void onSubscribeListener(String serialNum, String topic, String data) {
        super.onSubscribeListener(serialNum, topic, data);
        if(serialNumber.equals(serialNum)){
            if(topic.equals(SUB_SENSING_TOPIC))
                showMonitoringData(data);
            else if(topic.equals(SUB_CONTROL_TOPIC)){
                setModuleStatus(data);
            }
        }
    }

    /*
            Event
     */
    final SerialNumberView.OnSetSerialNumberListener onSetSerialNumberListener = new SerialNumberView.OnSetSerialNumberListener() {
        @Override
        public void onSetSerialNumber(String serialNum) {
            preferences.setPhysisSerialNumber(serialNumber = serialNum);
            Log.e(TAG, "# Set Serial Number : " + serialNumber);
        }
    };

    final OnSingleClickListener onClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()){
                case R.id.btn_wifi_setup:
                    if(serialNumber == null){
                        Toast.makeText(getApplicationContext(), "PHYSIs Kit의 시리얼 넘버를 설정하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!WIFIManager.getLocationProviderStatus(getApplicationContext())){
                        Toast.makeText(getApplicationContext(), "위치 상태를 활성화하고 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(new Intent(MainActivity.this, SetupActivity.class)
                            .putExtra("SERIALNUMBER", serialNumber));
                    break;
                case R.id.btn_connect:
                    if(serialNumber == null){
                        Toast.makeText(getApplicationContext(), "PHYSIs Kit의 시리얼 넘버를 설정하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(isConnected){
                        disconnectMQTT();
                    }else{
                        LoadingDialog.show(MainActivity.this, "Connecting..");
                        connectMQTT();
                    }
                    break;
                case R.id.btn_monitoring:
                    if(!isConnected){
                        Toast.makeText(getApplicationContext(), "MQTT가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    setMonitoring(isMonitoring = !isMonitoring);
                    break;
                case R.id.btn_fan_speed1:
                    publish(serialNumber, PUC_CONTROL_TOPIC, "F0");
                    break;
                case R.id.btn_fan_speed2:
                    publish(serialNumber, PUC_CONTROL_TOPIC, "F1");
                    break;
                case R.id.btn_fan_speed3:
                    publish(serialNumber, PUC_CONTROL_TOPIC, "F2");
                    break;
                case R.id.btn_pump_on:
                    publish(serialNumber, PUC_CONTROL_TOPIC, "P1");
                    break;
                case R.id.btn_pump_off:
                    publish(serialNumber, PUC_CONTROL_TOPIC, "P0");
                    break;
                case R.id.btn_led_on:
                    publish(serialNumber, PUC_CONTROL_TOPIC, "L1");
                    break;
                case R.id.btn_led_off:
                    publish(serialNumber, PUC_CONTROL_TOPIC, "L0");
                    break;
            }
        }
    };


    /*
            Helper Method
     */
    @SuppressLint("SetTextI18n")
    private void setModuleStatus(String data){
        if(data == null || data.equals(""))
            return;
        Log.e(TAG, "Status Data : " + data);
        String[] values = data.split(",");

        if (values.length != 3)
            return;

        tvFanSpeed.setText("FAN SPEED\n" + values[0]);
        tvPumpStatus.setText("PUMP STATUS\n" + (values[1].equals("0") ? "OFF" : "ON"));
        tvLedStatus.setText("LED STATUS\n" + (values[2].equals("0") ? "OFF" : "ON"));
    }

    @SuppressLint("SetTextI18n")
    private void showMonitoringData(String data) {
        if(data == null || data.equals(""))
            return;
        Log.e(TAG, "Sensing Data : " + data);
        String[] values = data.split(",");

        if (values.length != 3)
            return;

        tvTemperature.setText(values[0] + " ℃");
        tvHumidity.setText(values[1] + " %");
        tvIllumination.setText(values[2] + " %");
    }


    private void setMonitoring(boolean enable){
        if(enable){
            startSubscribe(serialNumber, SUB_SENSING_TOPIC);
            startSubscribe(serialNumber, SUB_CONTROL_TOPIC);
            btnMonitoring.setText("Stop Monitoring");
        }else{
            stopSubscribe(serialNumber, SUB_SENSING_TOPIC);
            stopSubscribe(serialNumber, SUB_CONTROL_TOPIC);
            btnMonitoring.setText("Start Monitoring");
        }
    }

    @SuppressLint("SetTextI18n")
    private void setConnectedResult(boolean state){
        LoadingDialog.dismiss();

        if(isConnected = state){
            btnConnect.setText("Disconnect");
        }

        String toastMsg;
        if(isConnected) {
            toastMsg = "PHYSIs MQTT Broker 연결에 성공하였습니다.";
        } else {
            toastMsg = "PHYSIs MQTT Broker 연결에 실패하였습니다.";
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
    }


    @SuppressLint("WrongConstant")
    private void init() {
        preferences = new PHYSIsPreferences(getApplicationContext());
        serialNumber = preferences.getPhysisSerialNumber();

        snvSetup = findViewById(R.id.snv_setup);
        snvSetup.setSerialNumber(serialNumber);
        snvSetup.showEditView(serialNumber == null);
        snvSetup.setOnSetSerialNumberListener(onSetSerialNumberListener);

        btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(onClickListener);
        btnWiFiSetup = findViewById(R.id.btn_wifi_setup);
        btnWiFiSetup.setOnClickListener(onClickListener);
        btnMonitoring = findViewById(R.id.btn_monitoring);
        btnMonitoring.setOnClickListener(onClickListener);

        tvTemperature = findViewById(R.id.tv_temperature_val);
        tvHumidity = findViewById(R.id.tv_humidity_val);
        tvIllumination = findViewById(R.id.tv_illumination_val);

        tvFanSpeed = findViewById(R.id.tv_fan_speed);
        tvPumpStatus = findViewById(R.id.tv_pump_status);
        tvLedStatus = findViewById(R.id.tv_led_status);

        btnFanSpeed1 = findViewById(R.id.btn_fan_speed1);
        btnFanSpeed1.setOnClickListener(onClickListener);
        btnFanSpeed2 = findViewById(R.id.btn_fan_speed2);
        btnFanSpeed2.setOnClickListener(onClickListener);
        btnFanSpeed3 = findViewById(R.id.btn_fan_speed3);
        btnFanSpeed3.setOnClickListener(onClickListener);
        btnPumpOn = findViewById(R.id.btn_pump_on);
        btnPumpOn.setOnClickListener(onClickListener);
        btnPumpOff = findViewById(R.id.btn_pump_off);
        btnPumpOff.setOnClickListener(onClickListener);
        btnLedOn = findViewById(R.id.btn_led_on);
        btnLedOn.setOnClickListener(onClickListener);
        btnLedOff = findViewById(R.id.btn_led_off);
        btnLedOff.setOnClickListener(onClickListener);
    }
}
