package com.example.dynamopush;

import static com.example.dynamopush.Login.patientDetails;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReportList extends AppCompatActivity {

    ImageView backButton, calendarButton,resetButton ;
    private RecyclerView recyclerView;
    private ReportListAdapter reportListAdapter;
    private List<ReportListClass> reportList;
    private RequestQueue queue;
    private String email;
    private List<ReportListClass> originalList = new ArrayList<>();
    private List<ReportListClass> filteredList = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_list);
        hideSystemUI();

        recyclerView = findViewById(R.id.testReports);
        calendarButton = findViewById(R.id.calendarButton);
        resetButton = findViewById(R.id.resetButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        try {
            email = patientDetails.getString("email");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        reportList = new ArrayList<>();

        // Initialize Volley RequestQueue
        queue = Volley.newRequestQueue(this);
        fetchPatientData();

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReportList.this, Dashboard.class);
                startActivity(intent);
            }
        });

        calendarButton.setOnClickListener(v -> showDatePickerDialog());
        resetButton.setOnClickListener(v -> resetList());

    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedYear + "-" + String.format("%02d", selectedMonth + 1) + "-" + String.format("%02d", selectedDay);
            filterListByDate(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    // To filter by a specific date
    private void filterListByDate(String selectedDate) {
        List<ReportListClass> filteredList = new ArrayList<>();
        for (ReportListClass report : originalList) {
            try {
                String recordDate = report.getRecord().getString("date");
                if (recordDate.equals(selectedDate)) {
                    filteredList.add(report);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        reportListAdapter.updateList(filteredList);
    }

    // To reset the list
    private void resetList() {
        reportListAdapter.updateList(originalList);
    }


    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void fetchPatientData() {
        String url = "https://dynamometer-api.onrender.com/patient-data?email=" + email;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (!response.isEmpty()) {
                                JSONObject patientDetails = new JSONObject(response);
                                Toast.makeText(ReportList.this, "Data Fetched Successfully", Toast.LENGTH_SHORT).show();
                                parsePatientData(patientDetails);
                            } else {
                                Log.w("API_RESPONSE", "Empty response received");
                                Toast.makeText(ReportList.this, "No Data Found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("API_RESPONSE", "Error parsing response: " + e.getMessage());
                            Toast.makeText(ReportList.this, "Invalid response format", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            Log.e("API_RESPONSE", "Email not found (404)");
                            Toast.makeText(ReportList.this, "Email not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("API_RESPONSE", "Volley Error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"));
                            Toast.makeText(ReportList.this, "Error: " + (error.getMessage() != null ? error.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        queue.add(stringRequest);
    }

    private void parsePatientData(JSONObject patientDetails) {
        try {
            if (patientDetails.has("exerciseRecord")) {
                JSONArray exerciseRecordArray = patientDetails.getJSONArray("exerciseRecord");

                Log.d("PatientDetails", "Exercise Record: " + exerciseRecordArray.toString());

                originalList.clear();  // Clear old data
                reportList.clear();

                for (int i = 0; i < exerciseRecordArray.length(); i++) {
                    JSONObject record = exerciseRecordArray.getJSONObject(i);
                    int totalMuscles = record.getInt("total_muscles");
                    String deviceName = record.getString("device_name");
                    String date = record.getString("date");
                    JSONObject individualReps = record.getJSONObject("individual_reps");

                    int repCount = individualReps.length();
                    int percentage = (int) ((totalMuscles / 25.0) * 100);

                    ReportListClass reportItem = new ReportListClass(String.valueOf(i + 1),
                            String.valueOf(repCount),
                            percentage,
                            record);
                    originalList.add(reportItem);
                }

                reportList.addAll(originalList); // Copy to the displayed list

                // Set up Adapter for RecyclerView
                reportListAdapter = new ReportListAdapter(reportList);
                recyclerView.setAdapter(reportListAdapter);

            } else {
                Log.w("API_RESPONSE", "No exerciseRecord found in response");
                Toast.makeText(this, "No exercise record available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("API_RESPONSE", "Error parsing exerciseRecord: " + e.getMessage());
            Toast.makeText(this, "Error parsing exercise data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent back navigation by not calling super.onBackPressed()
    }

}
