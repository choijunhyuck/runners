package com.runners.choi.runners.challenge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.login.LoginManager;
import com.runners.choi.runners.LoginActivity;
import com.runners.choi.runners.MainActivity;
import com.runners.choi.runners.R;
import com.runners.choi.runners.app.AppController;
import com.runners.choi.runners.app.AppLinks;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import bolts.AppLink;

public class ChallengeActivity extends AppCompatActivity {

    private SharedPreferences mPref;

    //USER INFO
    private String uid;
    private int ticket;

    //COMPONENTS
    private ImageView challengeNoneCharacter;
    private ImageView challengeFab;
    private TextView challengeCount;


    //LISTS
    private ListView listView;
    private ChallengeListAdapter listAdapter;
    private List<ChallengeItem> challengeItem;


    //TICKET VIEW
    private ImageView ticketView;

    private String TAG = "TAG.C";

    private int internetConnetion = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        if(isInternetAvailable()){
            internetConnetion = 1;
        }else{
            internetConnetion = 0;
        }

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        ticket = intent.getIntExtra("ticket", 0);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mPref.edit();

        if(internetConnetion == 1) {
            for (int i = 0; i < 6 ; i++) {
                if (!mPref.getString("challenge" + i, "").equals("")) {
                    updateChallenge(mPref.getString("challenge" + i, ""), i);
                }
            }
        }


        //WIDGET INITIALIZING & SETTING
        challengeNoneCharacter = (ImageView)
                findViewById(R.id.challenge_none_character);
        challengeFab = (ImageView)
                findViewById(R.id.challenge_fab);
        challengeFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChallengeActivity.this, MainActivity.class));
                finish();
            }
        });

        challengeCount = (TextView)
                findViewById(R.id.challenge_count);

        ticketView = (ImageView)
                findViewById(R.id.ticket_view);


        //TICKET SETTING
        switch(ticket) {

            case 0 :

            break;

            case 1 :
            ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_bronze_flat));
            break;

            case 2 :
            ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_silver_flat));
            break;

            case 3 :
            ticketView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ticket_gold_flat));
            break;

            default:
            Toast.makeText(this, "사용자 티켓 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            break;

        }


        //ABOUT LIST INITIALIZING
        listView = (ListView) findViewById
                (R.id.challenge_list_view);
        challengeItem = new ArrayList<ChallengeItem>();
        listAdapter = new ChallengeListAdapter(this, challengeItem);
        listView.setAdapter(listAdapter);


        //CATCH CHALLENGES
        if(!uid.equals("none")) {

            if(internetConnetion == 1) {
                catchChallenges(uid);
            } else {

                for(int j = 0 ; j < 6; j++){

                    if(mPref.getString("challenge"+j, "").equals("")){
                        break;
                    } else {

                        ChallengeItem item = new ChallengeItem(mPref.getString("challenge"+j, ""));
                        challengeItem.add(listAdapter.getCount(), item);

                    }

                }

                Toast.makeText(this, "인터넷 연결이 원활하지 않아 오프라인 데이터가 로드됩니다.", Toast.LENGTH_SHORT).show();

            }

        } else {
            LoginManager.getInstance().logOut();

            Toast.makeText(this, "사용자 정보가 유실되어 처음 화면으로 돌아갑니다."
                    , Toast.LENGTH_LONG).show();

            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

    }

    public void catchChallenges(String uid){

        // appending offset to url
        String url = AppLinks.URL_EXPORT_CHALLENGE + uid;

        JsonArrayRequest req = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() > 0) {

                            challengeCount.setText(String.valueOf(response.length() ));

                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    JSONObject challengeObj = response.getJSONObject(i);

                                    if (challengeObj.has("salt")) {
                                        String salt = challengeObj.getString("salt");

                                        ChallengeItem item = new ChallengeItem(salt);
                                        challengeItem.add(listAdapter.getCount(), item);

                                    }

                                } catch (Exception e) {
                                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                }
                            }

                            listAdapter.notifyDataSetChanged();

                        }else{
                            //NO CHALLENGE
                            challengeCount.setText("0");
                            challengeNoneCharacter.setVisibility(View.VISIBLE);

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(  TAG, "Server Error: " + error.getMessage());

                Toast.makeText(getApplicationContext(),
                        "도전 과제를 불러오는데 실패했습니다. : 인터넷 연결을 확인하세요.", Toast.LENGTH_LONG).show();

            }
        });

        AppController.getInstance().addToRequestQueue(req);

    }

    private void updateChallenge(final String salt, final int i){

            String url = AppLinks.URL_GET_CHALLENGE + uid + "&salt=" + salt;

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    Log.d(TAG, response.toString());
                    Log.d(TAG, "SUCCESS UPDATE : "+salt);

                    SharedPreferences.Editor editor = mPref.edit();
                    editor.remove("challnege"+i);
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
    public void onBackPressed() {

        startActivity(new Intent(ChallengeActivity.this, MainActivity.class));
        finish();

    }

}
