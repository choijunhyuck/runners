package com.runners.choi.runners.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.runners.choi.runners.MainActivity;
import com.runners.choi.runners.R;
import com.runners.choi.runners.app.AppController;
import com.runners.choi.runners.app.AppLinks;

import org.json.JSONObject;

import java.util.Calendar;

public class SettingOneAcitivity extends AppCompatActivity {

    private SharedPreferences mPref;

    private RelativeLayout background;

    private SeekBar difficultySeekBar;
    private int seekbarGauge;

    private int difficulty;
    private String uid;

    private ImageView idSettingButton;
    private ImageView settingEndButton;

    public static SettingOneAcitivity activitySetting = null;

    private String TAG = "TAG.SO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setting_first);

        activitySetting = this;

        //MY INFORMATION
        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        //DAY & NIGHT SETTING
        Calendar calendar = Calendar.getInstance( );
        int time = calendar.get((Calendar.HOUR_OF_DAY));

        background = (RelativeLayout)
                findViewById(R.id.setting_1_background);
        //NIGHT, NOON, MORNING
        if(time < 7 || time > 18 ){
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_night));
        } else if(time > 12) {
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_noon));
        } else{
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen));
        }


        //DIFFICULTY && SEEKBAR SET
        Intent intent = getIntent();
        difficulty = intent.getIntExtra("difficulty", 0);
        uid = intent.getStringExtra("uid");

        difficultySeekBar = (SeekBar)
                findViewById(R.id.difficulty_seekbar);

        switch (difficulty){

            case 1:
                difficultySeekBar.setProgress(0);
                break;

            case 2:

                difficultySeekBar.setProgress(50);
                break;
            case 3:

                difficultySeekBar.setProgress(100);
                break;
        }
        difficultySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                 seekbarGauge = i;
                 Log.d(TAG, "seekbar "+String.valueOf(seekbarGauge));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                    if (seekbarGauge < 33) {
                        seekBar.setProgress(0);
                        if (difficulty != 1) {
                            changeDiff(1);
                            difficulty = 1;
                        }
                    } else if (seekbarGauge > 63) {
                        seekBar.setProgress(100);
                        if (difficulty != 3) {
                            changeDiff(3);
                            difficulty = 3;
                        }
                    } else {
                        seekBar.setProgress(50);
                        if (difficulty != 2) {
                            changeDiff(2);
                            difficulty = 2;
                        }
                    }

            }
        });


        //BUTTONS SET

        idSettingButton = (ImageView)
                findViewById(R.id.id_setting_button);
        settingEndButton = (ImageView)
                findViewById(R.id.setting_end_button_first);

        idSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingOneAcitivity.this, SettingTwoAcitivity.class));
            }
        });

        settingEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingOneAcitivity.this, MainActivity.class));
                finish();
            }
        });
    }

    public void changeDiff(final int difficulty){

        if(isInternetAvailable()) {

            String url = AppLinks.URL_CAHNGE_USER_DIFF + uid + "&difficulty=" + String.valueOf(difficulty);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt("difficulty", difficulty);
                    editor.commit();

                    Log.d(TAG, response.toString());
                    Log.d(TAG, "SUCCESS : INTERNET CONNECTION");

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq);

        } else {
            SharedPreferences.Editor editor = mPref.edit();
            editor.putInt("difficulty", difficulty);
            editor.commit();

            Log.d("TAG", "SUCCESS : NO INTERNET CONNECTION , DIFF = "+difficulty);
        }

        Toast.makeText(SettingOneAcitivity.this, "난이도 적용됨", Toast.LENGTH_LONG).show();

    }

    public Boolean isInternetAvailable() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(SettingOneAcitivity.this, MainActivity.class));
        finish();

    }

}
