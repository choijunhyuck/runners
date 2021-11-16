package com.runners.choi.runners;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
import com.facebook.login.LoginManager;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.runners.choi.runners.app.AppController;
import com.runners.choi.runners.app.AppLinks;
import com.runners.choi.runners.challenge.ChallengeActivity;
import com.runners.choi.runners.guide.GuideActivity;
import com.runners.choi.runners.play.EasyPlayActivity;
import com.runners.choi.runners.play.HardPlayActivity;
import com.runners.choi.runners.play.NormalPlayActivity;
import com.runners.choi.runners.service.CloudService;
import com.runners.choi.runners.setting.SettingOneAcitivity;
import com.runners.choi.runners.utils.RandomUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends AppCompatActivity {

    //TO SAVE USER ALL
    private SharedPreferences mPref;

    //USER INFORMAION
    private String uid;
    private String email;
    private String name;
    private String gender;
    private String link;
    private String profile_img;


    //USER OPTION
    private int difficulty = 0;
    private int ticket = 0;
    private int music = 1;


    private int isFirst = 0;


    //LAYOUT COMPONENTS
    private FloatingActionsMenu fab_menu;

    private RelativeLayout background;
    private RelativeLayout shadowEffect;

    private LinearLayout countLayout;
    private TextView countView;

    private ImageView playButton;

    private ImageView musicTap;
    private ImageView checkTap;
    private ImageView safeFrameTap;

    private ImageView checkPop;
    private ImageView safeFramePop;

    //SOUND BAR COMPONENTS
    private ImageView musicView;
    private ImageView musicPlayIcon;
    private TextView musicPlay;
    private TextView musicBack;
    private LinearLayout musicMute;
    private TextView musicMuteText;
    private RelativeLayout soundBar;

    private MediaPlayer mp;


    //OFFLINE WIDGET
    private PrettyDialog pDialog;
    private Button offlineWidget;

    private final Handler delayHandler = new Handler();

    //TAG
    private String TAG = "TAG.M";

    private int internetConnection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        displayCheck();

            pDialog = new PrettyDialog(this);
            setPDialog();
            offlineWidget = (Button)
                    findViewById(R.id.main_off_widget);

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
                        Toast.makeText(MainActivity.this, "인터넷 연결됨", Toast.LENGTH_LONG).show();
                    } else {
                        internetConnection = 0;
                        pDialog.show();
                    }

                }
            });
        }

        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        deleteCache(getApplicationContext());

        //START UPLOAD
        Log.d(TAG, "서비스 정상 시작");
        Intent intent = new Intent(
                getApplicationContext(),//현재제어권자
                CloudService.class); // 이동할 컴포넌트
        startService(intent); // 서비스 시작


        //USER FIRST OR ERROR TO SAVE USER INFO
        if( (mPref.getString("uid", "none")).equals("none")){

            Intent i = getIntent();

            uid = i.getStringExtra("uid");
             email = i.getStringExtra("email");
            name = i.getStringExtra("name");
            gender = i.getStringExtra("gender");
            link = i.getStringExtra("link");
            profile_img = i.getStringExtra("profile_img");

            SharedPreferences.Editor editor = mPref.edit();
            editor.putString("uid", uid);
            editor.putString("email", email);
            editor.putString("name", name);
            editor.putString("gender", gender);
            editor.putString("link", link);
            editor.putString("profile_img", profile_img);
            editor.putInt("difficulty", 1);
            editor.putInt("tickets", 0);
            editor.putInt("isFirst", 0);

            //DEFAULT MUSIC OPTION
            editor.putInt("music", 1);
            editor.commit();

            difficulty = 1;

            if(uid == null) {

                LoginManager.getInstance().logOut();

                Toast.makeText(this, "사용자 정보가 유실되어 처음 화면으로 돌아갑니다."
                        , Toast.LENGTH_LONG).show();

                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();

            }

        } else {

            //USER NOT FIRST
            uid = mPref.getString("uid", "none");
            email = mPref.getString("email", "none");
            name = mPref.getString("name", "none");
            gender = mPref.getString("gender", "none");
            link = mPref.getString("link", "none");
            profile_img = mPref.getString("profile_img", "none");
            music = mPref.getInt("music",1);
            isFirst = mPref.getInt("isFirst", 0);

            if(internetConnection == 1){
                getOption(uid);
            } else {
                difficulty = mPref.getInt("difficulty", 1);
                ticket = mPref.getInt("ticket", 0);

                Log.d(TAG, String.valueOf(difficulty));
            }


            if(uid.equals("none")) {
                LoginManager.getInstance().logOut();

                Toast.makeText(this, "사용자 정보가 유실되어 처음 화면으로 돌아갑니다."
                        , Toast.LENGTH_LONG).show();

                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();

                Log.d(TAG, "MainActivity Error, USER NONE INFORMATION");

            } else {
                Log.d(TAG, "*****정보유실 검사완료***** / "+uid);
            }


        }


        //INITIALIZING
        background = (RelativeLayout)
                findViewById(R.id.main_background);
        playButton = (ImageView)
                findViewById(R.id.main_play_button);


        //TAPS
        musicTap = (ImageView)
                findViewById(R.id.main_music_tap);
        checkTap = (ImageView)
                findViewById(R.id.main_check_tap);
        safeFrameTap = (ImageView)
                findViewById(R.id.main_safe_frame_tap);


        //POPS
        checkPop = (ImageView)
                findViewById(R.id.main_check_pop);
        safeFramePop = (ImageView)
                findViewById(R.id.main_safe_frame_pop);

        /*SHADOW*/
        shadowEffect = (RelativeLayout)
                findViewById(R.id.shadow_effect);


        /*COUNTS*/
        countLayout = (LinearLayout)
                findViewById(R.id.count_layout);
        countView = (TextView)
                findViewById(R.id.count_view);


        /*FAB*/
        fab_menu = (FloatingActionsMenu)
                findViewById(R.id.fab_menu);

        /*SOUND BAR*/
        musicView = (ImageView)
                findViewById(R.id.main_music_view);
        musicPlayIcon = (ImageView)
                findViewById(R.id.music_play_icon);
        musicPlay = (TextView)
                findViewById(R.id.main_music_playing_button);
        musicBack = (TextView)
                findViewById(R.id.main_music_back_button);
        musicMute = (LinearLayout)
                findViewById(R.id.main_music_mute_button);
        musicMuteText = (TextView)
                findViewById(R.id.main_music_mute_button_text);
        soundBar = (RelativeLayout)
                findViewById(R.id.sound_bar_background);

        Calendar calendar = Calendar.getInstance( );
        int time = calendar.get((Calendar.HOUR_OF_DAY));

        //NIGHT, NOON, MORNING
        if(time < 7 || time > 18 ){
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_night));
            safeFrameTap.setVisibility(View.VISIBLE);
        } else if(time > 12) {
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_noon));
            safeFrameTap.setVisibility(View.INVISIBLE);
        } else{
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen));
            safeFrameTap.setVisibility(View.INVISIBLE);
        }

        //TEXT 4 SETTING (IS FIRST OR SAFETY FRAME)
        if(isFirst == 0){

            checkPop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.main_asset_guide_pop));
            checkPop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    startActivity(new Intent(MainActivity.this, GuideActivity.class));
                    finish();

                }
            });
        } else if(isFirst == 1){

            checkPop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.main_asset_weather_pop));
            checkPop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse("https://www.google.co.kr/search?q=weather");
                    intent.setData(uri);
                    startActivity(intent);

                }
            });
        }

        //TAPS & POPS SET FUNCTIONS

        safeFramePop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SafeFrameActivity.class));
            }
        });


        musicTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                musicTap.setVisibility(View.INVISIBLE);
                soundBar.setVisibility(View.VISIBLE);

                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                soundBar.setVisibility(View.INVISIBLE);
                musicTap.setVisibility(View.VISIBLE);

                    }
                }, 5000);

            }
        });

        checkTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPop.setVisibility(View.VISIBLE);
                checkTap.setVisibility(View.INVISIBLE);

                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        checkPop.setVisibility(View.INVISIBLE);
                        checkTap.setVisibility(View.VISIBLE);

                    }
                }, 5000);

            }
        });

        safeFrameTap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                safeFrameTap.setVisibility(View.INVISIBLE);
                safeFramePop.setVisibility(View.VISIBLE);

                delayHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        safeFramePop.setVisibility(View.INVISIBLE);
                        safeFrameTap.setVisibility(View.VISIBLE);

                    }
                }, 5000);

            }
        });

        //PLAY BUTTON SETTING
        playButton.setClickable(TRUE);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mp.isPlaying()){
                    mp.stop();
                }

                Log.d("TAG", String.valueOf(difficulty));

                if(difficulty != 0 && !uid.equals("none")) {

                    switch (difficulty) {
                        case 1:

                            Log.d(TAG, "EASY GO");

                            Intent i = new Intent(getApplicationContext(), EasyPlayActivity.class);
                            i.putExtra("music", music);
                            startActivity(i);
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in);
                            finish();

                            break;
                        case 2:

                            Log.d(TAG, "NORMAL GO");

                            Intent i2 = new Intent(getApplicationContext(), NormalPlayActivity.class);
                            i2.putExtra("music", music);
                            startActivity(i2);
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in);
                            finish();

                            break;
                        case 3:

                            Log.d(TAG, "HARD GO");

                            Intent i3 = new Intent(getApplicationContext(), HardPlayActivity.class);
                            i3.putExtra("music", music);
                            startActivity(i3);
                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in);
                            finish();

                            break;
                    }

                }else{
                    //WAIT...
                }

            }
        });


        //SHADOW EFFECT SETTING
        shadowEffect.setVisibility(View.GONE);
        shadowEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab_menu.collapse();
            }
        });


        //COUNT SETTNG
        countView.setText(String.valueOf(RandomUtils.getRandomIntNum(9,125)));


        //**************FAB MENU SETTINGS
        FloatingActionButton actionA = new FloatingActionButton(getBaseContext());
        actionA.setSize(FloatingActionButton.SIZE_MINI);
        actionA.setIcon(R.drawable.ic_runner_community);
        actionA.setColorNormal(getResources().getColor(R.color.colorPrimary));
        actionA.setColorPressed(getResources().getColor(R.color.colorGreyTrans30));
        actionA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse("https://www.facebook.com/RUNNERS000");
                intent.setData(uri);
                startActivity(intent);

            }
        });
        FloatingActionButton actionB = new FloatingActionButton(getBaseContext());
        actionB.setSize(FloatingActionButton.SIZE_MINI);
        actionB.setIcon(R.drawable.main_asset_challenge_fab);
        actionB.setColorNormal(getResources().getColor(R.color.colorAccent));
        actionB.setColorPressed(getResources().getColor(R.color.colorGreyTrans30));
        actionB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this, ChallengeActivity.class);
                i.putExtra("uid", uid);
                i.putExtra("ticket", ticket);
                startActivity(i);
                finish();

            }
        });
        FloatingActionButton actionC = new FloatingActionButton(getBaseContext());
        actionC.setSize(FloatingActionButton.SIZE_MINI);
        actionC.setIcon(R.drawable.ic_setting);
        actionC.setColorNormal(getResources().getColor(R.color.colorAccent));
        actionC.setColorPressed(getResources().getColor(R.color.colorGreyTrans30));
        actionC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(difficulty != 0) {

                    Intent i = new Intent(MainActivity.this, SettingOneAcitivity.class);
                    i.putExtra("uid", uid);
                    i.putExtra("difficulty", difficulty);
                    startActivity(i);
                    finish();

                }else{
                    //WAIT...
                }

            }
        });


        //FAB SETTING
        fab_menu.addButton(actionA);fab_menu.addButton(actionB);fab_menu.addButton(actionC);

        fab_menu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                shadowEffect.setVisibility(View.VISIBLE);
                countLayout.setVisibility(View.VISIBLE);
                playButton.setClickable(FALSE);
            }

            @Override
            public void onMenuCollapsed() {

                shadowEffect.setVisibility(View.GONE);
                countLayout.setVisibility(View.GONE);
                playButton.setClickable(TRUE);
            }
        });

        //**************FAB MENU SETTINGS END


        //SOUNDBAR SETTING

        if(music == 1){

            mp = MediaPlayer.create(this, R.raw.hyper_roof);
            musicView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.main_asset_music_text_1));
        } else if (music == 2) {

            mp = MediaPlayer.create(this, R.raw.indicator_1);
            musicView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.main_asset_music_text_2));

        } else {

            //Default
            mp = MediaPlayer.create(this, R.raw.hyper_roof);
            musicView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.main_asset_music_text_1));
        }

        musicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!mp.isPlaying()) {

                    //NEXT MUSIC
                    if (music == 1) {

                        music = 2;
                        mp = MediaPlayer.create(MainActivity.this, R.raw.indicator_1);
                        musicView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_asset_music_text_2));

                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putInt("music", music);
                        editor.commit();

                    } else if (music == 2) {

                        music = 1;
                        mp = MediaPlayer.create(MainActivity.this, R.raw.hyper_roof);
                        musicView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_asset_music_text_1));

                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putInt("music", music);
                        editor.commit();

                    }
                } else {

                }

            }
        });

        musicPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mp.isPlaying()){

                    musicPlayIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_asset_music_button_2));
                    mp.pause();
                    shakeSoundBarStop();

                } else {

                    musicPlayIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.main_asset_music_stop_button));
                    mp.start();
                    shakeSoundBar();

                }
            }
        });

        musicBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mp.isPlaying()){
                    mp.seekTo(0);
                    mp.start();
                }else{
                    mp.seekTo(0);
                }

            }
        });

        if(music == 0){
            musicMuteText.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }else {
            musicMuteText.setTextColor(ContextCompat.getColor(this, R.color.colorGrey));
        }

        musicMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(music == 0){
                    musicMuteText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorGrey));
                    music = 1;

                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt("music", music);
                    editor.commit();

                }else {
                    musicMuteText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
                    music = 0;

                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt("music", music);
                    editor.commit();
                }

            }
        });

    }

    public void displayCheck(){

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

        if( deviceWidth > 1081){

            setContentView(R.layout.activity_main);

        }else{

            setContentView(R.layout.activity_main);

        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }


    public void getOption(final String uid){

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                AppLinks.URL_EXPORT_USER_OPTION+uid, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());

                try {
                    // Parsing json object response
                    // response will be a json object

                   JSONObject user = response.getJSONObject("user");

                   String diff = user.getString("difficulty");
                   String tic = user.getString("ticket");;

                   difficulty = Integer.valueOf(diff);
                   ticket = Integer.valueOf(tic);

                   if(mPref.getInt("difficulty", 1) != difficulty){

                       difficulty = mPref.getInt("difficulty", 1);
                       updateDiff(difficulty);

                   }

                   if(mPref.getInt("ticket", 0) > ticket ){

                        ticket = mPref.getInt("ticket", 0);
                        updateTicket(ticket);

                    }else if(mPref.getInt("ticket", 0) < ticket ) {

                       SharedPreferences.Editor editor = mPref.edit();
                       editor.putInt("ticket", ticket);
                       editor.commit();

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
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

    public void updateDiff(final int difficulty){

            String url = AppLinks.URL_CAHNGE_USER_DIFF + uid + "&difficulty=" + String.valueOf(difficulty);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    Log.d("TAG", response.toString());
                    Log.d("TAG", "difficulty="+String.valueOf(difficulty));

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d("TAG", "Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq);

    }

    private void updateTicket(final int ticket){

            String url = AppLinks.URL_GET_USER_TICKET + uid + "&ticket=" + String.valueOf(ticket);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    Log.d(TAG, response.toString());
                    Log.d(TAG, "SUCCESS : ONLINE");

                    /*
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt("ticket", 0);
                    editor.commit();
                    */


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

    private void shakeSoundBar(){

        Animation shake;
        shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse);

        soundBar.startAnimation(shake); // starts animation

    }

    private void shakeSoundBarStop(){

        soundBar.clearAnimation();

    }

    private void setPDialog(){

        if(pDialog != null) {

            pDialog.setTitle("현재 인터넷 연결이 되어있지 않습니다.")
                    .setMessage("데이터 불러오기 및 저장이 원활히 진행되지 않을 수 있습니다.")
                    .addButton("캐시 삭제시 오프라인 데이터 삭제됨",
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
