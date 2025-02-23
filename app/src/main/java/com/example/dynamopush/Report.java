package com.example.dynamopush;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Report extends AppCompatActivity {

    ImageView backButton,toggleImage;
    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private List<ReportClass> reportList;
    private Spinner repSpinner; // Declare Spinner
    TextView testCount,dateOfReport,nameOfDevice;
    public static JSONObject storedRecord;
    String dateDone, deviceName,testCountText,recordString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);
        hideSystemUI();

        testCount = findViewById(R.id.testCount);
        dateOfReport = findViewById(R.id.dateOfReport);
        nameOfDevice = findViewById(R.id.deviceName);
        repSpinner = findViewById(R.id.repSpinner);
        Intent intent = getIntent();
        recordString = intent.getStringExtra("record");

        try {
            storedRecord = new JSONObject(recordString);
            Log.d("Recorded_Data", "Parsed Record: " + storedRecord.toString());

            setupSpinner(); // Dynamically set up the spinner with data

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Record Parsing Error", "Error parsing record: " + e.getMessage());
        }

        testCountText = intent.getStringExtra("testCount");
        try {
            testCount.setText("TEST "+testCountText);
            dateDone = storedRecord.getString("date");
            dateOfReport.setText(dateDone);
            deviceName = storedRecord.getString("device_name");
            nameOfDevice.setText(deviceName);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
// Initialize RecyclerView
        recyclerView = findViewById(R.id.testReports);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { // Detect when scrolling stops
                    updateMuscleFromRecyclerView();
                }
            }
        });

// Ensure first item is detected immediately after RecyclerView is set up
        recyclerView.postDelayed(() -> updateMuscleFromRecyclerView(), 200);



// Attach PagerSnapHelper to ensure only one item is fully visible at a time
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

