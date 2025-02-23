package com.example.dynamopush;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    LinearLayout loginButton;
    TextView signUpButton;
    EditText emailEditText, passwordEditText;
    ImageView google_login;
    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    public static String usermail,googleEmail;
    public static JSONObject patientDetails = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        hideSystemUI();

        signUpButton = findViewById(R.id.signUpButton);
        loginButton = findViewById(R.id.loginButton);

        passwordEditText = findViewById(R.id.passwordEditText);
        emailEditText = findViewById(R.id.emailEditText);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, AccountInfo.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    loginButton.setClickable(false);
//                Intent intent = new Intent(Login.this, Dashboard.class);
//                startActivity(intent);
//                finish();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject loginData = new JSONObject();
                    loginData.put("email", email);
                    loginData.put("password", password);

                    JsonObjectRequest loginRequest = new JsonObjectRequest(
                            Request.Method.POST, "https://dynamometer-api.onrender.com/login", loginData,
                            response -> {
                                try {
                                    if (response.has("message") && response.getString("message").equals("Login successful")) {
                                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        Log.e("mymail",email);
                                        loginmail(email);
//                                        // Navigate to Dashboard
//                                        Intent intent = new Intent(Login.this, Dashboard.class);
//                                        usermail = email;
//                                        startActivity(intent);
//                                        finish();
                                    } else {
                                        Toast.makeText(Login.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(Login.this, "Response Error", Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> {
                                Log.e("LoginError", error.toString());
                                Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json"); // JSON Content-Type
                            return headers;
                        }
                    };


                    // Add request to Volley queue
                    Volley.newRequestQueue(Login.this).add(loginRequest);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Login.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loginButton.setClickable(true);
                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        google_login = findViewById(R.id.google_login);
        google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                    // After signing out, start the sign-in intent
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, fetch the email
            String email = account.getEmail();
            //Toast.makeText(this, "Email: " + email, Toast.LENGTH_SHORT).show();
            Log.d("GoogleSignIn", "Email: " + email);
            if(email!=null){
                usermail = email;
                loginmail(email);
                //startActivity(new Intent(LoginActivity.this,UserSelection.class));
            }

        } catch (ApiException e) {
            Log.w("GoogleSignIn", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void loginmail(String email) {
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.e("Myemail",email);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "https://dynamometer-api.onrender.com/patient-data?email=" + email,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Check if response is not empty (valid JSON object)
                            if (!response.isEmpty()) {
                                Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();

                                usermail = email;
                                patientDetails = new JSONObject(response);
                                Log.e("DetailsHere", String.valueOf(patientDetails));
                                // Navigate to Dashboard
                                Intent intent = new Intent(Login.this, Dashboard.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Login.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(Login.this, "Invalid response format", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loginButton.setEnabled(true);

                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            Toast.makeText(Login.this, "Email not found", Toast.LENGTH_SHORT).show();
                            googleEmail = email;
                            Intent intent = new Intent(Login.this, AccountInfo.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(Login.this, "Error: " + (error.getMessage() != null ? error.getMessage() : "Server Closed"), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        queue.add(stringRequest);
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