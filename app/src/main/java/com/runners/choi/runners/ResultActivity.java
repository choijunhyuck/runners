package com.runners.choi.runners;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.facebook.login.LoginManager;
import com.runners.choi.runners.app.AppController;
import com.runners.choi.runners.app.AppLinks;
import com.runners.choi.runners.challenge.ChallengeName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.support.v4.view.accessibility.AccessibilityEventCompat.setAction;


public class ResultActivity extends AppCompatActivity {

    private SharedPreferences mPref;

    private RelativeLayout backgroundClicker;

    private TextView repeatView;
    private TextView pointView;
    private TextView timeView;
    private TextView bonusView;
    private TextView speedView;
    private TextView distanceView;
    private TextView goMainView;

    private ImageView challengeExistButton;

    private LinearLayout getLayout;
    private TextView nextTicketView;
    private ImageView ticketView;

    private String salt;

    private int repeat = 0;
    private int pointAmount = 0;
    private int timeAmount = 0;
    private int bonusTimeAmount = 0;
    private int speedMax = 0;
    private int distanceAmount = 0;

    private String uid;
    private int difficulty;
    private int ticket;

    private final Handler delayHandler = new Handler();

    private String TAG = "TAG.R";

    private int internetConnection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_result);

        if(isInternetAvailable()){
            internetConnection = 1;
        }else{
            internetConnection = 0;
        }

        //GET OPTION
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        uid = mPref.getString("uid", "none");
        difficulty = mPref.getInt("difficulty", 0);
        ticket = mPref.getInt("ticket", 0);

        Log.d(TAG, "ticket = "+String.valueOf(ticket));

        //GET PLAY RESULTS
        Intent i = getIntent();
        repeat = i.getIntExtra("repeat", 0);
        pointAmount = i.getIntExtra("point", 0);
        timeAmount = i.getIntExtra("time", 0);
        bonusTimeAmount = i.getIntExtra("bonus", 0);
        speedMax = i.getIntExtra("speed", 0);
        distanceAmount = i.getIntExtra("distance", 0);

        //************CHALLENGE CHECK//
        if( !i.getStringExtra("salt").equals("none") ){
            salt = i.getStringExtra("salt");
            getChallenge(salt);
        } else {
            salt = "none";
        }

        //************SPEED CHECK//
        if(speedMax > 45){
         speedMax -= 20;
        }


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
        goMainView = (TextView)
                findViewById(R.id.go_main_view);
        challengeExistButton = (ImageView)
                findViewById(R.id.challenge_asset_exist);

        //TICKET VIEW INITIALIZING
        getLayout = (LinearLayout)
                findViewById(R.id.get_layout);
        nextTicketView = (TextView)
                findViewById(R.id.next_ticket_view);
        ticketView = (ImageView)
                findViewById(R.id.ticket_view);

        if(!uid.equals("none")) {

        if(repeat == 0 && pointAmount == 0) {

            setDisplay();
            goMainFunc();

        } else {
            setDisplay();

            if(internetConnection == 1) {
                storeResult(uid, String.valueOf(repeat), String.valueOf(pointAmount), String.valueOf(timeAmount),
                        String.valueOf(distanceAmount), String.valueOf(bonusTimeAmount), String.valueOf(speedMax));
            } else {

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                Date date = new Date();
                String strDate = dateFormat.format(date);

                Log.d(TAG, "date = "+strDate);

                    JSONObject obj = new JSONObject() ;

                    try {
                        obj.put("uid", uid);
                        obj.put("repeat", repeat);
                        obj.put("pointAmount", pointAmount);
                        obj.put("timeAmount", timeAmount);
                        obj.put("distanceAmount", distanceAmount);
                        obj.put("bonusTimeAmount", bonusTimeAmount);
                        obj.put("speedMax", speedMax);
                        obj.put("date", strDate);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    saveJson(obj);

            }
        }

        }else{
            Toast.makeText(this, "사용자 인증 정보 확인에 실패했습니다. 처음 화면으로 돌아갑니다.", Toast.LENGTH_LONG).show();
            LoginManager.getInstance().logOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

    }

    private void saveJson(JSONObject obj){

        for(int i = 0 ; ; i++){

            if(!mPref.getString("result"+i, "").equals("")){
                continue;
            } else {

                SharedPreferences.Editor editor = mPref.edit();
                editor.putString("result"+i, obj.toString());
                editor.commit();

                goMainFunc();

                break;
            }
        }

    }


    private void setDisplay(){

        //REPEAT
        repeatView.setText("REPEAT " + String.valueOf(repeat)+" 도달!");

        //POINT
        pointView.setText(String.valueOf(pointAmount));

        //TIME
        if(timeAmount > 59) {
            timeView.setText(String.valueOf(timeAmount/60)+"분 " + String.valueOf(timeAmount%60)+"초");
        } else {
            timeView.setText(String.valueOf(timeAmount)+"초");
        }
        //END

        //BONUS
        bonusView.setText("+"+String.valueOf(bonusTimeAmount)+"초");

        //SPEED
        speedView.setText(String.valueOf(speedMax)+"km/h");

        //DISTANCE
        if(distanceAmount > 999) {
            distanceView.setText(String.valueOf(distanceAmount/1000)+"."+String.valueOf((distanceAmount%1000)/100)+"km");
        } else {
            distanceView.setText(String.valueOf(distanceAmount) + "m");
        }

        //CHALLENGE EXIST BUTTON
        if(!salt.equals("none")){
            challengeExistButton.setVisibility(View.VISIBLE);
            challengeExistButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                        challengeExistButton.setVisibility(View.GONE);

                        Snackbar snack = Snackbar.make(backgroundClicker, "도전과제 획득 : " + ChallengeName.setName(salt), Snackbar.LENGTH_LONG);
                        View snackView = snack.getView();
                        snackView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        snack.setAction("확인", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();

                }
            });

        }

        //TICKET SETTING
        newTextSetting();

        if(pointAmount > 3999 && pointAmount < 8000){

            switch (ticket){

                case 0 :
                    ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_bronze));
                    nextTicketView.setText("다음 티켓까지 "+String.valueOf((8000-pointAmount))+"점");
                    getLayout.setVisibility(View.VISIBLE);
                    getTicket(1);

                    break;

                case 1:
                    getLayout.setVisibility(View.INVISIBLE);
                    nextTicketView.setText("다음 티켓까지 "+String.valueOf((8000-pointAmount))+"점");

                    break;

                case 2:
                    getLayout.setVisibility(View.INVISIBLE);
                    nextTicketView.setText("다음 티켓까지 "+String.valueOf((12000-pointAmount))+"점");

                    break;

                case 3:
                    getLayout.setVisibility(View.INVISIBLE);
                    nextTicketView.setText("최대 티켓 달성");

                    break;

            }


        } else if (pointAmount > 7999 && pointAmount < 11999){

            switch (ticket){

                case 0 :
                    ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_silver));
                    getLayout.setVisibility(View.VISIBLE);
                    nextTicketView.setText("다음 티켓까지 "+String.valueOf((12000-pointAmount))+"점");
                    getTicket(2);

                    break;

                case 1:
                    ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_silver));
                    getLayout.setVisibility(View.VISIBLE);
                    nextTicketView.setText("다음 티켓까지 "+String.valueOf((12000-pointAmount))+"점");
                    getTicket(2);

                    break;

                case 2:
                    getLayout.setVisibility(View.INVISIBLE);
                    nextTicketView.setText("다음 티켓까지 "+String.valueOf((12000-pointAmount))+"점");

                    break;

                case 3:
                    getLayout.setVisibility(View.INVISIBLE);
                    nextTicketView.setText("최대 티켓 달성");

                    break;

            }

        } else if (pointAmount > 11999){

            switch (ticket){

                case 0 :
                    ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_gold));
                    getLayout.setVisibility(View.VISIBLE);
                    nextTicketView.setText("최대 티켓 달성");
                    getTicket(3);

                    break;

                case 1:
                    ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_gold));
                    getLayout.setVisibility(View.VISIBLE);
                    nextTicketView.setText("최대 티켓 달성");
                    getTicket(3);

                    break;

                case 2:
                    ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_gold));
                    getLayout.setVisibility(View.VISIBLE);
                    nextTicketView.setText("최대 티켓 달성");
                    getTicket(3);

                    break;

                case 3:
                    getLayout.setVisibility(View.INVISIBLE);
                    nextTicketView.setText("최대 티켓 달성");

                    break;

            }

        } else {

            switch (ticket){

                case 0 :
                    getLayout.setVisibility(View.INVISIBLE);
                    nextTicketView.setText("다음 티켓까지 "+String.valueOf((4000-pointAmount))+"점");

                    break;

                case 1:
                    getLayout.setVisibility(View.INVISIBLE);
                    nextTicketView.setText("다음 티켓까지 "+String.valueOf((8000-pointAmount))+"점");

                    break;

                case 2:
                    getLayout.setVisibility(View.INVISIBLE);
                    nextTicketView.setText("다음 티켓까지 "+String.valueOf((12000-pointAmount))+"점");

                    break;

                case 3:
                    getLayout.setVisibility(View.INVISIBLE);
                    nextTicketView.setText("최대 티켓 달성");

                    break;

            }

        }

        //END

    }

    private void getTicket(final int ticket){

        if(internetConnection == 1) {

            String url = AppLinks.URL_GET_USER_TICKET + uid + "&ticket=" + String.valueOf(ticket);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt("ticket", ticket);
                    editor.commit();

                    Log.d(TAG, response.toString());
                    Log.d(TAG, "SUCCESS : ONLINE");

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
            editor.putInt("ticket", ticket);
            editor.commit();

            Log.d(TAG, "SUCCESS : OFFLINE");

        }

    }

    public void storeResult(final String uid, final String repeat, final String point, final String time,
                                     final String distance, final String bonus, final String speed) {

        String tag_string_req = "request_register_result";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppLinks.URL_REGISTER_RESULT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        //GO MAIN VIEW DEALAY
                        goMainFunc();

                    } else {

                        Toast.makeText(ResultActivity.this, "결과 저장에 실패했습니다 : 서버 오류", Toast.LENGTH_SHORT).show();

                        goMainView.setVisibility(View.VISIBLE);
                        backgroundClicker.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(ResultActivity.this, MainActivity.class));
                                finish();
                            }
                        });

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e(TAG, "등록 에러: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "인터넷 연결을 확인하세요", Toast.LENGTH_LONG).show();

                goMainView.setVisibility(View.VISIBLE);
                backgroundClicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(ResultActivity.this, MainActivity.class));
                        finish();
                    }
                });

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("uid", uid);
                params.put("repeat", repeat);
                params.put("point", point);
                params.put("time", time);
                params.put("distance", distance);
                params.put("bonus", bonus);
                params.put("speed", speed);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void getChallenge(String salt){

            if (internetConnection == 1) {

                String url = AppLinks.URL_GET_CHALLENGE + uid + "&salt=" + salt;

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                        url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(TAG, response.toString());
                        Log.d(TAG, "SUCCESS");

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

                for (int i = 0; i < 6; i++) {

                    if (!mPref.getString("challenge" + i, "").equals("")) {
                        continue;
                    } else if (mPref.getString("challenge" + i, "").equals("")) {
                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putString("challenge" + i, salt);
                        editor.commit();

                        Log.d(TAG, "success : challenge : OFFLINE" + salt);

                        break;
                    }

                }

            }

    }

    private void goMainFunc(){
        //GO MAIN VIEW DEALAY
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                goMainView.setVisibility(View.VISIBLE);
                backgroundClicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(ResultActivity.this, MainActivity.class));
                        finish();
                    }
                });

            }
        }, 3000);
    }

    private void newTextSetting(){

        TextView myText = (TextView) findViewById(R.id.new_text);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(150); //You can manage the time of the blink with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        myText.startAnimation(anim);

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
        startActivity(new Intent(ResultActivity.this, MainActivity.class));
        finish();
    }

}
