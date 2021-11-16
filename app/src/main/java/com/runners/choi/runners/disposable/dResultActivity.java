package com.runners.choi.runners.disposable;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.runners.choi.runners.LoginActivity;
import com.runners.choi.runners.MainActivity;
import com.runners.choi.runners.R;
import com.runners.choi.runners.app.AppController;
import com.runners.choi.runners.app.AppLinks;

import org.json.JSONObject;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

public class dResultActivity extends AppCompatActivity {

    private RelativeLayout backgroundClicker;

    private TextView repeatView;
    private TextView pointView;
    private TextView timeView;
    private TextView bonusView;
    private TextView speedView;
    private TextView distanceView;
    private TextView goMainView;

    private LinearLayout getLayout;
    private TextView nextTicketView;
    private ImageView ticketView;

    private int repeat;
    private int pointAmount;
    private int timeAomount;
    private int bonusTimeAmount;
    private int speedMax;
    private int distanceAmount;

    private final Handler delayHandler = new Handler();

    private PrettyDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        displaycheck();

        //DIALOG SETTING
        pDialog = new PrettyDialog(this);


        //GET PLAY RESULTS
        Intent i = getIntent();
        repeat = i.getIntExtra("repeat", 0);
        pointAmount = i.getIntExtra("point", 0);
        timeAomount = i.getIntExtra("time", 0);
        bonusTimeAmount = i.getIntExtra("bonus", 0);
        speedMax = i.getIntExtra("speed", 0);
        distanceAmount = i.getIntExtra("distance", 0);

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
                findViewById(R.id.bonus_amount_view);
        ;
        speedView = (TextView)
                findViewById(R.id.max_speed_view);
        distanceView = (TextView)
                findViewById(R.id.distance_amount_view);
        goMainView = (TextView)
                findViewById(R.id.go_main_view);


        //TICKET VIEW INITIALIZING
        getLayout = (LinearLayout)
                findViewById(R.id.get_layout);
        nextTicketView = (TextView)
                findViewById(R.id.next_ticket_view);
        ticketView = (ImageView)
                findViewById(R.id.ticket_view);


        //SET DISPLAY
        if (repeat != 0) {
            setDisplay();
        }


        //GO LOGIN VIEW DEALAY
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                goMainView.setVisibility(View.VISIBLE);
                backgroundClicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        pDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                startActivity(new Intent(dResultActivity.this, LoginActivity.class));
                                finish();
                            }
                        });

                        pDialog.setTitle("RUNNERS 멤버가 되세요!")
                                .setMessage("멤버가 되어 얻을 수 있는 혜택")
                                .addButton("점수 별 티켓 획득 가능",
                                        R.color.colorAccent,
                                        R.color.pdlg_color_green,
                                        new PrettyDialogCallback() {
                                            @Override
                                            public void onClick() {

                                            }
                                        })
                                .addButton("난이도 변경 및 도전과제",
                                        R.color.colorAccent,
                                        R.color.pdlg_color_green,
                                        new PrettyDialogCallback() {
                                            @Override
                                            public void onClick() {

                                            }
                                        })
                                .addButton("기타 등등 (파워당당)",
                                        R.color.colorAccent,
                                        R.color.pdlg_color_green,
                                        new PrettyDialogCallback() {
                                            @Override
                                            public void onClick() {

                                            }
                                        })
                                .show();
                    }
                });

            }
        }, 3000);

    }

    private void setDisplay() {

        //REPEAT
        repeatView.setText("REPEAT " + String.valueOf(repeat) + " 도달!");

        //POINT
        pointView.setText(String.valueOf(pointAmount));

        //TIME
        if (timeAomount > 59) {
            timeView.setText(String.valueOf(timeAomount / 60) + "분 " + String.valueOf(timeAomount % 60) + "초");
        } else {
            timeView.setText(String.valueOf(timeAomount) + "초");
        }
        //END

        //BONUS
        bonusView.setText("+" + String.valueOf(bonusTimeAmount) + "초");

        //SPEED
        speedView.setText(String.valueOf(speedMax) + "km/h");

        //DISTANCE
        if (distanceAmount > 999) {
            distanceView.setText(String.valueOf(distanceAmount / 1000) + "." + String.valueOf((distanceAmount % 1000) / 100) + "km");
        } else {
            distanceView.setText(String.valueOf(distanceAmount) + "m");
        }

        //TICKET SETTING
        newTextSetting();

        if (pointAmount > 3999 && pointAmount < 8000) {


            ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_bronze));
            nextTicketView.setText("다음 티켓까지 " + String.valueOf((8000 - pointAmount)) + "점");
            getLayout.setVisibility(View.VISIBLE);


        } else if (pointAmount > 7999 && pointAmount < 11999) {

            ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_silver));
            nextTicketView.setText("다음 티켓까지 " + String.valueOf((12000 - pointAmount)) + "점");
            getLayout.setVisibility(View.VISIBLE);

        } else if (pointAmount > 11999) {

            ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_gold));
            nextTicketView.setText("최대 티켓 달성");
            getLayout.setVisibility(View.VISIBLE);

        } else {

            getLayout.setVisibility(View.GONE);
            nextTicketView.setText("다음 티켓까지 " + String.valueOf((4000 - pointAmount)) + "점");

        }
    }
    //END

    private void newTextSetting(){

        TextView myText = (TextView) findViewById(R.id.new_text);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(50); //You can manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        myText.startAnimation(anim);

    }

    private void displaycheck(){
        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int deviceWidth = displayMetrics.widthPixels;

        int deviceHeight = displayMetrics.heightPixels;

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int dipWidth  = (int) (120  * displayMetrics.density);

        int dipHeight = (int) (90 * displayMetrics.density);

        /*
        System.out.println("displayMetrics.density : " + displayMetrics.density);
        System.out.println("deviceWidth : " + deviceWidth +", deviceHeight : "+deviceHeight);
        */

        //1920
        if( deviceHeight > 1900){

            setContentView(R.layout.activity_result);

        }else{

            setContentView(R.layout.activity_result);

        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(dResultActivity.this, MainActivity.class));
        finish();
    }

}
