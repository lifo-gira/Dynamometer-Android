package com.example.dynamopush;

import static com.example.dynamopush.AccountInfo.userCreation;
import static com.example.dynamopush.AccountInfo.userDetails;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class BiodataInfo extends AppCompatActivity {

    Spinner bloodGroupSpinner,genderSpinner;
    public static String bloodgroup,gender;
    Calendar calendar;
    ImageView back_to_login;
    LinearLayout to_dashboard;
    EditText firstnameEditText, lastnameEditText, heightEditText,weightEditText,dob_calendar,phoneEditText,addressEditText;
    public static String dateofbirth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_biodata_info);
        hideSystemUI();

        back_to_login = findViewById(R.id.back_to_login);
        back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BiodataInfo.this, Login.class);
                startActivity(intent);
            }
        });

        to_dashboard = findViewById(R.id.to_dashboard);

        bloodGroupSpinner = findViewById(R.id.bloodGroupSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.blood_group_items,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        bloodGroupSpinner.setAdapter(adapter);

        bloodGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bloodgroup= adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        genderSpinner = findViewById(R.id.genderSpinner);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_items,
                R.layout.spinner_item
        );
        genderAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                gender= adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dob_calendar = findViewById(R.id.dob_calendar);

        dob_calendar.setOnClickListener(v -> {
            calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    R.style.CustomDatePickerDialog,
                    (view1, year1, month1, dayOfMonth) -> {
                        String date = String.format("%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
                        dob_calendar.setText(date);
                        dateofbirth = date;
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        firstnameEditText = findViewById(R.id.firstnameEditText);
        lastnameEditText = findViewById(R.id.lastnameEditText);
        heightEditText = findViewById(R.id.heightEditText);
        weightEditText = findViewById(R.id.weightEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);

        to_dashboard.setOnClickListener(view -> {

//            Intent intent = new Intent(BiodataInfo.this, Login.class);
//            startActivity(intent);
            // Get the input values
            // Get the input values
            String firstname = firstnameEditText.getText().toString().trim();
            String lastname = lastnameEditText.getText().toString().trim();
            String heightStr = heightEditText.getText().toString().trim();
            String weightStr = weightEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();

// Check if any field is empty
            if (firstname.isEmpty() || lastname.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() ||
                    phone.isEmpty() || address.isEmpty() || dateofbirth.isEmpty() || gender.isEmpty() || bloodgroup.isEmpty()) {
                Toast.makeText(BiodataInfo.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

// Convert height and weight to integers
            int height = 0, weight = 0;
            try {
                height = Integer.parseInt(heightStr);
                weight = Integer.parseInt(weightStr);
            } catch (NumberFormatException e) {
                Toast.makeText(BiodataInfo.this, "Invalid height or weight", Toast.LENGTH_SHORT).show();
                return;
            }


            try {
                userDetails.put("user_id", firstname + " " + lastname);
                userDetails.put("first_name", firstname);
                userDetails.put("last_name", lastname);
                userDetails.put("dob", dateofbirth);
                userDetails.put("blood_grp", bloodgroup);
                userDetails.put("flag", 1);
                userDetails.put("height", height);
                userDetails.put("weight", weight);
                userDetails.put("gender", gender);
                userDetails.put("phone_number", phone);
                userDetails.put("address", address);
                userDetails.put("exerciseRecord", new JSONArray());
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            Log.e("User Details", userCreation.toString());

            // Create POST request using Volley
            JsonObjectRequest jsonUserCreation = new JsonObjectRequest(
                    Request.Method.POST, "https://dynamometer-api.onrender.com/register", userCreation,
                    response -> {
                        // Handle the API Response
                        Log.e("Response", response.toString());
                        Toast.makeText(BiodataInfo.this, "User Created Successfully", Toast.LENGTH_SHORT).show();
                    },
                    error -> {
                        // Handle Errors
                        Log.e("Error", error.toString());
                        if (error instanceof com.android.volley.TimeoutError) {
                            Toast.makeText(BiodataInfo.this, "Request Timeout. Try Again.", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof com.android.volley.NoConnectionError) {
                            Toast.makeText(BiodataInfo.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BiodataInfo.this, "Error: " + "Email already registered with a patient", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    // Add headers if required (e.g., Authorization token)
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json"); // JSON Content-Type
                    return headers;
                }
            };


            JsonObjectRequest jsonUserDetails = new JsonObjectRequest(
                    Request.Method.POST, "https://dynamometer-api.onrender.com/patient-data", userDetails,
                    response -> {
                        // Handle the API Response
                        Log.e("Response", response.toString());
                        Toast.makeText(BiodataInfo.this, "Data Saved Successfully", Toast.LENGTH_SHORT).show();

                        // Navigate to next activity if needed
                        Intent intent = new Intent(BiodataInfo.this, Login.class);
                        startActivity(intent);
                    },
                    error -> {
                        // Handle Errors
                        Log.e("Error", error.toString());
                        if (error instanceof com.android.volley.TimeoutError) {
                            Toast.makeText(BiodataInfo.this, "Request Timeout. Try Again.", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof com.android.volley.NoConnectionError) {
                            Toast.makeText(BiodataInfo.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    // Add headers if required (e.g., Authorization token)
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json"); // JSON Content-Type
                    return headers;
                }
            };

            // Add request to Volley RequestQueue
            RequestQueue requestQueue = Volley.newRequestQueue(BiodataInfo.this);
            requestQueue.add(jsonUserCreation);
            requestQueue.add(jsonUserDetails);
        });


    }

    private void hideSystemUI() {
        // Hide the status bar and navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }

    @Override
    public void onBackPressed() {
        // Prevent back navigation by not calling super.onBackPressed()
    }
}