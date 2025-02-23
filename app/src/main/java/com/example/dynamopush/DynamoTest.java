package com.example.dynamopush;

import static com.example.dynamopush.Login.patientDetails;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DynamoTest extends AppCompatActivity {

    private ONNXModelHelper modelHelper;
    String deviceName;
    ImageView toggleImage, backButton, backExerciseButton;
    TextView toggleText, repCountTextView, userName;
    boolean isGauge = true;
    private static final String TAG = "BLE_Dummy";
    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetoothAdapter;
    private String deviceAddress;
    private TextView forceCountNumber;
    private final Handler reconnectHandler = new Handler(Looper.getMainLooper());
    private static final int MAX_PROGRESS = 7000;
    private CircularProgressView circularProgressView;
    private Handler handler = new Handler();
    private float progress = 0; // Initial progress
    private boolean isFront = true;
    private LineChart lineChart;
    private List<Entry> entries = new ArrayList<>();
    private LineDataSet dataSet;
    private LineData lineData;
    private int xIndex = 0;
    private ImageView startStopButton;
    private boolean isRunning = false;
    private int xValue = 0; // X-axis counter for the chart
    private ImageView leftThigh, rightThigh, rightBiceps, leftBiceps, leftPec, rightPec, leftOblique, rightOblique, abs, leftHip, rightHip, leftTrap, rightTrap, leftDeltoid, rightDeltoid, leftGlute, rightGlute, leftCalves, rightCalves, leftHamstring, rightHamstring, leftLat, rightLat, rightTriceps, leftTriceps;
    private TextView selectedMuscleText;
    private ChipGroup chipGroup;
    private static List<String> selectedMuscles = new ArrayList<>();
    private int currentMuscleIndex = 0;
    private TextView selectedMuscleTextView;
    private ImageView nextExerciseButton;
    private List<Float> forceValues = new ArrayList<>();
    private List<Map<String, Object>> sessionDataList = new ArrayList<>(); // Store all sessions
    private Map<String, List<Float>> muscleData = new HashMap<>();
    private Set<String> uniqueMuscles = new HashSet<>();
    private List<Float> currentForceValues = new ArrayList<>();
    private Map<String, Map<String, List<Float>>> repData = new HashMap<>();
    private List<Map<String, Object>> allRepsData = new ArrayList<>(); // Stores all reps
    private Map<String, List<Float>> currentRepData = new HashMap<>(); // Stores data for the current rep
    private int repCount = 1;
    public String firstName, lastName, email;
    TextView onProgressMuscle, totalMuscleSelected;

    private Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            progress += 1;
            if (progress > 100) {
                progress = 0;  // Reset progress after 100%
            }
            circularProgressView.setProgress(progress);
            handler.postDelayed(this, 100);  // Update every 100ms
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dynamo_test);
        hideSystemUI();
        modelHelper = new ONNXModelHelper(this); // Initialize ONNX model
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceAddress = getIntent().getStringExtra("device_address");

        if (deviceAddress != null && bluetoothAdapter != null) {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

            deviceName = device.getName(); // Retrieve the device name
            if (deviceName == null || deviceName.isEmpty()) {
                Log.w(TAG, "Device name is null, trying to fetch again after connection...");
            } else {
                Log.d(TAG, "Connecting to device: " + deviceName + " (" + deviceAddress + ")");
            }

            connectToDevice(device);
        } else {
            Log.e(TAG, "Bluetooth Adapter is null or Device Address is missing");
        }

        onProgressMuscle = findViewById(R.id.onProgressMuscle);
        totalMuscleSelected = findViewById(R.id.totalMuscleSelected);
        startStopButton = findViewById(R.id.start_stop);
        userName = findViewById(R.id.userName);
        // Initialize and set up the LineChart in one function
        try {
            firstName = patientDetails.getString("first_name");
            lastName = patientDetails.getString("last_name");
            userName.setText(firstName + " " + lastName);
            email = patientDetails.getString("email");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        lineChart = findViewById(R.id.line_chart);
        toggleImage = findViewById(R.id.toggleImage);

        toggleText = findViewById(R.id.toggleText);
        repCountTextView = findViewById(R.id.repCount);
        LinearLayout toggleView = findViewById(R.id.toggleView);
        LinearLayout getStarted = findViewById(R.id.getStarted);
        selectedMuscleTextView = findViewById(R.id.selected_muscle);
        nextExerciseButton = findViewById(R.id.next_exercise);
        backExerciseButton = findViewById(R.id.back_exercise);
        // Set initial values
        if (!selectedMuscles.isEmpty()) {
            onProgressMuscle.setText("1"); // First muscle
            totalMuscleSelected.setText(String.valueOf(selectedMuscles.size()));
            selectedMuscleTextView.setText(selectedMuscles.get(0));
        } else {
            onProgressMuscle.setText("0");
            totalMuscleSelected.setText("0");
        }

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DynamoTest.this, Dashboard.class);
                resetSessionData();
                startActivity(intent);
                finish();
            }
        });

        startStopButton.setOnClickListener(view -> {
            if (isRunning) {
                stopProgress();
            } else {
                startProgress();
            }
        });

        toggleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMuscles.isEmpty()) {
                    // Prevent click action if no muscles are selected
                    Toast.makeText(v.getContext(), "Please select at least one muscle", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the first selected muscle (or change logic based on how selection works)
                String currentMuscle = selectedMuscles.get(0); // Assuming first selected muscle

                // Proceed with toggling if muscles are selected
                if (isGauge) {
                    toggleImage.setImageResource(R.drawable.speedometer); // Change to Gauge
                    toggleText.setText("Model");
                } else {
                    updateToggleImage(currentMuscle); // Change to selected muscle image
                    toggleText.setText("Gauge");
                }
                isGauge = !isGauge; // Toggle state
            }
        });


        forceCountNumber = findViewById(R.id.forceCountNumber);

        // Get reference to CircularProgressView
        circularProgressView = findViewById(R.id.circularProgressView);

        getStarted.setOnClickListener(v -> {

            if (!selectedMuscles.isEmpty()) {
                Toast.makeText(this, "Muscles already chosen has to be completed", Toast.LENGTH_SHORT).show();
                return;
            }
            // Create AlertDialog Builder
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // Inflate the custom layout for the dialog
            View dialogView = getLayoutInflater().inflate(R.layout.select_model, null);
            builder.setView(dialogView);

            // Initialize views from the dialog layout
            chipGroup = dialogView.findViewById(R.id.chipGroup);
            LinearLayout changeModel = dialogView.findViewById(R.id.changeModel);
            ImageView toggleImage = dialogView.findViewById(R.id.toggleImage);
            ImageView closeIcon = dialogView.findViewById(R.id.close_icon);
            selectedMuscleText = dialogView.findViewById(R.id.selected_muscle_text); // Add this ID to your XML

            // Front Model Muscles
            leftThigh = dialogView.findViewById(R.id.left_thigh);
            rightThigh = dialogView.findViewById(R.id.right_thigh);
            rightBiceps = dialogView.findViewById(R.id.right_biceps);
            leftBiceps = dialogView.findViewById(R.id.left_biceps);
            leftPec = dialogView.findViewById(R.id.left_pecs);
            rightPec = dialogView.findViewById(R.id.right_pecs);
            leftOblique = dialogView.findViewById(R.id.left_oblique);
            rightOblique = dialogView.findViewById(R.id.right_oblique);
            abs = dialogView.findViewById(R.id.abs);
            leftHip = dialogView.findViewById(R.id.left_hip);
            rightHip = dialogView.findViewById(R.id.right_hip);

            setMuscleClickListener(leftThigh, "Left Thigh");
            setMuscleClickListener(rightThigh, "Right Thigh");
            setMuscleClickListener(rightBiceps, "Right Biceps");
            setMuscleClickListener(leftBiceps, "Left Biceps");
            setMuscleClickListener(leftPec, "Left Pec");
            setMuscleClickListener(rightPec, "Right Pec");
            setMuscleClickListener(leftOblique, "Left Oblique");
            setMuscleClickListener(rightOblique, "Right Oblique");
            setMuscleClickListener(abs, "Abs");
            setMuscleClickListener(leftHip, "Left Hip");
            setMuscleClickListener(rightHip, "Right Hip");

// Back Model Muscles
            leftLat = dialogView.findViewById(R.id.left_lats);
            rightLat = dialogView.findViewById(R.id.right_lats);
            leftTriceps = dialogView.findViewById(R.id.left_triceps);
            rightTriceps = dialogView.findViewById(R.id.right_triceps);
            leftHamstring = dialogView.findViewById(R.id.left_hamstring);
            rightHamstring = dialogView.findViewById(R.id.right_hamstrings);
            leftTrap = dialogView.findViewById(R.id.left_traps);
            rightTrap = dialogView.findViewById(R.id.right_traps);
            leftDeltoid = dialogView.findViewById(R.id.left_deltoids);
            rightDeltoid = dialogView.findViewById(R.id.right_deltoids);
            leftGlute = dialogView.findViewById(R.id.left_glutes);
            rightGlute = dialogView.findViewById(R.id.right_glutes);
            leftCalves = dialogView.findViewById(R.id.left_calves);
            rightCalves = dialogView.findViewById(R.id.right_calves);

            setMuscleClickListener(leftLat, "Left Lat");
            setMuscleClickListener(rightLat, "Right Lat");
            setMuscleClickListener(leftTriceps, "Left Triceps");
            setMuscleClickListener(rightTriceps, "Right Triceps");
            setMuscleClickListener(leftHamstring, "Left Hamstring");
            setMuscleClickListener(rightHamstring, "Right Hamstring");
            setMuscleClickListener(leftTrap, "Left Trap");
            setMuscleClickListener(rightTrap, "Right Trap");
            setMuscleClickListener(leftDeltoid, "Left Deltoid");
            setMuscleClickListener(rightDeltoid, "Right Deltoid");
            setMuscleClickListener(leftGlute, "Left Glute");
            setMuscleClickListener(rightGlute, "Right Glute");
            setMuscleClickListener(leftCalves, "Left Calf");
            setMuscleClickListener(rightCalves, "Right Calf");


            restoreChips();

            // Toggle Image and Text on Click
            changeModel.setOnClickListener(h -> {
                if (isFront) {
                    toggleImage.setImageResource(R.drawable.front_model); // Set the front image
                    selectedMuscleText.setText("FRONT"); // Update the text

                    // Show front-related images
                    leftThigh.setVisibility(View.VISIBLE);
                    rightThigh.setVisibility(View.VISIBLE);
                    rightBiceps.setVisibility(View.VISIBLE);
                    leftBiceps.setVisibility(View.VISIBLE);
                    leftPec.setVisibility(View.VISIBLE);
                    rightPec.setVisibility(View.VISIBLE);
                    leftOblique.setVisibility(View.VISIBLE);
                    rightOblique.setVisibility(View.VISIBLE);
                    abs.setVisibility(View.VISIBLE);
                    leftHip.setVisibility(View.VISIBLE);
                    rightHip.setVisibility(View.VISIBLE);

                    // Hide back-related images
                    rightTriceps.setVisibility(View.GONE);
                    leftTriceps.setVisibility(View.GONE);
                    leftHamstring.setVisibility(View.GONE);
                    rightHamstring.setVisibility(View.GONE);
                    leftLat.setVisibility(View.GONE);
                    rightLat.setVisibility(View.GONE);
                    leftTrap.setVisibility(View.GONE);
                    rightTrap.setVisibility(View.GONE);
                    leftDeltoid.setVisibility(View.GONE);
                    rightDeltoid.setVisibility(View.GONE);
                    leftGlute.setVisibility(View.GONE);
                    rightGlute.setVisibility(View.GONE);
                    leftCalves.setVisibility(View.GONE);
                    rightCalves.setVisibility(View.GONE);
                } else {
                    toggleImage.setImageResource(R.drawable.back_model); // Set the back image
                    selectedMuscleText.setText("BACK"); // Update the text

                    // Show back-related images
                    rightTriceps.setVisibility(View.VISIBLE);
                    leftTriceps.setVisibility(View.VISIBLE);
                    leftHamstring.setVisibility(View.VISIBLE);
                    rightHamstring.setVisibility(View.VISIBLE);
                    leftLat.setVisibility(View.VISIBLE);
                    rightLat.setVisibility(View.VISIBLE);
                    leftTrap.setVisibility(View.VISIBLE);
                    rightTrap.setVisibility(View.VISIBLE);
                    leftDeltoid.setVisibility(View.VISIBLE);
                    rightDeltoid.setVisibility(View.VISIBLE);
                    leftGlute.setVisibility(View.VISIBLE);
                    rightGlute.setVisibility(View.VISIBLE);
                    leftCalves.setVisibility(View.VISIBLE);
                    rightCalves.setVisibility(View.VISIBLE);

                    // Hide front-related images
                    leftThigh.setVisibility(View.GONE);
                    rightThigh.setVisibility(View.GONE);
                    rightBiceps.setVisibility(View.GONE);
                    leftBiceps.setVisibility(View.GONE);
                    leftPec.setVisibility(View.GONE);
                    rightPec.setVisibility(View.GONE);
                    leftOblique.setVisibility(View.GONE);
                    rightOblique.setVisibility(View.GONE);
                    abs.setVisibility(View.GONE);
                    leftHip.setVisibility(View.GONE);
                    rightHip.setVisibility(View.GONE);
                }

                isFront = !isFront; // Toggle state
            });

            // Create and show the AlertDialog
            AlertDialog dialog = builder.create();
            closeIcon.setOnClickListener(i -> dialog.dismiss());
            // Set full width for the dialog
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            dialog.setOnDismissListener(d -> Log.d("SelectedMuscles", "Final list: " + selectedMuscles));

            dialog.show();
        });

        // Handle next exercise button click
        nextExerciseButton.setOnClickListener(v -> {
            if (!isRunning) {
                if (!selectedMuscles.isEmpty()) {
                    if (currentMuscleIndex == selectedMuscles.size() - 1) {
                        Toast.makeText(this, "This is the last muscle.", Toast.LENGTH_SHORT).show();
                    } else {
                        storeCurrentMuscleData(); // Store current values before switching
                        currentForceValues.clear(); // Reset for new muscle

                        currentMuscleIndex = (currentMuscleIndex + 1) % selectedMuscles.size();
                        String currentMuscle = selectedMuscles.get(currentMuscleIndex);

                        // Update UI elements
                        selectedMuscleTextView.setText(currentMuscle);
                        onProgressMuscle.setText(String.valueOf(currentMuscleIndex + 1)); // 1-based index
                        totalMuscleSelected.setText(String.valueOf(selectedMuscles.size()));

                        // Dynamically update toggleImage based on the selected muscle
                        updateToggleImage(currentMuscle);

                        Log.d("MuscleSwitch", "Now exercising: " + currentMuscle);
                    }
                }
            } else {
                Toast.makeText(this, "Stop the current exercise before moving to the next muscle.", Toast.LENGTH_SHORT).show();
            }
        });

        backExerciseButton.setOnClickListener(v -> {
            if (!isRunning) {
                if (!selectedMuscles.isEmpty()) {
                    if (currentMuscleIndex == 0) {
                        Toast.makeText(this, "This is the first muscle.", Toast.LENGTH_SHORT).show();
                    } else {
                        storeCurrentMuscleData(); // Store current values before switching
                        currentForceValues.clear(); // Reset for new muscle

                        currentMuscleIndex = (currentMuscleIndex - 1 + selectedMuscles.size()) % selectedMuscles.size();
                        String currentMuscle = selectedMuscles.get(currentMuscleIndex);

                        // Update UI elements
                        selectedMuscleTextView.setText(currentMuscle);
                        onProgressMuscle.setText(String.valueOf(currentMuscleIndex + 1)); // 1-based index
                        totalMuscleSelected.setText(String.valueOf(selectedMuscles.size()));

                        // Dynamically update toggleImage based on the selected muscle
                        updateToggleImage(currentMuscle);

                        Log.d("MuscleSwitch", "Now exercising: " + currentMuscle);
                    }
                }
            } else {
                Toast.makeText(this, "Stop the current exercise before moving to the previous muscle.", Toast.LENGTH_SHORT).show();
            }
        });


        ImageView endActivity = findViewById(R.id.end_activity);
        endActivity.setOnClickListener(v -> {
            if (isMuscleDataValid()) {
                showCustomDialog();
            } else {
                Log.d("EndActivity", "Cannot end activity: Some muscles have no values.");
                Toast.makeText(this, "Cannot end activity: Some muscles have no values or no muscles selected", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateToggleImage(String muscle) {
        int imageResId;
        switch (muscle.toLowerCase()) {
            case "left thigh":
                imageResId = R.drawable.left_thigh_png;
                break;
            case "right thigh":
                imageResId = R.drawable.right_thigh_png;
                break;
            case "left pec":
                imageResId = R.drawable.left_pec_png;
                break;
            case "right pec":
                imageResId = R.drawable.right_pec_png;
                break;
            case "left hamstring":
                imageResId = R.drawable.left_hamstring_png;
                break;
            case "right hamstring":
                imageResId = R.drawable.right_hamstring_png;
                break;
            case "left biceps":
                imageResId = R.drawable.left_biceps_png;
                break;
            case "right biceps":
                imageResId = R.drawable.right_biceps_png;
                break;
            case "left lat":
                imageResId = R.drawable.left_lat_png;
                break;
            case "right lat":
                imageResId = R.drawable.right_lat_png;
                break;
            case "left triceps":
                imageResId = R.drawable.left_triceps_png;
                break;
            case "right triceps":
                imageResId = R.drawable.right_triceps_png;
                break;
            case "left oblique":
                imageResId = R.drawable.left_oblique_png;
                break;
            case "right oblique":
                imageResId = R.drawable.right_oblique_png;
                break;
            case "abs":
                imageResId = R.drawable.abs_png;
                break;
            case "left hip":
                imageResId = R.drawable.left_hip_png;
                break;
            case "right hip":
                imageResId = R.drawable.right_hip_png;
                break;
            case "left trap":
                imageResId = R.drawable.left_trap_png;
                break;
            case "right trap":
                imageResId = R.drawable.right_trap_png;
                break;
            case "left deltoid":
                imageResId = R.drawable.left_deltoid_png;
                break;
            case "right deltoid":
                imageResId = R.drawable.right_deltoid_png;
                break;
            case "left glute":
                imageResId = R.drawable.left_glute_png;
                break;
            case "right glute":
                imageResId = R.drawable.right_glute_png;
                break;
            case "left calf":
                imageResId = R.drawable.left_calf_png;
                break;
            case "right calf":
                imageResId = R.drawable.right_calf_png;
                break;
            default:
                imageResId = R.drawable.thigh_skeleton; // Default model image
                break;
        }

        toggleImage.setImageResource(imageResId);
    }


    private boolean isMuscleDataValid() {
        if (selectedMuscles.isEmpty()) {
            return false; // No muscles selected
        }

        for (String muscle : selectedMuscles) {
            List<Float> values = muscleData.get(muscle);
            if (values == null || values.isEmpty()) {
                return false; // Invalid if any muscle has no values
            }
        }
        return true; // All selected muscles have values
    }

    private void addChip(String text) {
        if (selectedMuscles.contains(text)) {
            return; // Avoid duplicates
        }

        selectedMuscles.add(text);
        updateMuscleTextViews(); // Update TextViews

        // If it's the first muscle, automatically assign it to selectedMuscleTextView
        if (selectedMuscles.size() == 1) {
            currentMuscleIndex = 0;
            selectedMuscleTextView.setText(text);
            onProgressMuscle.setText("1"); // Show first index

            // Update toggleImage based on the first muscle added
            updateToggleImage(text);
        }

        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chipitem, chipGroup, false);
        chip.setText(text);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setClickable(false);

        chip.setOnCloseIconClickListener(v -> removeChip(text));
        chipGroup.addView(chip);
    }


    // Method to remove a chip
    private void removeChip(String text) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.getText().toString().equals(text)) {
                chipGroup.removeView(chip);
                selectedMuscles.remove(text);

                if (currentMuscleIndex >= selectedMuscles.size()) {
                    currentMuscleIndex = 0; // Reset index if out of bounds
                }

                updateMuscleTextViews(); // Update TextViews
                break;
            }
        }
    }

    private void updateMuscleTextViews() {
        totalMuscleSelected.setText(String.valueOf(selectedMuscles.size()));

        if (!selectedMuscles.isEmpty()) {
            onProgressMuscle.setText(String.valueOf(currentMuscleIndex + 1));
        } else {
            onProgressMuscle.setText("0");
        }
    }


    // Method to restore previously selected chips
    private void restoreChips() {
        chipGroup.removeAllViews();
        for (String muscle : selectedMuscles) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chipitem, chipGroup, false);
            chip.setText(muscle);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            chip.setCloseIconVisible(true);
            chip.setCheckable(false);
            chip.setClickable(false);
            chip.setOnCloseIconClickListener(v -> removeChip(muscle));
            chipGroup.addView(chip);
        }

        currentMuscleIndex = 0; // Ensure it starts from the first item
        updateSelectedMuscle(); // Ensure the correct muscle is displayed after restoring
    }


    // Method to update the TextView with the selected muscle
    private void updateSelectedMuscle() {
        if (selectedMuscles.isEmpty()) {
            selectedMuscleTextView.setText("SELECT YOUR MUSCLE");
            currentMuscleIndex = 0; // Reset index when empty
            return;
        }

        if (currentMuscleIndex >= selectedMuscles.size()) {
            currentMuscleIndex = 0; // Reset index if out of bounds
        }

        selectedMuscleTextView.setText(selectedMuscles.get(currentMuscleIndex));
    }


    // Method to set muscle click listeners
    private void setMuscleClickListener(ImageView muscle, String muscleName) {
        muscle.setOnClickListener(v -> addChip(muscleName));
    }

    private void storeCurrentMuscleData() {
        if (!selectedMuscles.isEmpty() && !currentForceValues.isEmpty()) {
            String currentMuscle = selectedMuscles.get(currentMuscleIndex);
            muscleData.put(currentMuscle, new ArrayList<>(currentForceValues)); // Overwrite old values
            Log.d("StoredData", "Saved " + currentMuscle + ": " + muscleData.get(currentMuscle).toString());
        }
    }

    private void showCustomDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_end_activity_dialogue);

        LinearLayout btnEnd = dialog.findViewById(R.id.end_button);
        LinearLayout btnNextRep = dialog.findViewById(R.id.nextRep_button);

        btnEnd.setOnClickListener(v -> {
            // Ensure the last rep is saved
            if (!currentRepData.isEmpty()) {
                Map<String, Object> repEntry = new HashMap<>();
                repEntry.put("rep " + repCount, new HashMap<>(currentRepData));
                allRepsData.add(repEntry);
            }

            // Convert allRepsData to the required format
            Map<String, Map<String, List<Integer>>> repData = new HashMap<>();
            for (int i = 0; i < allRepsData.size(); i++) {
                repData.put("rep " + (i + 1), (Map<String, List<Integer>>) allRepsData.get(i).values().iterator().next());
            }

            // Get current date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());

            // Store session data
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("total_muscles", uniqueMuscles.size());
            sessionData.put("device_name", deviceName);
            sessionData.put("date", currentDate);
            sessionData.put("individual_reps", repData);

            // Add session to the list
            sessionDataList.add(sessionData);

            // Convert to JSON array
            JSONArray sessionDataJsonArray = new JSONArray(sessionDataList);
            Log.d("SessionData", sessionDataJsonArray.toString());

            // Define API URL
//            String url = "https://dynamometer-api.onrender.com/upload-exercise/?email="+email+"&first_name="+firstName+"&last_name"+lastName;
            String url = "https://dynamometer-api.onrender.com/upload-exercise/?email=anirudh20017%40gmail.com&first_name=anirudh&last_name=menon";
            // Send sessionDataJsonArray directly
            JsonArrayRequest jsonSessionUpload = new JsonArrayRequest(
                    Request.Method.POST, url, sessionDataJsonArray,
                    response -> {
                        Log.d("ResponseHere", response.toString());
                        Toast.makeText(DynamoTest.this, "Session Data Uploaded Successfully", Toast.LENGTH_SHORT).show();

                        // Reset session data
                        resetSessionData();

                        // Navigate to Dashboard
                        Intent intent = new Intent(DynamoTest.this, Dashboard.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    },
                    error -> {
                        Log.e("AnirudhTab", error.toString());
                        Toast.makeText(DynamoTest.this, "Error Uploading Data", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            // Add request to Volley queue
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(jsonSessionUpload);

            dialog.dismiss();
        });


        btnNextRep.setOnClickListener(v -> {
            // Store current rep data in common array
            Map<String, Object> repEntry = new HashMap<>();
            repEntry.put("rep " + repCount, new HashMap<>(currentRepData));
            allRepsData.add(repEntry);

            // Clear for the next rep
            currentRepData.clear();
            currentForceValues.clear();
            selectedMuscles.clear(); // Clear selected muscles
            chipGroup.removeAllViews();
            repCount++;
            entries.clear();
            xValue = 0;
            updateLineChart(entries);
            selectedMuscleTextView.setText("SELECT YOUR MUSCLE");
            forceCountNumber.setText("_");
            // Update UI
            repCountTextView.setText(String.format("%02d", repCount));

            Log.d("RepsData", "All Reps: " + new JSONArray(allRepsData).toString());

            dialog.dismiss();
        });


        dialog.show();
    }

    private void resetSessionData() {
        selectedMuscles.clear();
        currentMuscleIndex = 0;
        forceValues.clear();
        sessionDataList.clear();
        muscleData.clear();
        uniqueMuscles.clear();
        currentForceValues.clear();
        repData.clear();
        allRepsData.clear();
        currentRepData.clear();
        repCount = 1;
    }


    private void startProgress() {
        if (selectedMuscles.isEmpty()) {
            Toast.makeText(this, "Please select at least one muscle before starting.", Toast.LENGTH_SHORT).show();
            return;
        }

        isRunning = true;
        startStopButton.setImageResource(R.drawable.baseline_pause_24); // Change to pause icon
        entries.clear();
        xValue = 0;
        updateLineChart(entries); // Refresh chart

        currentForceValues.clear(); // Reset force values for the current session
    }


    private void stopProgress() {
        isRunning = false;
        startStopButton.setImageResource(R.drawable.baseline_play_arrow_24); // Change to play icon

        storeCurrentMuscleData(); // Store final values before stopping

        Log.d("ForceValues", "Stored values: " + new JSONObject(muscleData).toString());

        forceValues.clear(); // Reset for next session
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }


    private void updateLineChart(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, ""); // Empty label (no legend)
        dataSet.setColor(0xFF3CAEF5);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false); // No dots
        dataSet.setDrawValues(false); // No value labels
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(0x803CAEF5);
        dataSet.setFillAlpha(20);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        // Disable description text
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);

        lineChart.getLegend().setEnabled(false);

        // Configure X-axis (Label it as "Time")
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(0xFF000000);
        xAxis.setTextSize(12f);
        xAxis.setLabelCount(5, true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ""; // Hide values on the X-axis
            }
        });

        // Configure Y-axis (Label it as "kgf")
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setAxisMinimum(0f);
        yAxis.setTextColor(0xFF000000);
        yAxis.setTextSize(12f);
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return ""; // Hide values on the Y-axis
            }
        });

        // Hide right Y-axis
        lineChart.getAxisRight().setEnabled(false);

        // Add extra space for axis labels
        lineChart.setExtraBottomOffset(20f); // Space for X-axis label
        lineChart.setExtraLeftOffset(30f); // Space for Y-axis label

        // Set custom marker
        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.custom_marker_view);
        lineChart.setMarker(markerView);

        lineChart.invalidate(); // Refresh the chart
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
                // Apply the condition to floatValue
                float floatValue = bytesToFloat(value);
                if (floatValue <= 1.01972) {
                    floatValue = 0.00f;
                }

                Log.d("karthi", "Received data: " + floatValue);

                float prediction = modelHelper.predict(floatValue);
                Log.d("floatvalues", "Received data: " + prediction);  // Log prediction

                if (isRunning && !selectedMuscles.isEmpty()) {
                    float finalFloatValue = floatValue;
                    runOnUiThread(() -> {
                        // Apply the condition before updating the UI and storing values
                        float finalValue = (prediction/9.8f <= 1.02) ? 0.00f : prediction/9.8f;
                        Log.d("floatvalues", "Received data: " + finalValue);  // Log prediction
                        forceCountNumber.setText(String.format(Locale.US, "%.2f", finalValue));  // Show float with 2 decimal places

                        // Store new force values for the current muscle
                        currentForceValues.add(finalValue);
                        String currentMuscle = selectedMuscles.get(currentMuscleIndex);
                        uniqueMuscles.add(currentMuscle);
                        currentRepData.put(currentMuscle, new ArrayList<>(currentForceValues));

                        // Update UI elements
                        int progress = Math.min(100, Math.max(0, (int) ((finalFloatValue / MAX_PROGRESS) * 100)));
                        circularProgressView.setProgress(progress);
                        entries.add(new Entry(xValue++, finalValue));
                        updateLineChart(entries);
                    });
                }

            }
        }

        private float bytesToFloat(byte[] bytes) {
            try {
                String strValue = new String(bytes).trim();  // Convert bytes to string
                float floatValue = Float.parseFloat(strValue);  // Convert to float
                return floatValue;
            } catch (NumberFormatException e) {
                Log.e("Bluetooth", "Failed to convert data.");
                return -3.0f;
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
        handler.removeCallbacks(progressRunnable);
        disconnectGatt();
    }

    @Override
    public void onBackPressed() {
        // Prevent back navigation by not calling super.onBackPressed()
    }
}
