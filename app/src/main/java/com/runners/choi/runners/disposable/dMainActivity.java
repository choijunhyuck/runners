package com.runners.choi.runners.disposable;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.runners.choi.runners.MainActivity;
import com.runners.choi.runners.R;
import com.runners.choi.runners.SafeFrameActivity;
import com.runners.choi.runners.guide.GuideActivity;
import com.runners.choi.runners.utils.RandomUtils;

import java.util.Calendar;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class dMainActivity extends AppCompatActivity {

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
    private RelativeLayout soundBar;
    private ImageView musicView;
    private ImageView musicPlayIcon;
    private TextView musicPlay;
    private TextView musicBack;
    private LinearLayout musicMute;
    private TextView musicMuteText;
    private int music = 0;

    private MediaPlayer mp;

    private String TAG = "TAG.DM";

    private final Handler delayHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //GET USER INFORMATION

        displayCheck();


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
        soundBar = (RelativeLayout)
                findViewById(R.id.sound_bar_background);
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


        Calendar calendar = Calendar.getInstance( );
        int time = calendar.get((Calendar.HOUR_OF_DAY));

        if(time < 7 || time > 19 ){
            /*NIGHT*/
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_night));
            safeFrameTap.setVisibility(View.VISIBLE);

        } else {
            /*DAY*/
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen));
            safeFrameTap.setVisibility(View.INVISIBLE);

        }

        //TEXT 4 SETTING (IS FIRST OR SAFETY FRAME)

        checkPop.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.main_asset_guide_pop));
        checkPop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    startActivity(new Intent(dMainActivity.this, GuideActivity.class));
                    finish();

                }
            });


        //TAPS & POPS SET FUNCTIONS

        safeFramePop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(dMainActivity.this, SafeFrameActivity.class));
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

                Intent i = new Intent(getApplicationContext(), dPlayActivity.class);
                i.putExtra("music", music);

                startActivity(i);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in);
                finish();

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


        //FAB MENU SETTING
        fab_menu.addButton(actionA);

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

        mp = MediaPlayer.create(this, R.raw.hyper_roof);

        musicPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mp.isPlaying()){

                    musicPlayIcon.setImageDrawable(ContextCompat.getDrawable(dMainActivity.this, R.drawable.main_asset_music_button_2));
                    mp.pause();
                    shakeSoundBarStop();

                } else {

                    musicPlayIcon.setImageDrawable(ContextCompat.getDrawable(dMainActivity.this, R.drawable.main_asset_music_stop_button));
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

        //SOUND BAR SETTING
        musicMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(music == -1){
                    musicMuteText.setTextColor(ContextCompat.getColor(dMainActivity.this, R.color.colorGrey));
                    music = 0;


                }else {
                    musicMuteText.setTextColor(ContextCompat.getColor(dMainActivity.this, R.color.colorPrimary));
                    music = -1;

                }

                Log.d(TAG, String.valueOf(music));

            }
        });

    }

    private void shakeSoundBar(){

        Animation shake;
        shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse);

        soundBar.startAnimation(shake); // starts animation

    }

    private void shakeSoundBarStop(){

        soundBar.clearAnimation();

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

        if( deviceWidth > 1200){

            setContentView(R.layout.activity_main_d);

        }else{

            setContentView(R.layout.activity_main_d);

        }
    }

}
