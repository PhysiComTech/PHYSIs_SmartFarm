package com.physicomtech.kit.smartfarm.wifi;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.physicomtech.kit.smartfarm.R;


public class WiFiHolder extends RecyclerView.ViewHolder {

    ImageView ivSecret, ivRSSI;
    TextView tvSSID;
    RelativeLayout rlItem;

    public WiFiHolder(@NonNull View itemView) {
        super(itemView);

        ivSecret = itemView.findViewById(R.id.iv_wifi_secret);
        ivRSSI = itemView.findViewById(R.id.iv_wifi_rssi);
        tvSSID = itemView.findViewById(R.id.tv_wifi_ssid);
        rlItem = itemView.findViewById(R.id.rl_wifi_item);
    }
}
