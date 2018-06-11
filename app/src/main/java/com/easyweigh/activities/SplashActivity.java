package com.easyweigh.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.easyweigh.R;
import com.github.ybq.android.spinkit.style.Wave;

/**
 * Created by Michael on 15/09/2016.
 */
public class SplashActivity extends AppCompatActivity {
    private Wave mWaveDrawable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializer();
    }

    private void initializer() {
        new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //this will be done every 1000 milliseconds ( 1 seconds )
                ImageView imageView = (ImageView) findViewById(R.id.image);
                mWaveDrawable = new Wave();
                mWaveDrawable.setColor(Color.WHITE);
                imageView.setImageDrawable(mWaveDrawable);
                imageView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                mWaveDrawable.start();
            }

            @Override
            public void onFinish() {
                finish();
                Intent login = new Intent(getApplicationContext(), ImportUsersActivity.class);
                startActivity(login);
            }

        }.start();

    }
    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onStop() {
        super.onStop();
        mWaveDrawable.stop();

    }



}
