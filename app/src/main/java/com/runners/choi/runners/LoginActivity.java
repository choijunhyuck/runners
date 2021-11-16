package com.runners.choi.runners;

        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageInfo;
        import android.content.pm.PackageManager;
        import android.content.pm.Signature;
        import android.net.ConnectivityManager;
        import android.support.v4.content.ContextCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Base64;
        import android.util.DisplayMetrics;
        import android.util.Log;
        import android.view.View;
        import android.widget.ImageView;
        import android.widget.RelativeLayout;
        import android.widget.Toast;

        import com.android.volley.AuthFailureError;
        import com.android.volley.Request;
        import com.android.volley.Response;
        import com.android.volley.VolleyError;
        import com.android.volley.toolbox.StringRequest;
        import com.facebook.AccessToken;
        import com.facebook.CallbackManager;
        import com.facebook.FacebookCallback;
        import com.facebook.FacebookException;
        import com.facebook.GraphRequest;
        import com.facebook.GraphResponse;
        import com.facebook.appevents.AppEventsLogger;
        import com.facebook.login.Login;
        import com.facebook.login.LoginManager;
        import com.facebook.login.LoginResult;
        import com.facebook.login.widget.LoginButton;
        import com.runners.choi.runners.app.AppController;
        import com.runners.choi.runners.app.AppLinks;
        import com.runners.choi.runners.disposable.dMainActivity;

        import org.json.JSONException;
        import org.json.JSONObject;

        import java.net.InetAddress;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;
        import java.util.Arrays;
        import java.util.Calendar;
        import java.util.HashMap;
        import java.util.Map;

        import libs.mjn.prettydialog.PrettyDialog;
        import libs.mjn.prettydialog.PrettyDialogCallback;

