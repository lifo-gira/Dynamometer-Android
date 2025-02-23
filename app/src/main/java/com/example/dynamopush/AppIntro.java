package com.example.dynamopush;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.airbnb.lottie.LottieAnimationView;

public class AppIntro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_intro);
        hideSystemUI();

        LottieAnimationView animationView = findViewById(R.id.animationView);

        // Handler to wait for 1 second before transition
        new Handler().postDelayed(() -> {
            fadeOutAndStartNewActivity();
        }, 4550);
    }

    private void fadeOutAndStartNewActivity() {
        // Apply fade-out animation
        ConstraintLayout mainLayout = findViewById(R.id.main);
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(300);
        mainLayout.startAnimation(fadeOut);

        // Start new activity after fade-out
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(AppIntro.this, GetStarted.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
