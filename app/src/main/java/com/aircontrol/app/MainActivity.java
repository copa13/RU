package com.aircontrol.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView tvStatus;
    ListView lvDevices, lvSensors;
    Button btnScan;
    BluetoothAdapter bluetoothAdapter;
    SensorManager sensorManager;
    List<String> deviceList = new ArrayList<>();
    List<String> sensorList = new ArrayList<>();
    ArrayAdapter<String> deviceAdapter, sensorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        tvStatus = new TextView(this);
        tvStatus.setText("AirControl - Bluetooth & Sensor Test");
        tvStatus.setTextSize(20);
        
        lvDevices = new ListView(this);
        lvSensors = new ListView(this);
        
        btnScan = new Button(this);
        btnScan.setText("Scan Bluetooth Devices");
        
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        lvDevices.setAdapter(deviceAdapter);
        
        sensorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensorList);
        lvSensors.setAdapter(sensorAdapter);
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensors) {
            sensorList.add(s.getName());
        }
        sensorAdapter.notifyDataSetChanged();
        
        btnScan.setOnClickListener(v -> scanDevices());
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        layout.addView(tvStatus);
        layout.addView(btnScan);
        layout.addView(lvDevices);
        layout.addView(lvSensors);
        
        setContentView(layout);
    }

    void scanDevices() {
        deviceList.clear();
        deviceAdapter.notifyDataSetChanged();
        
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            }, 1);
            return;
        }
        
        bluetoothAdapter.startDiscovery();
        
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                if (BluetoothDevice.ACTION_FOUND.equals(i.getAction())) {
                    BluetoothDevice d = i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (d != null) {
                        String name = d.getName() != null ? d.getName() : "Unknown";
                        deviceList.add(name + " - " + d.getAddress());
                        deviceAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
        
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show();
    }
}
