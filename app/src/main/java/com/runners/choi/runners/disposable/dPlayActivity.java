package com.runners.choi.runners.disposable;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.runners.choi.runners.R;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;


public class dPlayActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //PUT INTENTS
    private int timeAmount = 0;
    private int bonusTimeAmount = 0;
    private int speedMax = 0;
    private int distanceAmount = 0;
    private int pointAmount = 0;


    //GAMEPLAY LOCATION
    private double amountLatitude = 0;
    private double amountLongitude = 0;

    private double standardLatitude = 0;
    private double standardLongitude = 0;

    private double lastLatitude = 0;
    private double lastLongitude = 0;
    //END

    //*****GAMEPLAY COMPONENTS

    private int firstcheck = 0;
    private int timerFirstcheck = 0;

    private int delay_seconds = 7;

    private int repeat_count = 0;
    private int repeat_count_temp = 0;

    private int secondRemaining = 0;

    private int speed = 0;

    private Runnable run;
    private final Handler delayHandler = new Handler();
    private final Handler handler = new Handler();

    private static TimerTask timerTask;

    private LinearLayout safetyFrame;

    private RelativeLayout shadowEffct;
    private TextView delayView;
    private TextView leadRunView;

    private TextView timerTextView;
    private TextView statusView;
    private TextView statusDetailView;
    private TextView speedView;

    private ImageView stop_play_button;

    private ImageView guageView;

    private  Circle mapCircle;

    private PrettyDialog pDialog;
    private int isFirstDialog = 0;


    /*music*/
    private MediaPlayer mp;
    private int music;
    //*****END


    //MAP SETTING
    private GoogleApiClient mGoogleApiClient = null;
    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 0.5ch
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000; // 0.5초

    private AppCompatActivity mActivity;
    private boolean askPermissionOnceAgain = false;
    private boolean mRequestingLocationUpdates = false;
    private Location mCurrentLocatiion;
    private boolean mMoveMapByUser = true;
    private boolean mMoveMapByAPI = true;
    private LatLng currentPosition;
    //END


    private LocationRequest locationRequest = new LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.acitivity_play);

        Log.d(TAG, "onCreate");
        mActivity = this;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.play_map);
        mapFragment.getMapAsync(this);

        Intent i = getIntent();
        music = i.getIntExtra("music", 0);

        //DAY & NIGHT CHECK
        safetyFrame = (LinearLayout)
                findViewById(R.id.safety_frame);

        Calendar calendar = Calendar.getInstance( );
        int time = calendar.get((Calendar.HOUR_OF_DAY));

        if(time < 7 || time > 18 ){
            safetyFrame.setVisibility(View.VISIBLE);
        } else {
            safetyFrame.setVisibility(View.GONE);
        }


        //DIALOG SETTING
        pDialog = new PrettyDialog(this);


        //DELAY VIEW SETTING
        shadowEffct = (RelativeLayout)
                findViewById(R.id.shadow_effect_play);
        delayView = (TextView)
                findViewById(R.id.delay_view);
        leadRunView = (TextView)
                findViewById(R.id.lead_to_run);


        //ABOUT GAME COMPONETS SETTINGS
        timerTextView = (TextView)
                findViewById(R.id.timer);
        statusView = (TextView)
                findViewById(R.id.status_view);
        statusDetailView = (TextView)
                findViewById(R.id.status_detail_view);
        speedView = (TextView)
                findViewById(R.id.speed_view);
        stop_play_button = (ImageView)
                findViewById(R.id.stop_button);
        guageView = (ImageView)
                findViewById(R.id.guage_view);
        //


        //GAMEOVER
        stop_play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(delay_seconds < 1) {

                    musicStop();
                    timerTask.cancel();

                    final Toast toast = Toast.makeText(getApplicationContext(), "GAME OVER!", Toast.LENGTH_LONG);
                    toast.show();

                    Intent i = new Intent(dPlayActivity.this, dResultActivity.class);
                    i.putExtra("repeat", repeat_count);
                    i.putExtra("point", "0");
                    i.putExtra("time", timeAmount);
                    i.putExtra("bonus", bonusTimeAmount);
                    i.putExtra("speed", speedMax);
                    i.putExtra("distance", distanceAmount);
                    i.putExtra("point", pointAmount);
                    startActivity(i);
                    finish();
                } else {

                    delayHandler.removeCallbacks(run);
                    Toast.makeText(mActivity, "GPS 좌표를 확인할 수 없습니다 : 와이파이 연결을 권장합니다", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(dPlayActivity.this, dMainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });

    }

    public TimerTask timerManager() {

        TimerTask timerTaskManager = new TimerTask() {

            @Override
            public void run() {

                secondRemaining--;
                timeAmount += 1;

                //*****GAMEOVER
                if(secondRemaining == 0 ){

                    musicStop();

                    timerTask.cancel();
                    runOnUiThread(new Runnable() {
                        public void run() {

                            final Toast toast = Toast.makeText(getApplicationContext(), "GAME OVER!", Toast.LENGTH_LONG);
                            toast.show();

                            Intent i = new Intent(dPlayActivity.this, dResultActivity.class);
                            i.putExtra("repeat", repeat_count);
                            i.putExtra("point", "0");
                            i.putExtra("time", timeAmount);
                            i.putExtra("bonus", bonusTimeAmount);
                            i.putExtra("speed", speedMax);
                            i.putExtra("distance", distanceAmount);
                            i.putExtra("point", pointAmount);
                            startActivity(i);
                            finish();

                        }
                    });
                } else {
                    Update();
                }
                //*****

            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTaskManager, 0, 1000);

        return timerTaskManager;
    }

    protected void Update() {

        Runnable updater = new Runnable() {
            public void run() {

                timerTextView.setText(String.valueOf(secondRemaining));
            }
        };
        handler.post(updater);

    }


    @Override
    public void onResume() {

        super.onResume();

        if (mGoogleApiClient.isConnected()) {

            Log.d(TAG, "onResume : call startLocationUpdates");
            if (!mRequestingLocationUpdates) startLocationUpdates();
        }


        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;

                checkPermissions();
            }
        }
    }


    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            Log.d(TAG, "startLocationUpdates : call FusedLocationApi.requestLocationUpdates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            mRequestingLocationUpdates = true;

            mGoogleMap.setMyLocationEnabled(false);

        }

    }



    private void stopLocationUpdates() {

        Log.d(TAG,"stopLocationUpdates : LocationServices.FusedLocationApi.removeLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "onMapReady :");

        mGoogleMap = googleMap;


        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();
        mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){

            @Override
            public boolean onMyLocationButtonClick() {

                Log.d( TAG, "onMyLocationButtonClick : 위치에 따른 카메라 이동 활성화");
                mMoveMapByAPI = true;
                return true;
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d( TAG, "onMapClick :");
            }
        });

        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {

            @Override
            public void onCameraMoveStarted(int i) {

                if (mMoveMapByUser == true && mRequestingLocationUpdates){

                    Log.d(TAG, "onCameraMove : 위치에 따른 카메라 이동 비활성화");
                    mMoveMapByAPI = false;
                }

                mMoveMapByUser = true;

            }
        });


        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {

            @Override
            public void onCameraMove() {


            }
        });
    }


    @Override
    public void onLocationChanged(Location location) {

        Log.d("TAG", "point : " + String.valueOf(pointAmount));

            //CURRENT POSITION SETTING
            currentPosition
                = new LatLng( location.getLatitude(), location.getLongitude());

            Log.d(TAG, "onLocationChanged : ");

            String markerTitle = getCurrentAddress(currentPosition);
            String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                + " 경도:" + String.valueOf(location.getLongitude());
            //END

        //BECAUSE FIRST LOCATION IS SEOUL, AND MY LOCATION IS VERY FAR!!!****
        if(repeat_count != 0) {

            //SPEED SETTINGS
            Location lastLocation = new Location("last");
            lastLocation.setLatitude(lastLatitude);
            lastLocation.setLongitude(lastLongitude);

            speed = (int) (location.distanceTo(lastLocation) * 3.6);
            speedSetting(speed);

            //GET MAX SPEED
            if (speedMax < speed && repeat_count != 0) {
                speedMax = speed;
            }
            //END


            //GET DISTANCE AMOUNT
            if(amountLatitude == 0 && amountLongitude == 0) {

                amountLatitude = standardLatitude;
                amountLongitude = standardLongitude;

            } else {
                Location amountLocation = new Location("amount");
                amountLocation.setLatitude(amountLatitude);
                amountLocation.setLongitude(amountLongitude);

                distanceAmount += location.distanceTo(amountLocation);

                amountLatitude = location.getLatitude();
                amountLongitude = location.getLongitude();
            }
            //END


            //100M CHECK
            Location standardLocation = new Location("standard");
            standardLocation.setLatitude(standardLatitude);
            standardLocation.setLongitude(standardLongitude);

            if (location.distanceTo(standardLocation) > 100) {

                repeat_count += 1;
                Toast.makeText(getApplicationContext(), String.valueOf(repeat_count), Toast.LENGTH_SHORT).show();
            }
            //END
        }

            //SET MARKER AND MOVE
            setCurrentLocation(location, markerTitle, markerSnippet);
            mCurrentLocatiion = location;

    }

    private void speedSetting(int speed){

            speedView.setText(String.valueOf(speed) +"Km/h");


            //VIEW SETTING & POINT
            if(speed > 0 && speed < 11){
                pointAmount += 100;
                guageView.setImageResource(R.drawable.guage_1);
                speedView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
            }else if(speed > 10 && speed < 21){
                pointAmount += 150;
                guageView.setImageResource(R.drawable.guage_2);
                speedView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
            }else if(speed > 20 && speed < 31){
                pointAmount += 200;
                guageView.setImageResource(R.drawable.guage_3);
                speedView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));
            }else{
                pointAmount += 250;
                guageView.setImageResource(R.drawable.guage_4);
                speedView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            }
            //END


            //BONUS SETTING
            if(speed > 30){

                statusDetailView.setText("시속보너스 1+");
                statusDetailView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAchieve));
                secondRemaining += 1;
                bonusTimeAmount += 1;

            }else if(speed <30 && speed > 0 && repeat_count %3 != 0){

                statusDetailView.setText("보너스 없음");
                statusDetailView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));

            } else if(speed <30 && speed > 0 && repeat_count %3 == 0 && repeat_count != 0){

                statusDetailView.setText("오차 보너스 10+");
                statusDetailView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAchieve));
            }
            //END

    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        pDialog.dismiss();

        lastLatitude = location.getLatitude();
        lastLongitude = location.getLongitude();

        mMoveMapByUser = false;

        if (currentMarker != null) currentMarker.remove();


        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        //구글맵의 디폴트 현재 위치는 파란색 동그라미로 표시
        //마커를 원하는 이미지로 변경하여 현재 위치 표시하도록 수정 fix - 2017. 11.27
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_runner_community));

        currentMarker = mGoogleMap.addMarker(markerOptions);
        currentMarker.remove();


        if ( mMoveMapByAPI ) {

            Log.d( TAG, "setCurrentLocation :  mGoogleMap moveCamera "
                    + location.getLatitude() + " " + location.getLongitude() ) ;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16);
            mGoogleMap.moveCamera(cameraUpdate);
        }

        Log.d("TAG", String.valueOf("달린시간 " + timeAmount));
        Log.d("TAG", String.valueOf("보너스시간 " + bonusTimeAmount));
        Log.d("TAG", String.valueOf("속력 " + String.valueOf(speedMax)));
        Log.d("TAG", String.valueOf("총거리 " + distanceAmount));

        if(repeat_count == 0){

            //FIRST RUN CHECK
            if(firstcheck == 1) {

                standardLatitude = currentLatLng.latitude;
                standardLongitude = currentLatLng.longitude;

                makeCircle(currentLatLng);

                repeat_count_temp = repeat_count = 1;
                leadRunView.setVisibility(View.GONE);
            } else {

                secondRemaining = 25;

                timerTextView.setText(String.valueOf(secondRemaining));

                //TIMER REACTIVE PREVENTION
                if (timerFirstcheck == 0) {

                    run = new Runnable() {
                        @Override
                        public void run() {

                            delay_seconds--;
                            delayView.setText(String.valueOf(delay_seconds));
                            if (delay_seconds > 0) {
                                delayHandler.postDelayed(this, 1000);
                            } else {

                                shadowEffct.setVisibility(View.GONE);
                                leadRunView.setVisibility(View.VISIBLE);
                                timerTask = timerManager();
                                firstcheck = 1;

                                musicPlay();
                            }

                        }
                    };

                    delayHandler.postDelayed(run, 1000);


                    timerFirstcheck = 1;

                }
            }

        } else if(repeat_count > 0 && repeat_count_temp != repeat_count){

            //POINT
            pointAmount += repeat_count*200;

            if (repeat_count % 3 == 0) {

                //POINT
                pointAmount += 1000;

                statusDetailView.setText("오차 보너스 10+");
                statusDetailView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAchieve));

                secondRemaining += 10;
                bonusTimeAmount += 10;

            } else {

                statusDetailView.setText("보너스 없음");
                statusDetailView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorBlack));

            }

            standardLatitude = currentLatLng.latitude;
            standardLongitude = currentLatLng.longitude;

            removeCircle();
            makeCircle(currentLatLng);

            secondRemaining += 25;

            repeat_count_temp = repeat_count;

            statusView.setText("REPEAT " + String.valueOf(repeat_count));
        }

    }

    public void makeCircle(LatLng currentLatLng){
        CircleOptions circle = new CircleOptions().center(currentLatLng) //원점
                .radius(100)      //반지름 단위 : m
                .strokeWidth(0f)  //선너비 0f : 선없음
                .fillColor(getResources().getColor(R.color.colorPrimaryTrans35)); //배경색
        mGoogleMap.addCircle(circle);

        mapCircle = mGoogleMap.addCircle(circle);
    }

    public void removeCircle(){

        if(mapCircle!=null){
            mGoogleMap.clear();
            Toast.makeText(getApplicationContext(), "hi..", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStart() {

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected() == false){

            Log.d(TAG, "onStart: mGoogleApiClient connect");
            mGoogleApiClient.connect();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {

        if (mRequestingLocationUpdates) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            stopLocationUpdates();
        }

        if ( mGoogleApiClient.isConnected()) {

            Log.d(TAG, "onStop : mGoogleApiClient disconnect");
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }


    @Override
    public void onConnected(Bundle connectionHint) {


        if ( mRequestingLocationUpdates == false ) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {

                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                } else {

                    Log.d(TAG, "onConnected : 퍼미션 가지고 있음");
                    Log.d(TAG, "onConnected : call startLocationUpdates");
                    startLocationUpdates();
                    mGoogleMap.setMyLocationEnabled(true);
                }

            }else{

                Log.d(TAG, "onConnected : call startLocationUpdates");
                startLocationUpdates();
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed");
        setDefaultLocation();
    }


    @Override
    public void onConnectionSuspended(int cause) {

        Log.d(TAG, "onConnectionSuspended");
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }


    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Log.d(TAG, "인터넷 연결이 안되었거나 그 문제가 있음 : 지오코더");
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Log.d("TAG", "주소 미발견");
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void setDefaultLocation() {

        mMoveMapByUser = false;


        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 여부를 확인하세요";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);

    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat
                .shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager
                .PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("게임을 플레이하려면 위치정보사용에 동의해야합니다.");

        else if (hasFineLocationPermission
                == PackageManager.PERMISSION_DENIED && !fineLocationRationale) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "번거로우시더라도 설정에서 위치정보사용을 허가해주세요!.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {


            Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");

            if ( mGoogleApiClient.isConnected() == false) {

                Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (permsRequestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {

            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (permissionAccepted) {


                if ( mGoogleApiClient.isConnected() == false) {

                    Log.d(TAG, "onRequestPermissionsResult : mGoogleApiClient connect");
                    mGoogleApiClient.connect();
                }



            } else {

                checkPermissions();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        pDialog.dismiss();

        if(isFirstDialog == 0) {

            isFirstDialog = 1;
            pDialog.setTitle("위치 정보 사용 동의")
                    .setTitleColor(R.color.pdlg_color_blue)
                    .setIcon(
                            R.drawable.pdlg_icon_info,     // icon resource
                            R.color.pdlg_color_blue,      // icon tint
                            new PrettyDialogCallback() {   // icon OnClick listener
                                @Override
                                public void onClick() {
                                    // Do what you gotta do
                                }
                            })
                    .setMessage(msg)
                    .addButton("동의",
                            R.color.colorAccent,
                            R.color.pdlg_color_green,
                            new PrettyDialogCallback() {
                                @Override
                                public void onClick() {
                                    ActivityCompat.requestPermissions(mActivity,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                                }
                            })
                    .addButton("거부",
                            R.color.colorAccent,
                            R.color.colorPrimary,
                            new PrettyDialogCallback() {
                                @Override
                                public void onClick() {
                                    finish();
                                    startActivity(new Intent(dPlayActivity.this, dMainActivity.class));
                                }
                            });
        }
        pDialog.show();


    }

    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(dPlayActivity.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                askPermissionOnceAgain = true;

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(dPlayActivity.this);
        builder.setTitle("경고 : 위치 서비스 비활성화");
        builder.setMessage("러너를 사용하기 위해서는 위치 정보 사용에 동의해야합니다.\n"
                + "위치 정보 사용 설정을 변경하시겠어요?:D");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : 퍼미션 가지고 있음");


                        if ( mGoogleApiClient.isConnected() == false ) {

                            Log.d( TAG, "onActivityResult : mGoogleApiClient connect ");
                            mGoogleApiClient.connect();
                        }
                        return;
                    }
                }

                break;
        }
    }

    private void musicPlay(){

        if(music != -1){

            if(mp == null) {
                mp = MediaPlayer.create(this, R.raw.hyper_roof);
                mp.setLooping(true);
            }

            mp.start();

        }

    }

    private void musicStop(){

        Log.d(TAG, "stopped");

        if(music != -1){

            mp = null;

        }

    }

    @Override
    public void onBackPressed() {

        Animation shake;
        shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);

        ImageView image;
        image = (ImageView) findViewById(R.id.stop_button);

        image.startAnimation(shake); // starts animation

    }
}