public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;

    //SCREEN
    private RelativeLayout background;
    private RelativeLayout welcomeScreen;

    //LOGIN BUTTONS
    private LoginButton facebookLoginButton;
    private ImageView mfacebookLoginButton;
    private ImageView disposableLoginButton;

    private ImageView exDisposableButton;

    private String TAG = "TAG.L";

    private int internetConnection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        if (AccessToken.getCurrentAccessToken() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }else{
            Log.d(TAG,"ACCESSTOKEN WASHOUT OR NO LOGIN");
        }

        if(isInternetAvailable()){
            internetConnection = 1;
        } else {
            internetConnection = 0;
        }

        //SCREEN SETTING
        background = (RelativeLayout)
                findViewById(R.id.login_background);

        welcomeScreen = (RelativeLayout)
                findViewById(R.id.welcome_screen);


        //TIME SETTING
        Calendar calendar = Calendar.getInstance( );
        int time = calendar.get((Calendar.HOUR_OF_DAY));

        //NIGHT, NOON, MORNING
        if(time < 7 || time > 18 ){
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_night));
        } else if(time > 12) {
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen_noon));
        } else{
            background.setBackground(ContextCompat.getDrawable(this, R.drawable.main_screen));
        }


        //DISPOSABLE EXPLAIN SETTING
        exDisposableButton = (ImageView)
                findViewById(R.id.ex_disposable_button);
        exDisposableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PrettyDialog(LoginActivity.this)
                        .setTitle("What is Disposable?")
                        .setMessage("회원가입 없는 1회성 로그인입니다.")
                        .addButton("난이도 변경 불가",
                                R.color.colorAccent,
                                R.color.colorPrimary,
                                new PrettyDialogCallback() {
                            @Override
                            public void onClick() {

                            }
                        })
                        .addButton("세션 저장 불가",
                                R.color.colorAccent,
                                R.color.colorPrimary,
                                new PrettyDialogCallback() {
                                    @Override
                                    public void onClick() {

                                    }
                                })
                        .show();
            }
        });


        //LOGIN SETTINGS
        callbackManager = CallbackManager.Factory.create();
        facebookLoginButton =
                findViewById(R.id.facebook_login_button);
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        AppEventsLogger.activateApp(getApplication());

        mfacebookLoginButton = (ImageView)
                findViewById(R.id.login_with_facebook_button);
        mfacebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(internetConnection == 1) {
                    facebookLoginButton.performClick();
                } else {
                    Toast.makeText(LoginActivity.this, "인터넷 연결을 확인하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        disposableLoginButton = (ImageView)
                findViewById(R.id.login_with_disposable_button);
        disposableLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginActivity.this, dMainActivity.class));
                finish();


            }
        });


        //FACEBOOK LOGIN SETTINGS
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        welcomeScreen.setVisibility(View.VISIBLE);
                        mfacebookLoginButton.setClickable(false);
                        disposableLoginButton.setClickable(false);

                        Log.i(TAG, "User ID: " + loginResult.getAccessToken().getUserId());
                        Log.i(TAG, "Auth Token: " + loginResult.getAccessToken().getToken());

                        getFacebookProfile(AccessToken.getCurrentAccessToken());

                    }

                    @Override
                    public void onCancel() {

                        // App code

                        Log.d(TAG,"onCancel");
                    }

                    @Override
                    public void onError(FacebookException exception) {

                        // App code

                        welcomeScreen.setVisibility(View.GONE);
                        Log.d(TAG,"onError :" + exception.toString());
                    }
                });

    }
    //ONCREATE FUNCTION ENDPOINT


    public void registerFacebookUser(final String uid, final String email, final String name, final String gender,
                                     final String link, final String profile_img) {

        String tag_string_req = "request_register";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppLinks.URL_REGISTER, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        Intent i = new Intent(LoginActivity.this, MainActivity.class);

                        i.putExtra("uid", uid);
                        i.putExtra("email", email);
                        i.putExtra("name", name);
                        i.putExtra("gender", gender);
                        i.putExtra("link", link);
                        i.putExtra("profile_img", profile_img);

                        startActivity(i);
                        finish();

                    } else {

                        String errorMsg = jObj.getString("error_msg");
                        Log.d("TAG", errorMsg);

                        if (errorMsg.substring(0, 4).equals("User")) {
                            Log.d("TAG", "이미 등록된 FACEBOOK INFORMATION");

                                Intent i = new Intent(LoginActivity.this, MainActivity.class);

                                i.putExtra("uid", uid);
                                i.putExtra("email", email);
                                i.putExtra("name", name);
                                i.putExtra("gender", gender);
                                i.putExtra("link", link);
                                i.putExtra("profile_img", profile_img);

                                startActivity(i);
                                finish();


                        } else {
                            Log.d("TAG", errorMsg.substring(0, 4));
                            Toast.makeText(getApplicationContext(),
                                    "다시 한번 시도해주세요", Toast.LENGTH_LONG).show();
                            welcomeScreen.setVisibility(View.GONE);
                        }

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

                LoginManager.getInstance().logOut();
                welcomeScreen.setVisibility(View.GONE);
                facebookLoginButton.setClickable(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("uid", uid);
                params.put("email", email);
                params.put("name", name);
                params.put("gender", gender);
                params.put("link", link);
                params.put("profile_img", profile_img);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    public void getFacebookProfile(final AccessToken token) {

        GraphRequest request = GraphRequest.newMeRequest(token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        Log.d(TAG, "페이스북 로그인 결과" + response.toString());

                        try {
                            String uid = object.getString("id");
                            String email = object.getString("email");
                            String name = object.getString("name");         // 이름
                            String profile_img = "https://graph.facebook.com/" + uid + "/picture?type=square";
                            //FULL SIZE = String profile_img = "https://graph.facebook.com/" + uid + "/picture?width=9999;

                            String gender = "deprecated";
                            String link = "deprecated";     // 링크

                            Log.d("TAG", "페이스북 구별아이디 -> " + uid);
                            Log.d("TAG", "페이스북 이메일 -> " + email);
                            Log.d("TAG", "페이스북 이름 -> " + name);
                            Log.d("TAG", "페이스북 사진 -> " + profile_img);

                            if(internetConnection == 1){
                                registerFacebookUser(uid, email, name, gender, link, profile_img);
                            }else{
                                Toast.makeText(LoginActivity.this, "인터넷 연결이 원활하지 않습니다. 재시도 해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                                LoginManager.getInstance().logOut();
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, e.getMessage());
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,name,gender,link");
        request.setParameters(parameters);
        request.executeAsync();

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
