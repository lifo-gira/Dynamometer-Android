package com.example.dynamopush;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class Dummy extends AppCompatActivity {

    private static final String TAG = "BLE_Dummy";
    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetoothAdapter;
    private String deviceAddress;
    private TextView dataTextView;
    private final Handler reconnectHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);

        dataTextView = findViewById(R.id.dataTextView);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceAddress = getIntent().getStringExtra("device_address");

        if (deviceAddress != null && bluetoothAdapter != null) {
            Log.d(TAG, "Starting connection to device: " + deviceAddress);
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connectToDevice(device);
        } else {
            Log.e(TAG, "Bluetooth Adapter is null or Device Address is missing");
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        if (device == null) {
            Log.e(TAG, "Device not found. Unable to connect.");
            return;
        }

        if (bluetoothGatt != null) {
            Log.d(TAG, "Closing previous GATT connection before reconnecting...");
            bluetoothGatt.close();
            bluetoothGatt = null;
        }

        Log.d(TAG, "Connecting to device: " + device.getName() + " - " + device.getAddress());
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange: status = " + status + ", newState = " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server. Discovering services...");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server. Status: " + status);
                reconnectWithDelay();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered.");
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.d(TAG, "Service UUID: " + service.getUuid().toString());
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        Log.d(TAG, "Characteristic UUID: " + characteristic.getUuid().toString());
                        if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                            enableNotifications(gatt, characteristic);
                        }
                    }
                }
            } else {
                Log.w(TAG, "Service discovery failed with status: " + status);
            }
        }

        private void enableNotifications(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            gatt.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                Log.d(TAG, "Enabled notifications for " + characteristic.getUuid().toString());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] value = characteristic.getValue();
            if (value != null && value.length > 0) {
                int intValue = bytesToInt(value);
                Log.d(TAG, "Received data: " + intValue);
                runOnUiThread(() -> dataTextView.setText("Force: " + intValue));
            }
        }

        private int bytesToInt(byte[] bytes) {
            try {
                return Integer.parseInt(new String(bytes).trim());
            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to convert data.");
                return -1;
            }
        }
    };

    private void reconnectWithDelay() {
        reconnectHandler.postDelayed(() -> {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                Log.d(TAG, "Reconnecting to BLE device...");
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                connectToDevice(device);
            } else {
                Log.w(TAG, "Bluetooth is off. Cannot reconnect.");
            }
        }, 5000); // Delay of 5 seconds before reconnecting
    }

    private void disconnectGatt() {
        if (bluetoothGatt != null) {
            Log.d(TAG, "Disconnecting Bluetooth GATT...");
            bluetoothGatt.disconnect(); // Disconnects the GATT connection
            bluetoothGatt.close(); // Closes the GATT connection
            bluetoothGatt = null; // Clears the GATT reference
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Checking BLE connection...");
        if (deviceAddress != null && bluetoothGatt == null) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connectToDevice(device);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called. Disconnecting Bluetooth...");
        disconnectGatt();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called. Disconnecting Bluetooth...");
        disconnectGatt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called. Disconnecting Bluetooth...");
        reconnectHandler.removeCallbacksAndMessages(null); // Remove pending reconnection attempts
        disconnectGatt();
    }
}
