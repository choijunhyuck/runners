package com.runners.choi.runners.record;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.runners.choi.runners.MainActivity;
import com.runners.choi.runners.R;


public class ResultDetailActivity extends AppCompatActivity {

    private SharedPreferences rPref;

    private RelativeLayout backgroundClicker;

    private TextView repeatView;
    private TextView pointView;
    private TextView timeView;
    private TextView bonusView;
    private TextView speedView;
    private TextView distanceView;
    private TextView nextTicketView;
    private TextView goMainView;

    private String repeat;
    private String pointAmount;
    private String timeAmount;
    private String distanceAmount;
    private String bonusTimeAmount;
    private String speedMax;
    private String date;

    private String uid;
    private int difficulty;
    private int ticket;

    private final Handler delayHandler = new Handler();

    private String TAG = "TAG.RD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_result);

        //GET OPTION
        rPref = PreferenceManager.getDefaultSharedPreferences(this);
        uid = rPref.getString("uid", "none");
        difficulty = rPref.getInt("difficulty", 0);
        ticket = rPref.getInt("ticket", 0);

        Log.d(TAG, String.valueOf(ticket));

        //GET PLAY RESULTS
        Intent i = getIntent();
        repeat = i.getStringExtra("repeat");
        pointAmount = i.getStringExtra("point");
        timeAmount = i.getStringExtra("time");
        distanceAmount = i.getStringExtra("distance");
        bonusTimeAmount = i.getStringExtra("bonus");
        speedMax = i.getStringExtra("speed");


        //WIDGET INITIALIZING
        backgroundClicker = (RelativeLayout)
                findViewById(R.id.background);
        repeatView = (TextView)
                findViewById(R.id.repeat_view);
        pointView = (TextView)
                findViewById(R.id.point_view);
        timeView = (TextView)
                findViewById(R.id.time_amount_view);
        bonusView = (TextView)
                findViewById(R.id.bonus_amount_view);;
        speedView = (TextView)
                findViewById(R.id.max_speed_view);
        distanceView = (TextView)
                findViewById(R.id.distance_amount_view);
        nextTicketView = (TextView)
                findViewById(R.id.next_ticket_view);
        goMainView = (TextView)
                findViewById(R.id.go_main_view);


        nextTicketView.setVisibility(View.INVISIBLE);

        goMainView.setVisibility(View.VISIBLE);
        goMainView.setText("아무 곳을 눌러 전 화면으로!");
        backgroundClicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //SET DISPLAY
        setDisplay();

    }

    private void setDisplay(){

        //REPEAT
        repeatView.setText("REPEAT " + String.valueOf(repeat)+" 도달!");

        //POINT
        pointView.setText(String.valueOf(pointAmount));

        //TIME
        if(Integer.valueOf(timeAmount) > 59) {
            timeView.setText(String.valueOf(Integer.valueOf(timeAmount)/60)+"분 " + String.valueOf(Integer.valueOf(timeAmount)%60)+"초");
        } else {
            timeView.setText(timeAmount+"초");
        }
        //END

        //BONUS
        bonusView.setText("+"+bonusTimeAmount+"초");

        //SPEED
        speedView.setText(speedMax+"km/h");

        //DISTANCE
        if(Integer.valueOf(distanceAmount) > 999) {
            distanceView.setText(String.valueOf(Integer.valueOf(distanceAmount)/1000)+"."+String.valueOf((Integer.valueOf(distanceAmount)%1000)/100)+"km");
        } else {
            distanceView.setText(distanceAmount + "m");
        }

    }

}
