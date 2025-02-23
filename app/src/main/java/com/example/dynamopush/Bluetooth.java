package com.example.dynamopush;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {

    private static final String TAG = "BLE";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private ListView listView;
    private List<HashMap<String, String>> deviceList = new ArrayList<>();
    private boolean isReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bluetooth);
        hideSystemUI();
        listView = findViewById(R.id.deviceListView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not supported or not enabled.");
            return;
        }

        requestPermissions();
        startScan();
        ImageView imageView = findViewById(R.id.backgroundImage);

        // Set the AnimatedVectorDrawable to the ImageView
        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(getApplicationContext(), R.drawable.your_animated_vector);
        imageView.setImageDrawable(animatedVectorDrawable);

        // Start the animation
        if (animatedVectorDrawable != null) {
            animatedVectorDrawable.start();
        }
    }

    private void requestPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }
    }

    private void startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(scanCallback);
            Log.d(TAG, "Started scanning for BLE devices.");
        } else {
            registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            isReceiverRegistered = true;
            bluetoothAdapter.startDiscovery();
            Log.d(TAG, "Started scanning for classic Bluetooth devices.");
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                if (deviceName != null && !deviceName.isEmpty() && !isDeviceAlreadyAdded(deviceAddress)) {
                    HashMap<String, String> deviceInfo = new HashMap<>();
                    deviceInfo.put("device_name", deviceName);
                    deviceInfo.put("device_address", deviceAddress);

                    deviceList.add(deviceInfo);
                    updateDeviceList();
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan failed with error code: " + errorCode);
            Toast.makeText(Bluetooth.this, "Scan failed: " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };

    private boolean isDeviceAlreadyAdded(String address) {
        for (HashMap<String, String> device : deviceList) {
            if (device.get("device_address").equals(address)) {
                return true;
            }
        }
        return false;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Not needed for BLE scanning
        }
    };

    private void updateDeviceList() {
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                deviceList,
                android.R.layout.simple_list_item_2,
                new String[]{"device_name", "device_address"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceAddress = deviceList.get(position).get("device_address");

            // Start Dummy activity and pass the Bluetooth device address
            Intent intent = new Intent(Bluetooth.this, DynamoTest.class);
            intent.putExtra("device_address", deviceAddress);
            startActivity(intent);
        });
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}