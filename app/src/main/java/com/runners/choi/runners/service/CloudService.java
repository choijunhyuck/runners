package com.runners.choi.runners.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.runners.choi.runners.app.AppController;
import com.runners.choi.runners.app.AppLinks;
import com.runners.choi.runners.record.RecordActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CloudService extends Service {

    private SharedPreferences mPref;
    SharedPreferences.Editor editor;

    private String uid;

    private String TAG = "TAG.CS";

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.d("test", "서비스의 onCreate");

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mPref.edit();

        uid = mPref.getString("uid", "none");

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행

        if(isInternetAvailable()) {

            for (int i = 0; i < 18; i++) {

                //result
                if (!mPref.getString("result" + i, "").equals("")) {

                    if (!uid.equals("none")) {

                        String strJson = mPref.getString("result" + i, "");

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

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "서비스 사용 불가 : NONE UID");
                    }

                }
            }

            for(int i = 0 ; i < 6 ; i++){

                //challenge
                if (!mPref.getString("challenge" + i, "").equals("")) {

                    if(!uid.equals("none")) {
                        updateChallenge(mPref.getString("challenge" + i, ""), i);

                    } else {
                        Log.d(TAG, "서비스 사용 불가 : NONE UID");
                    }

                }
            }

            this.onDestroy();

        } else {
            Log.d(TAG, "NO INTERNET CONNECTION");
            this.onDestroy();
        }

        return super.onStartCommand(intent, flags, startId);
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

                        Log.d(TAG, "online 업로드됨");

                        SharedPreferences.Editor editor = mPref.edit();
                        editor.remove("result"+i);
                        editor.commit();

                    } else {

                        Log.d(TAG, "결과 저장 실패 : 서버 오류");

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

    private void updateChallenge(final String salt, final int i){

        String url = AppLinks.URL_GET_CHALLENGE + uid + "&salt=" + salt;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                Log.d(TAG, response.toString());
                Log.d(TAG, "SUCCESS UPDATE : "+salt);

                editor.remove("challnege" + i);
                editor.commit();

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
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행
    }
}