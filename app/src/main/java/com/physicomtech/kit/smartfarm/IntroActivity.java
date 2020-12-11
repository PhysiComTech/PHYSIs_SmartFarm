package com.physicomtech.kit.smartfarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private static final long INTRO_DELAY = 1500;
    private static final int REQ_APP_PERMISSION = 101;

    private Handler handler = new Handler();
    private String[] appPermissionList = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private void checkPermissions(){
        final List<String> reqPermissions = new ArrayList<>();
        for(String permission : appPermissionList){
            if(checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED)
                reqPermissions.add(permission);
        }
        if(reqPermissions.size() == 0){
            handler.postDelayed(startActivityRunnable, INTRO_DELAY);
        }else{
            requestPermissions(reqPermissions.toArray(new String[reqPermissions.size()]), REQ_APP_PERMISSION);
        }
    }

    private Runnable startActivityRunnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(startActivityRunnable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQ_APP_PERMISSION){
            boolean isBlocking = false;
            for(int result : grantResults){
                isBlocking = result == PackageManager.PERMISSION_DENIED;
                if(isBlocking)
                    break;
            }
            if(isBlocking){
                Toast.makeText(getApplicationContext(), "Permission blocked.", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                handler.postDelayed(startActivityRunnable, INTRO_DELAY);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        checkPermissions();
    }

}