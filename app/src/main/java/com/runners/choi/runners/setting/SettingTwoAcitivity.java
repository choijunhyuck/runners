package com.runners.choi.runners.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.runners.choi.runners.LoginActivity;
import com.runners.choi.runners.MainActivity;
import com.runners.choi.runners.R;
import com.runners.choi.runners.record.RecordActivity;
import com.runners.choi.runners.app.AppController;
import com.runners.choi.runners.imageview.ProfileNetworkImageView;

import java.util.Calendar;

public class SettingTwoAcitivity extends AppCompatActivity {

    private SharedPreferences userPref;

    private RelativeLayout background;

    private ProfilePictureView profileImgView;
    private ImageLoader imageLoader;

    private String uid;
    private String email;
    private String name;

    private TextView userNameView;

    private ImageView logoutButton;
    private ImageView recordButton;
    private ImageView settingEndButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setting_second);

        //GET USER INFORMATION
        userPref =  PreferenceManager.getDefaultSharedPreferences(this);

        uid = userPref.getString("uid", "none");
        email = userPref.getString("email", "none");
        name = userPref.getString("name", "none");

        //DAY & NIGHT SETTING
        Calendar calendar = Calendar.getInstance( );
        int time = calendar.get((Calendar.HOUR_OF_DAY));

        background = (RelativeLayout)
                findViewById(R.id.setting_2_background);
        //NIGHT, NOON, MORNING
        if(time < 7 || time > 18 ){
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_night));
        } else if(time > 12) {
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_noon));
        } else{
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen));
        }


        //PROFILE IMAGE SETTING

        profileImgView = (ProfilePictureView)
                findViewById(R.id.setting_profile_img);
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        profileImgView.setProfileId(uid);

        //TEXTVIEW INITIALIZING & SET

        userNameView = (TextView)
                findViewById(R.id.user_name_view);
        userNameView.setText(name);

        //BUTTONS INITIALIZING
        logoutButton = (ImageView)
                findViewById(R.id.setting_2_logout_button);
        recordButton = (ImageView)
                findViewById(R.id.setting_2_record_button);
        settingEndButton = (ImageView)
                findViewById(R.id.setting_2_end_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = userPref.edit();
                editor.clear().commit();

                LoginManager.getInstance().logOut();

                if(SettingOneAcitivity.activitySetting != null){ //액티비티가 살아 있다면

                    SettingOneAcitivity activity = (SettingOneAcitivity) SettingOneAcitivity.activitySetting;
                    activity.finish();

                    startActivity(new Intent(SettingTwoAcitivity.this, LoginActivity.class));
                    finish();

                } else {

                    startActivity(new Intent(SettingTwoAcitivity.this, LoginActivity.class));
                    finish();
                }
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(SettingTwoAcitivity.this, RecordActivity.class));

            }
        });

        settingEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(SettingOneAcitivity.activitySetting != null){ //액티비티가 살아 있다면

                    SettingOneAcitivity activity = (SettingOneAcitivity) SettingOneAcitivity.activitySetting;
                    activity.finish();

                    startActivity(new Intent(SettingTwoAcitivity.this, MainActivity.class));
                    finish();

                } else {

                    startActivity(new Intent(SettingTwoAcitivity.this, MainActivity.class));
                    finish();
                }

            }
        });

    }
}
