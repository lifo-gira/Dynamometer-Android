package com.example.dynamopush;

import static com.example.dynamopush.Login.googleEmail;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountInfo extends AppCompatActivity {

    LinearLayout to_biodatainfo;
    ImageView back_to_login;
    EditText usernameEditText, emailEditText, passwordEditText,confirmpasswordEditText;
    public static JSONObject userCreation = new JSONObject();
    public static JSONObject userDetails = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_info);
        hideSystemUI();
        userCreation = new JSONObject();
        userDetails = new JSONObject();
        back_to_login = findViewById(R.id.back_to_login);
        back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountInfo.this, Login.class);
                startActivity(intent);
            }
        });

        to_biodatainfo = findViewById(R.id.to_biodatainfo);

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        if(googleEmail!=null){
            emailEditText.setText(googleEmail);
            emailEditText.setInputType(InputType.TYPE_NULL);
            emailEditText.setFocusable(false);
            emailEditText.setClickable(false);

        }
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmpasswordEditText = findViewById(R.id.confirmpasswordEditText);

        to_biodatainfo.setOnClickListener(view -> {
            // Get the input values
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmpasswordEditText.getText().toString().trim();

            // Check if any field is empty
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(AccountInfo.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if passwords match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(AccountInfo.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                userCreation.put("username",username);
                userCreation.put("email",email);
                userCreation.put("password",password);

                userDetails.put("username",username);
                userDetails.put("email",email);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            // Pass the values to the next activity
            Intent intent = new Intent(AccountInfo.this, BiodataInfo.class);
            startActivity(intent);
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
