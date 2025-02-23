package com.example.dynamopush;

import static com.example.dynamopush.Login.patientDetails;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Profile extends AppCompatActivity {

    ImageView log_out,backButton;
    TextView userName,height,weight,age;
    public String firstName,lastName,userAge,userWeight,userHeight;
    LinearLayout check_report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        userName = findViewById(R.id.userName);
        height = findViewById(R.id.height);
        weight = findViewById(R.id.weight);
        age = findViewById(R.id.age);
        check_report = findViewById(R.id.check_report);

        try {
            firstName = patientDetails.getString("first_name");
            lastName = patientDetails.getString("last_name");
            userName.setText(firstName + " " + lastName);
            userAge = patientDetails.getString("dob"); // "07/12/2001"
            int ageValue = calculateAge(userAge);
            age.setText(String.valueOf(ageValue)); // Convert int to String before setting
            userWeight = patientDetails.getString("weight");
            weight.setText(userWeight);
            userHeight = patientDetails.getString("height");
            height.setText(userHeight);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        log_out = findViewById(R.id.log_out);

        log_out.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Ensure Profile activity is also closed
        });

        check_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, ReportList.class);
                startActivity(intent);
            }
        });


        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, Dashboard.class);
                startActivity(intent);
            }
        });

    }

    private int calculateAge(String dob) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date birthDate = sdf.parse(dob);
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);

            // Adjust age if birthday hasn't occurred this year yet
            if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0; // Return 0 if parsing fails
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent back navigation by not calling super.onBackPressed()
    }

}