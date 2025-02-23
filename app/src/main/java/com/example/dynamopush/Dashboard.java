package com.example.dynamopush;

import static com.example.dynamopush.Login.patientDetails;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    private BarChart barChart;
    Button to_report;
    ImageView profileIcon;
    LinearLayout to_bluetooth;
    private CircularProgressView circularProgressView;
    TextView userName, progressText,progress ;
    public String firstName,lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        hideSystemUI();

        // Initialize the TextView for the username
        circularProgressView = findViewById(R.id.circularProgressView);
        progressText = findViewById(R.id.progressText);
        progress = findViewById(R.id.progress);
        // Calculate progress percentage
        // Initialize the LineChart
        userName = findViewById(R.id.userName);

        try {
            if (patientDetails != null) {
                Log.e("API Response", patientDetails.toString()); // Debugging line

                firstName = patientDetails.optString("first_name", "Unknown");
                lastName = patientDetails.optString("last_name", "User");

                userName.setText(firstName + " " + lastName);
            } else {
                Log.e("Error", "patientDetails is null");
            }
        } catch (Exception e) {
            Log.e("JSON Error", "Error parsing JSON", e);
        }

        profileIcon = findViewById(R.id.profileIcon);
        to_bluetooth = findViewById(R.id.to_bluetooth);
        to_report = findViewById(R.id.to_report);
        to_report.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, ReportList.class);
            startActivity(intent);
        });

        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Profile.class);
            startActivity(intent);
        });

        to_bluetooth.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_bluetooth_dialoguebox, null);
            builder.setView(dialogView);

            // Initialize buttons in the dialog
            CardView hand_held = dialogView.findViewById(R.id.hand_held);
            CardView pinch_held = dialogView.findViewById(R.id.pinch_held);
            ImageView close_icon = dialogView.findViewById(R.id.close_icon);

            AlertDialog dialog = builder.create();
            dialog.show();

            // Handle button clicks
            close_icon.setOnClickListener(view -> dialog.dismiss());

            hand_held.setOnClickListener(view -> {
                Intent intent = new Intent(Dashboard.this, Bluetooth.class);
                startActivity(intent);
                dialog.dismiss();
            });

            pinch_held.setOnClickListener(view -> {
                Intent intent = new Intent(Dashboard.this, Bluetooth.class);
                startActivity(intent);
                dialog.dismiss();
            });
        });

        BarChart barChart = findViewById(R.id.bar_chart);
        TextView noDataText = findViewById(R.id.no_data_text);
        TextView progressMadeText = findViewById(R.id.progressmade); // Reference to your TextView

        setupBarChart(barChart, noDataText, progressMadeText, circularProgressView, progressText);

    }

    public void setupBarChart(BarChart barChart, TextView noDataText, TextView progressMadeText, CircularProgressView circularProgressView, TextView progressText) {
        try {
            JSONArray exerciseRecord = patientDetails.getJSONArray("exerciseRecord");

            if (exerciseRecord.length() == 0) {
                barChart.setVisibility(View.GONE);
                noDataText.setVisibility(View.VISIBLE);
                progressMadeText.setText("No progress yet");
                progressMadeText.setTextColor(Color.GRAY);
                circularProgressView.setProgress(0);
                progressText.setText("0%"); // Reflect progress in TextView
                return;
            }

            LinkedHashMap<String, Integer> muscleData = new LinkedHashMap<>();

            // Extract date-wise total_muscles
            for (int i = 0; i < exerciseRecord.length(); i++) {
                JSONObject record = exerciseRecord.getJSONObject(i);
                String date = record.getString("date");
                int muscles = record.getInt("total_muscles");

                // Sum muscles if multiple records exist for the same date
                muscleData.put(date, muscleData.getOrDefault(date, 0) + muscles);
            }

            // Prepare Bar Entries
            List<BarEntry> entries = new ArrayList<>();
            List<String> dates = new ArrayList<>();
            int index = 0;
            Integer previousMuscles = null;
            int progressPercentage = 0;

            for (String date : muscleData.keySet()) {
                int currentMuscles = muscleData.get(date);
                entries.add(new BarEntry(index, currentMuscles));
                dates.add(date);

                // Calculate progress percentage (from previous test)
                if (previousMuscles != null && previousMuscles > 0) {
                    progressPercentage = ((currentMuscles - previousMuscles) * 100) / previousMuscles;
                }

                // Ensure progress percentage stays within ±30%
                progressPercentage = Math.max(-30, Math.min(30, progressPercentage));

                previousMuscles = currentMuscles;
                index++;
            }

            // Update progressMadeText based on progress
            if (progressPercentage > 0) {
                progressMadeText.setText("+" + progressPercentage + "%");
                progressMadeText.setTextColor(Color.parseColor("#5FB70B")); // Green for positive
            } else if (progressPercentage < 0) {
                progressMadeText.setText(progressPercentage + "%");
                progressMadeText.setTextColor(Color.RED); // Red for negative
            } else {
                progressMadeText.setText("0%");
                progressMadeText.setTextColor(Color.GRAY); // Gray for no change
            }

            // Ensure circular progress is never negative
            int circularProgress = Math.max(0, progressPercentage);

            // Update Circular Progress View
            circularProgressView.setProgress(circularProgress);

            // **Update progressText to reflect the progress**
            progressText.setText(circularProgress + "%");
            progress.setText(circularProgress + "%");
            // Hide noDataText and show chart
            noDataText.setVisibility(View.GONE);
            barChart.setVisibility(View.VISIBLE);

            // Create DataSet
            BarDataSet dataSet = new BarDataSet(entries, "Muscles per Day");
            dataSet.setColor(0xFF70DBB9);  // Green Color
            dataSet.setValueTextSize(12f);
            barChart.getDescription().setEnabled(false);

            // Create BarData
            BarData barData = new BarData(dataSet);
            barChart.setData(barData);

            // Configure X-Axis for Dates
            XAxis xAxis = barChart.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(true);

            // Refresh Chart
            barChart.invalidate();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




    private void hideSystemUI() {
        // Hide the status bar and navigation bar
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
