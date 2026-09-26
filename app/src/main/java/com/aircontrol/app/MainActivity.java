package com.aircontrol.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    LinearLayout mainLayout;
    LinearLayout btContainer;
    TextView tvTitle, tvBTStatus;
    Button btnScan, btnSensors;
    LinearLayout podsContainer;
    TextView tvLeftPod, tvRightPod, tvLeftBattery, tvRightBattery;
    ListView lvDevices, lvSensors;
    LinearLayout customizeContainer;
    Button btnCustomizeLeft, btnCustomizeRight;
    
    BluetoothAdapter bluetoothAdapter;
    SensorManager sensorManager;
    List<String> deviceList = new ArrayList<>();
    List<String> sensorList = new ArrayList<>();
    ArrayAdapter<String> deviceAdapter, sensorAdapter;
    boolean isSensorsVisible = false;
    BluetoothDevice connectedDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Main Layout
        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#1a1a2e"));
        mainLayout.setPadding(16, 16, 16, 16);
        
        // Title
        tvTitle = new TextView(this);
        tvTitle.setText("AirControl");
        tvTitle.setTextSize(28);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, 20, 0, 20);
        
        // Bluetooth Status
        tvBTStatus = new TextView(this);
        tvBTStatus.setText("Bluetooth: Not Connected");
        tvBTStatus.setTextSize(16);
        tvBTStatus.setTextColor(Color.parseColor("#a0a0a0"));
        tvBTStatus.setGravity(Gravity.CENTER);
        tvBTStatus.setPadding(0, 0, 0, 16);
        
        // AirPods Container (Transparent with rounded corners)
        btContainer = new LinearLayout(this);
        btContainer.setOrientation(LinearLayout.HORIZONTAL);
        btContainer.setGravity(Gravity.CENTER);
        btContainer.setPadding(20, 30, 20, 30);
        btContainer.setBackground(createRoundedBackground(Color.parseColor("#2d2d44"), 30));
        
        // Left Pod Circle
        tvLeftPod = new TextView(this);
        tvLeftPod.setText("L");
        tvLeftPod.setTextSize(32);
        tvLeftPod.setTextColor(Color.parseColor("#606060"));
        tvLeftPod.setGravity(Gravity.CENTER);
        tvLeftPod.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        tvLeftPod.setBackground(createRoundedBackground(Color.parseColor("#1a1a2e"), 50));
        
        // Right Pod Circle
        tvRightPod = new TextView(this);
        tvRightPod.setText("R");
        tvRightPod.setTextSize(32);
        tvRightPod.setTextColor(Color.parseColor("#606060"));
        tvRightPod.setGravity(Gravity.CENTER);
        tvRightPod.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        tvRightPod.setBackground(createRoundedBackground(Color.parseColor("#1a1a2e"), 50));
        
        btContainer.addView(tvLeftPod);
        btContainer.addView(tvLeftPod, 1); // Spacing
        btContainer.addView(tvRightPod);
        
        // Battery Display
        podsContainer = new LinearLayout(this);
        podsContainer.setOrientation(LinearLayout.HORIZONTAL);
        podsContainer.setGravity(Gravity.CENTER);
        podsContainer.setPadding(0, 16, 0, 16);
        
        tvLeftBattery = new TextView(this);
        tvLeftBattery.setText("L: --%");
        tvLeftBattery.setTextSize(16);
        tvLeftBattery.setTextColor(Color.parseColor("#00ff88"));
        
        tvRightBattery = new TextView(this);
        tvRightBattery.setText("R: --%");
        tvRightBattery.setTextSize(16);
        tvRightBattery.setTextColor(Color.parseColor("#00ff88"));
        
        podsContainer.addView(tvLeftBattery);
        podsContainer.addView(tvRightBattery);
        
        // Scan Button
        btnScan = new Button(this);
        btnScan.setText("Scan Bluetooth Devices");
        btnScan.setTextSize(16);
        btnScan.setBackgroundColor(Color.parseColor("#00d4ff"));
        btnScan.setTextColor(Color.WHITE);
        btnScan.setPadding(20, 16, 20, 16);
        btnScan.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        
        // Sensors Button
        btnSensors = new Button(this);
        btnSensors.setText("Phone Sensors ▼");
        btnSensors.setTextSize(16);
        btnSensors.setBackgroundColor(Color.parseColor("#ff6b6b"));
        btnSensors.setTextColor(Color.WHITE);
        btnSensors.setPadding(20, 16, 20, 16);
        btnSensors.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
        
        // Sensors ListView (Hidden initially)
        lvSensors = new ListView(this);
        lvSensors.setBackgroundColor(Color.parseColor("#2d2d44"));
        lvSensors.setPadding(16, 16, 16, 16);
        lvSensors.setVisibility(View.GONE);
        
        sensorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensorList);
        lvSensors.setAdapter(sensorAdapter);
        
        // Devices ListView
        lvDevices = new ListView(this);
        lvDevices.setBackgroundColor(Color.parseColor("#2d2d44"));
        lvDevices.setPadding(16, 16, 16, 16);
        
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        lvDevices.setAdapter(deviceAdapter);
        
        // Customize Container (Hidden initially)
        customizeContainer = new LinearLayout(this);
        customizeContainer.setOrientation(LinearLayout.VERTICAL);
        customizeContainer.setPadding(16, 16, 16, 16);
        customizeContainer.setBackgroundColor(Color.parseColor("#2d2d44"));
        customizeContainer.setVisibility(View.GONE);
        
        btnCustomizeLeft = new Button(this);
        btnCustomizeLeft.setText("Customize Left Pod");
        btnCustomizeLeft.setTextSize(14);
        btnCustomizeLeft.setBackgroundColor(Color.parseColor("#4ecdc4"));
        btnCustomizeLeft.setTextColor(Color.WHITE);
        
        btnCustomizeRight = new Button(this);
        btnCustomizeRight.setText("Customize Right Pod");
        btnCustomizeRight.setTextSize(14);
        btnCustomizeRight.setBackgroundColor(Color.parseColor("#4ecdc4"));
        btnCustomizeRight.setTextColor(Color.WHITE);
        
        customizeContainer.addView(btnCustomizeLeft);
        customizeContainer.addView(btnCustomizeRight);
        
        // Add all to main layout
        mainLayout.addView(tvTitle);
        mainLayout.addView(tvBTStatus);
        mainLayout.addView(btContainer);
        mainLayout.addView(podsContainer);
        mainLayout.addView(btnScan);
        mainLayout.addView(lvDevices);
        mainLayout.addView(btnSensors);
        mainLayout.addView(lvSensors);
        mainLayout.addView(customizeContainer);
        
        setContentView(mainLayout);
        
        // Initialize
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s : sensors) {
            sensorList.add(s.getName());
        }
        sensorAdapter.notifyDataSetChanged();
        
        // Click Listeners
        btnScan.setOnClickListener(v -> scanDevices());
        
        btnSensors.setOnClickListener(v -> {
            isSensorsVisible = !isSensorsVisible;
            lvSensors.setVisibility(isSensorsVisible ? View.VISIBLE : View.GONE);
            btnSensors.setText(isSensorsVisible ? "Phone Sensors ▲" : "Phone Sensors ▼");
        });
        
        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            if (connectedDevice != null) {
                // Already connected, show customize options
                customizeContainer.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Connected! Customize options below.", Toast.LENGTH_SHORT).show();
            }
        });
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
        
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            return;
        }
        
        bluetoothAdapter.startDiscovery();
        
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                String action = i.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        String name = device.getName() != null ? device.getName() : "Unknown";
                        deviceList.add(name + " - " + device.getAddress());
                        deviceAdapter.notifyDataSetChanged();
                        
                        // Simulate connection (for demo)
                        if (name.toLowerCase().contains("airpods") || name.toLowerCase().contains("air")) {
                            connectedDevice = device;
                            tvBTStatus.setText("Bluetooth: Connected to " + name);
                            tvBTStatus.setTextColor(Color.parseColor("#00ff88"));
                            
                            // Change pod colors
                            tvLeftPod.setTextColor(Color.parseColor("#00ff88"));
                            tvLeftPod.setBackground(createRoundedBackground(Color.parseColor("#00ff8820"), 50));
                            tvRightPod.setTextColor(Color.parseColor("#00ff88"));
                            tvRightPod.setBackground(createRoundedBackground(Color.parseColor("#00ff8820"), 50));
                            
                            // Show battery (simulated)
                            tvLeftBattery.setText("L: 85%");
                            tvRightBattery.setText("R: 90%");
                            
                            // Show customize options
                            customizeContainer.setVisibility(View.VISIBLE);
                            
                            Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if (deviceList.isEmpty()) {
                        Toast.makeText(MainActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        
        Toast.makeText(this, "Scanning...", Toast.LENGTH_SHORT).show();
    }
    
    GradientDrawable createRoundedBackground(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
    }
}
