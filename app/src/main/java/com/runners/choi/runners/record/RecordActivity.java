package com.runners.choi.runners.record;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.facebook.login.LoginManager;
import com.runners.choi.runners.LoginActivity;
import com.runners.choi.runners.MainActivity;
import com.runners.choi.runners.R;
import com.runners.choi.runners.ResultActivity;
import com.runners.choi.runners.app.AppController;
import com.runners.choi.runners.app.AppLinks;
import com.runners.choi.runners.challenge.ChallengeItem;
import com.runners.choi.runners.challenge.ChallengeListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;


public class RecordActivity extends AppCompatActivity {

    private SharedPreferences mPref;


    //COMPONENTS
    private TextView pointView;
    private TextView distanceView;
    private TextView timeView;
    private TextView speedView;
    private TextView repeatView;


    //LIST
    private GridView resultList;
    private RecordListAdapter listAdapter;
    private List<RecordItem> recordItem;

    //USER INFO
    private String uid;

    //DATA SET
    private String point;
    private String distance;
    private String time;
    private String speed;
    private String repeat;

    //INT

    private int intPoint = 0;
    private int intDistance = 0;
    private int intTime = 0;
    private int intSpeed = 0;
    private int intRepeat = 0;

    //OFFLINE WIDGET
    private PrettyDialog pDialog;
    private Button offlineWidget;

    private String TAG = "TAG.RC";