// Initialize empty list and adapter
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(reportList, this);
        recyclerView.setAdapter(reportAdapter);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Report.this, ReportList.class);
                startActivity(intent);
                finish();;
            }
        });

    }

    private void updateMuscleFromRecyclerView() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition(); // Get first visible item

        if (position == RecyclerView.NO_POSITION && reportList.size() > 0) {
            position = 0; // Default to first item if none is visible
        }

        if (position != RecyclerView.NO_POSITION) {
            ReportClass report = reportList.get(position);
            String muscleName = report.getMuscleName(); // Get the muscle name

            // Fetch average force from predefined values
            Map<String, Double> averageForces = getAverageForceValues();
            double avgForce = averageForces.getOrDefault(muscleName, 0.0); // Default to 0.0 if not found

            Log.e("musclenamehere", muscleName);
            Log.e("avgForce", String.valueOf(avgForce)); // Debug log

            final double avgForceFinal = avgForce; // Ensure it's effectively final

            runOnUiThread(() -> {
                resetMuscleVisibility();  // Hide all muscles first
                updateMuscleImage(muscleName, (float) avgForceFinal); // Use average force

                // Update average_force dynamically
                TextView avgForceText = findViewById(R.id.average_force);
                avgForceText.setText(String.format(Locale.US, "Avg Force - %.2f Kgf", avgForceFinal));
            });
        }
    }



    private void setupSpinner() {
        List<String> repOptions = new ArrayList<>();

        try {
            JSONObject individualReps = storedRecord.getJSONObject("individual_reps");

            // Extract reps dynamically from JSON keys (Only "rep 1", "rep 2", etc.)
            Iterator<String> keys = individualReps.keys();
            while (keys.hasNext()) {
                repOptions.add(keys.next());
            }

            // Sort list to ensure order "rep 1", "rep 2", "rep 3", etc.
            Collections.sort(repOptions, new Comparator<String>() {
                @Override
                public int compare(String rep1, String rep2) {
                    int num1 = Integer.parseInt(rep1.replaceAll("\\D", ""));
                    int num2 = Integer.parseInt(rep2.replaceAll("\\D", ""));
                    return Integer.compare(num1, num2);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create an ArrayAdapter for the spinner
        ArrayAdapter<String> repAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, repOptions);
        repAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repSpinner.setAdapter(repAdapter);

        repSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedRep = adapterView.getItemAtPosition(i).toString();

                // Temporarily remove the scroll listener to avoid unwanted interference
                recyclerView.clearOnScrollListeners();

                // Clear and update dataset in a controlled way
                reportList.clear();
                reportAdapter.notifyDataSetChanged(); // Notify adapter before adding new data

                logMusclesForRep(selectedRep); // This updates reportList

                displayAverageForceValues();

                // Ensure UI updates before setting position
                recyclerView.post(() -> {
                    recyclerView.scrollToPosition(0);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    layoutManager.scrollToPositionWithOffset(0, 0);
                });

                // Small delay to prevent UI glitches
                recyclerView.postDelayed(() -> updateMuscleFromRecyclerView(), 50);

                // Reattach scroll listener after ensuring position
                recyclerView.postDelayed(() -> {
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                updateMuscleFromRecyclerView();
                            }
                        }
                    });
                }, 100);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No action needed
            }
        });

    }

    private void displayAverageForceValues() {
        Map<String, Double> averageForces = getAverageForceValues();

        for (String muscle : averageForces.keySet()) {
            System.out.println(muscle + ": " + averageForces.get(muscle) + " kgf");
        }
    }

    private Map<String, Double> getAverageForceValues() {
        Map<String, Double> averageForces = new HashMap<>();

        // Front Model Muscles
        averageForces.put("Left Thigh", 30.0);
        averageForces.put("Right Thigh", 30.0);
        averageForces.put("Right Biceps", 18.0);
        averageForces.put("Left Biceps", 18.0);
        averageForces.put("Left Pec", 22.0);
        averageForces.put("Right Pec", 22.0);
        averageForces.put("Left Oblique", 15.0);
        averageForces.put("Right Oblique", 15.0);
        averageForces.put("Abs", 20.0);
        averageForces.put("Left Hip", 25.0);
        averageForces.put("Right Hip", 25.0);

        // Back Model Muscles
        averageForces.put("Left Lat", 28.0);
        averageForces.put("Right Lat", 28.0);
        averageForces.put("Left Triceps", 14.0);
        averageForces.put("Right Triceps", 14.0);
        averageForces.put("Left Hamstring", 36.0);
        averageForces.put("Right Hamstring", 36.0);
        averageForces.put("Left Trap", 26.0);
        averageForces.put("Right Trap", 26.0);
        averageForces.put("Left Deltoid", 20.0);
        averageForces.put("Right Deltoid", 20.0);
        averageForces.put("Left Glute", 40.0);
        averageForces.put("Right Glute", 40.0);
        averageForces.put("Left Calf", 24.0);
        averageForces.put("Right Calf", 24.0);

        return averageForces;
    }



    private void logMusclesForRep(String repKey) {
        try {
            JSONObject individualReps = storedRecord.getJSONObject("individual_reps");
            JSONObject selectedRep = individualReps.getJSONObject(repKey);

            List<ReportClass> updatedReportList = new ArrayList<>();
            Iterator<String> muscleKeys = selectedRep.keys();

            while (muscleKeys.hasNext()) {
                String muscle = muscleKeys.next();
                JSONArray values = selectedRep.getJSONArray(muscle);

                // Convert JSON values into a List<Float> and find max value
                List<Float> forceValues = new ArrayList<>();
                float maxForce = Float.MIN_VALUE;

                for (int i = 0; i < values.length(); i++) {
                    float value = (float) values.getDouble(i);
                    forceValues.add(value);
                    if (value > maxForce) {
                        maxForce = value;
                    }
                }

                updatedReportList.add(new ReportClass(
                        String.format(Locale.US, "%.2f Kgf", maxForce),
                        muscle,
                        forceValues
                ));


                final float maxForceFinal = maxForce;
                runOnUiThread(() -> {
                    resetMuscleVisibility();  // Hide all muscle images first
                    updateMuscleImage(muscle, maxForceFinal); // Pass final variable
                });

            }

            // Update RecyclerView with new data
            updateRecyclerView(updatedReportList);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Muscle Display Error", "Error displaying muscle data: " + e.getMessage());
        }
    }


    private void updateMuscleImage(String muscle, float maxForce) {
        int muscleViewId = getMuscleImageViewId(muscle);

        if (muscleViewId == -1) {
            Log.e("UpdateMuscleImage", "Invalid muscle name: " + muscle);
            return;
        }

        ImageView muscleView = findViewById(muscleViewId);
        Log.e("Muscleviewhere", "Muscle: " + muscle + ", View: " + muscleView);

        if (muscleView != null) {
            muscleView.setVisibility(View.VISIBLE); // Show only the current muscle

            // Show front or back model based on muscle type
            if (isFrontMuscle(muscle)) {
                findViewById(R.id.front_model).setVisibility(View.VISIBLE);
                findViewById(R.id.back_model).setVisibility(View.GONE);
            } else {
                findViewById(R.id.back_model).setVisibility(View.VISIBLE);
                findViewById(R.id.front_model).setVisibility(View.GONE);
            }

            // Apply heatmap color based on maxForce
            int color = getHeatmapColor(maxForce);
            Drawable drawable = muscleView.getDrawable();
            if (drawable instanceof VectorDrawableCompat || drawable instanceof VectorDrawable) {
                muscleView.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        } else {
            Log.e("UpdateMuscleImage", "ImageView not found for muscle: " + muscle);
        }
    }

    private int getHeatmapColor(float maxForce) {
        // Define force thresholds (adjust as needed)
        float minForce = 0;
        float maxForceThreshold = 11.1f; // Adjust based on your max values

        float normalizedForce = (maxForce - minForce) / (maxForceThreshold - minForce);
        normalizedForce = Math.max(0, Math.min(1, normalizedForce)); // Keep within 0-1 range

        // Heatmap color gradient (Bottom to Top)
        int color1 = Color.parseColor("#6348FF"); // Bottom
        int color2 = Color.parseColor("#48FFF6");
        int color3 = Color.parseColor("#48FF70");
        int color4 = Color.parseColor("#D4FF48");
        int color5 = Color.parseColor("#FFAA48");
        int color6 = Color.parseColor("#FF4848"); // Top

        if (normalizedForce <= 0.2) {
            return interpolateColor(color1, color2, normalizedForce / 0.2f);
        } else if (normalizedForce <= 0.4) {
            return interpolateColor(color2, color3, (normalizedForce - 0.2f) / 0.2f);
        } else if (normalizedForce <= 0.6) {
            return interpolateColor(color3, color4, (normalizedForce - 0.4f) / 0.2f);
        } else if (normalizedForce <= 0.8) {
            return interpolateColor(color4, color5, (normalizedForce - 0.6f) / 0.2f);
        } else {
            return interpolateColor(color5, color6, (normalizedForce - 0.8f) / 0.2f);
        }
    }
    
    // Helper function to interpolate between two colors
    private int interpolateColor(int color1, int color2, float fraction) {
        int r1 = Color.red(color1), g1 = Color.green(color1), b1 = Color.blue(color1);
        int r2 = Color.red(color2), g2 = Color.green(color2), b2 = Color.blue(color2);

        int r = (int) (r1 + (r2 - r1) * fraction);
        int g = (int) (g1 + (g2 - g1) * fraction);
        int b = (int) (b1 + (b2 - b1) * fraction);

        return Color.rgb(r, g, b);
    }


    private void resetMuscleVisibility() {
        int[] allMuscles = {
                // Front Muscles
                R.id.left_pec, R.id.right_pec,
                R.id.left_biceps, R.id.right_biceps,
                R.id.left_thigh, R.id.right_thigh,
                R.id.left_obliques, R.id.right_obliques,
                R.id.abs,
                R.id.left_hip, R.id.right_hip,

                // Back Muscles
                R.id.left_lat, R.id.right_lat,
                R.id.left_triceps, R.id.right_triceps,
                R.id.left_hamstring, R.id.right_hamstring,
                R.id.left_traps, R.id.right_traps,
                R.id.left_deltoids, R.id.right_deltoids,
                R.id.left_glutes, R.id.right_glutes,
                R.id.left_calves, R.id.right_calves
        };


        // Hide all muscles
        for (int id : allMuscles) {
            findViewById(id).setVisibility(View.GONE);
        }

        // Hide both FrameLayouts
        findViewById(R.id.front_model).setVisibility(View.GONE);
        findViewById(R.id.back_model).setVisibility(View.GONE);
    }


    private int getMuscleImageViewId(String muscleName) {
        switch (muscleName) {
            // Front Muscles
            case "Left Pec": return R.id.left_pec;
            case "Right Pec": return R.id.right_pec;
            case "Left Biceps": return R.id.left_biceps;
            case "Right Biceps": return R.id.right_biceps;
            case "Left Thigh": return R.id.left_thigh;
            case "Right Thigh": return R.id.right_thigh;
            case "Left Oblique": return R.id.left_obliques;
            case "Right Oblique": return R.id.right_obliques;
            case "Abs": return R.id.abs;
            case "Left Hip": return R.id.right_hip;
            case "Right Hip": return R.id.left_hip;

            // Back Muscles
            case "Left Lat": return R.id.left_lat;
            case "Right Lat": return R.id.right_lat;
            case "Left Triceps": return R.id.left_triceps;
            case "Right Triceps": return R.id.right_triceps;
            case "Left Hamstring": return R.id.left_hamstring;
            case "Right Hamstring": return R.id.right_hamstring;
            case "Left Trap": return R.id.left_traps;
            case "Right Trap": return R.id.right_traps;
            case "Left Deltoid": return R.id.left_deltoids;
            case "Right Deltoid": return R.id.right_deltoids;
            case "Left Glute": return R.id.left_glutes;
            case "Right Glute": return R.id.right_glutes;
            case "Left Calf": return R.id.left_calves;
            case "Right Calf": return R.id.right_calves;

            default: return -1; // Muscle not found
        }
    }


    private boolean isFrontMuscle(String muscleName) {
        return muscleName.equals("Left Pec") || muscleName.equals("Right Pec") ||
                muscleName.equals("Left Biceps") || muscleName.equals("Right Biceps") ||
                muscleName.equals("Left Thigh") || muscleName.equals("Right Thigh") ||
                muscleName.equals("Left Oblique") || muscleName.equals("Right Oblique") ||
                muscleName.equals("Abs") ||
                muscleName.equals("Left Hip") || muscleName.equals("Right Hip");
    }



    private void updateRecyclerView(List<ReportClass> newReportList) {
        reportList.clear();
        reportList.addAll(newReportList);
        reportAdapter.notifyDataSetChanged();
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onBackPressed() {
        // Prevent back navigation by not calling super.onBackPressed()
    }
}