    private int internetConnection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_record);

        if(isInternetAvailable()){
            internetConnection = 1;
        }else{
            internetConnection = 0;
        }

        pDialog = new PrettyDialog(this);
        setPDialog();
        offlineWidget = (Button)
                findViewById(R.id.record_off_widget)
        ;
        if(isInternetAvailable()){
            internetConnection = 1;
        } else {
            internetConnection = 0;
            offlineWidget.setVisibility(View.VISIBLE);
            offlineWidget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isInternetAvailable()){

                        internetConnection = 1;
                        Toast.makeText(RecordActivity.this, "????????? ?????????", Toast.LENGTH_LONG).show();
                    } else {
                        internetConnection = 0;
                        pDialog.show();
                    }

                }
            });
        }

        //GET USER INFO
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        uid = mPref.getString("uid", "none");

        if(internetConnection == 1) {
            for (int i = 0; i < 18 ; i++) {
                if (!mPref.getString("result" + i, "").equals("")) {

                    String strJson = mPref.getString("result"+i,"");

                    try {
                        JSONObject response = new JSONObject(strJson);

                        /*String uid = response.get("uid").toString();*/
                        String repeat = response.get("repeat").toString();
                        String pointAmount = response.get("pointAmount").toString();
                        String timeAmount = response.get("timeAmount").toString();
                        String distanceAmount = response.get("distanceAmount").toString();
                        String bonus = response.get("bonusTimeAmount").toString();
                        String speedMax = response.get("speedMax").toString();
                        String date = response.get("date").toString();

                        updateResult(uid, repeat, pointAmount, timeAmount, distanceAmount, bonus, speedMax, date, i);

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        }


        //RECORD SETTING
        pointView = (TextView)
                findViewById(R.id.record_point_view);
        distanceView = (TextView)
                findViewById(R.id.record_distance_view);
        timeView = (TextView)
                findViewById(R.id.record_time_view);
        speedView = (TextView)
                findViewById(R.id.record_speed_view);
        repeatView = (TextView)
                findViewById(R.id.record_repeat_view);
        resultList = (GridView)
                findViewById(R.id.result_list);



        //DAY & NIGHT SETTING
        Calendar calendar = Calendar.getInstance( );
        int time = calendar.get((Calendar.HOUR_OF_DAY));
        //NIGHT, NOON, MORNING
        if(time < 7 || time > 18 ){
            findViewById(R.id.record_background).setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_night));
        } else if(time > 12) {
            findViewById(R.id.record_background).setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_noon));
        } else{
            findViewById(R.id.record_background).setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen));
        }


        //DISPLAY SETTING
        getRecord(uid);


        //LIST SETTING
        recordItem = new ArrayList<RecordItem>();
        listAdapter = new RecordListAdapter(this, recordItem);
        resultList.setAdapter(listAdapter);

        //CATCH RESULTS
        if(!uid.equals("none")) {

            catchResults(uid);

        } else {
            LoginManager.getInstance().logOut();

            Toast.makeText(this, "????????? ????????? ???????????? ?????? ???????????? ???????????????."
                    , Toast.LENGTH_LONG).show();

            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }


        findViewById(R.id.record_exit_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    public void getRecord(String uid) {

        if(internetConnection == 1) {

            // appending offset to url
            String url = AppLinks.URL_EXPORT_USER_RECORD + uid;

            JsonArrayRequest req = new JsonArrayRequest(url,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {

                            if (response.length() > 0) {

                                for (int i = 0; i < response.length(); i++) {
                                    try {

                                        JSONObject recordObject = response.getJSONObject(i);

                                        point = recordObject.getString("pointAmount");
                                        distance = recordObject.getString("distance");
                                        time = recordObject.getString("time");
                                        speed = recordObject.getString("speed");
                                        repeat = recordObject.getString("repeat");


                                        int distanceConv = 0;
                                        int timeConv = 0;

                                        if(!distance.equals("0")){
                                            distanceConv = Integer.valueOf(distance);
                                        }
                                        if(!time.equals("0")) {
                                            timeConv = Integer.valueOf( time);
                                        }

                                        pointView.setText(point);

                                        if(distanceConv > 999) {
                                            distanceView.setText(String.valueOf(distanceConv/1000)+"."+String.valueOf((distanceConv%1000)/100)+"km");
                                        } else {
                                            distanceView.setText(String.valueOf(distanceConv) + "m");
                                        }

                                        if(timeConv > 59) {
                                            timeView.setText(String.valueOf(timeConv/60)+"??? " + String.valueOf(timeConv%60)+"???");
                                        } else {
                                            timeView.setText(String.valueOf(timeConv)+"???");
                                        }

                                        speedView.setText(speed+"km/h");
                                        repeatView.setText(repeat+"???");

                                    } catch (Exception e) {
                                        Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                    }
                                }

                            } else {
                                //NO RECORDS

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Server Error: " + error.getMessage());

                    Toast.makeText(getApplicationContext(),
                            "?????? ??????", Toast.LENGTH_LONG).show();

                }
            });

            AppController.getInstance().addToRequestQueue(req);
        } else {

            Toast.makeText(this, "???????????? ???????????? ???????????????", Toast.LENGTH_LONG).show();

            for(int i = 0 ; i < 18 ; i++) {

                String strJson = mPref.getString("result"+i,"");

                if (!strJson.equals("")){

                    //GET RECORDS (OFFLINE)
                    try {
                        JSONObject response = new JSONObject(strJson);

                        intPoint += Integer.valueOf(response.get("pointAmount").toString());

                        intDistance += Integer.valueOf(response.get("distanceAmount").toString());

                        intTime += Integer.valueOf(response.get("timeAmount").toString());

                        if(intSpeed < Integer.valueOf(response.get("speedMax").toString())) {
                            speed = response.get("speedMax").toString();
                            intSpeed = Integer.valueOf(response.get("speedMax").toString());
                        }

                        if(intRepeat < Integer.valueOf(response.get("repeat").toString())) {
                            repeat = response.get("repeat").toString();
                            intRepeat = Integer.valueOf(response.get("repeat").toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    break;
                }

            }

            pointView.setText(point);

            if(intDistance > 999) {
                distanceView.setText(String.valueOf(intDistance/1000)+"."+String.valueOf((intDistance%1000)/100)+"km");
            } else {
                distanceView.setText(String.valueOf(intDistance) + "m");
            }

            if(intTime > 59) {
                timeView.setText(String.valueOf(intTime/60)+"??? " + String.valueOf(intTime%60)+"???");
            } else {
                timeView.setText(String.valueOf(intTime)+"???");
            }

            speedView.setText(speed+"km/h");
            repeatView.setText(repeat+"???");

        }

    }


    public void catchResults(String uid){

        if(internetConnection == 1) {

            // appending offset to url
            String url = AppLinks.URL_EXPORT_USER_RESULT + uid;

            JsonArrayRequest req = new JsonArrayRequest(url,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {

                            if (response.length() > 0) {

                                //RESULT ONLY STORED LIMIT 17
                                if(response.length() < 18) {

                                    for (int i = response.length(); i > -1; i--) {
                                        try {
                                            JSONObject resultObj = response.getJSONObject(i);

                                            if (resultObj.has("repeat")) {

                                                String repeat = resultObj.getString("repeat");
                                                String point = resultObj.getString("pointAmount");
                                                String time = resultObj.getString("time");
                                                String distance = resultObj.getString("distance");
                                                String bonus = resultObj.getString("bonus");
                                                String speed = resultObj.getString("speed");
                                                String date = resultObj.getString("created_at");

                                                RecordItem item = new RecordItem(repeat, point, time, distance, bonus, speed, date);
                                                recordItem.add(listAdapter.getCount(), item);

                                            }

                                        } catch (Exception e) {
                                            Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                        }
                                    }

                                    listAdapter.notifyDataSetChanged();

                                } else {

                                    for (int i = 17; i > -1; i--) {
                                        try {
                                            JSONObject resultObj = response.getJSONObject(i);

                                            if (resultObj.has("repeat")) {

                                                String repeat = resultObj.getString("repeat");
                                                String point = resultObj.getString("pointAmount");
                                                String time = resultObj.getString("time");
                                                String distance = resultObj.getString("distance");
                                                String bonus = resultObj.getString("bonus");
                                                String speed = resultObj.getString("speed");
                                                String date = resultObj.getString("created_at");

                                                RecordItem item = new RecordItem(repeat, point, time, distance, bonus, speed, date);
                                                recordItem.add(listAdapter.getCount(), item);

                                            }

                                        } catch (Exception e) {
                                            Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                        }
                                    }

                                    listAdapter.notifyDataSetChanged();

                                }

                            } else {
                                //NO CHALLENGE

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Server Error: " + error.getMessage());

                    Toast.makeText(getApplicationContext(),
                            "????????? ??????????????? ??????????????????. : ????????? ????????? ???????????????.", Toast.LENGTH_LONG).show();

                }
            });

            AppController.getInstance().addToRequestQueue(req);
        } else {

            for(int i = 0 ; i < 18 ; i++){

                if(!mPref.getString("result"+i, "").equals("")){

                    String strJson = mPref.getString("result"+i,"");

                    try {
                        JSONObject response = new JSONObject(strJson);

                        /*String uid = response.get("uid").toString();*/
                        String repeat = response.get("repeat").toString();
                        String pointAmount = response.get("pointAmount").toString();
                        String timeAmount = response.get("timeAmount").toString();
                        String distanceAmount = response.get("distanceAmount").toString();
                        String bonus = response.get("bonusTimeAmount").toString();
                        String speedMax = response.get("speedMax").toString();
                        String date = response.get("date").toString();

                        RecordItem item = new RecordItem(repeat, pointAmount, timeAmount, distanceAmount, bonus, speedMax, date);
                        recordItem.add(listAdapter.getCount(), item);

                        Log.d(TAG, "offline ?????? ????????????");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }

    public void updateResult(final String uid, final String repeat, final String point, final String time,
                            final String distance, final String bonus, final String speed, final String date, final int i) {

        String tag_string_req = "request_register_result";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppLinks.URL_REGISTER_RESULT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        Log.d(TAG, "online ????????????");

                        SharedPreferences.Editor editor = mPref.edit();
                        editor.remove("result"+i);
                        editor.commit();

                    } else {

                        Toast.makeText(RecordActivity.this, "?????? ????????? ?????????????????? : ?????? ??????", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e(TAG, "?????? ??????: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "????????? ????????? ???????????????", Toast.LENGTH_LONG).show();

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
                params.put("date", date);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void setPDialog(){

        if(pDialog != null) {

            pDialog.setTitle("?????? ????????? ????????? ???????????? ????????????.")
                    .setMessage("?????? ???????????? ????????? ???????????????.")
                    .addButton("?????? ????????? ???????????? ????????? ?????????",
                            R.color.colorAccent,
                            R.color.colorPrimary,
                            new PrettyDialogCallback() {
                                @Override
                                public void onClick() {

                                }
                            });

        }

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

}
