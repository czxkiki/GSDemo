package com.dji.GSDemo.GaodeMap;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.SurfaceTexture;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import dji.common.camera.SystemState;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.product.Model;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;
import dji.ux.c.a;

import com.dji.GSDemo.GaodeMap.utils.ToastUtils;

import org.bouncycastle.LICENSE;

import static dji.common.mission.waypoint.WaypointActionType.START_TAKE_PHOTO;


public class MainActivity extends FragmentActivity implements View.OnClickListener,OnMapClickListener,SurfaceTextureListener {
    LinkedHashMap<String, Object> jobMap=new LinkedHashMap<>();

    protected static final String TAG = "MainActivity";
    protected VideoFeeder.VideoDataListener mReceivedVideoDataListener = null;
    // Codec for video live view
    protected DJICodecManager mCodecManager = null;
    protected TextureView mVideoSurface = null;
    private TextView recordingTime;

    private MapView mapView;
    private AMap aMap;
    //定义List名称
    public String Listname = null;
    //定义任务加载按钮
    private Button load, save;
    private Button locate, add, clear, Live;
    private Button config, upload, start, stop;
    //Live
    private String liveShowUrl = "rtmp://180.76.107.160:1935/live/123";
    //保存list地址
    private static final String URLSAVELIST = "http://180.76.107.160:8080/register/json/data";
    //设置增加按钮
    private Button set,Pause,Resume;
    //获取jobname
    public String URL1 = "http://180.76.107.160:8080/jobresluts/json/data";
    //获取joblist
    public String URL2 = "http://180.76.107.160:8080/Waypointlist";
    //joblist
    public String temp1;

    private boolean isAdd = false;

    private double droneLocationLat = 181, droneLocationLng = 181;
    private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<Integer, Marker>();
    private Marker droneMarker = null;

    private float altitude = 100.0f;
    private float mSpeed = 10.0f;

    private List<Waypoint> waypointList = new ArrayList<>();

    public static WaypointMission.Builder waypointMissionBuilder;
    private FlightController mFlightController;
    private WaypointMissionOperator instance;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;

    @Override
    protected void onResume(){
        super.onResume();
        initFlightController();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(mReceiver);
        removeListener();
        super.onDestroy();
    }

    /**
     * @Description : RETURN Button RESPONSE FUNCTION
     */
    public void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }

    private void setResultToToast(final String string){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initUI() {
        mVideoSurface = (TextureView)findViewById(R.id.picture);
        recordingTime = (TextView) findViewById(R.id.timer);
        locate = (Button) findViewById(R.id.locate);
        add = (Button) findViewById(R.id.add);
        clear = (Button) findViewById(R.id.clear);
        config = (Button) findViewById(R.id.config);
        upload = (Button) findViewById(R.id.upload);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        set = (Button) findViewById(R.id.set);
        Live = (Button) findViewById(R.id.Live);
        load = (Button) findViewById(R.id.load);
        save = (Button) findViewById(R.id.save);

        locate.setOnClickListener(this);
        add.setOnClickListener(this);
        clear.setOnClickListener(this);
        config.setOnClickListener(this);
        upload.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        set.setOnClickListener(this);
        Live.setOnClickListener(this);
        load.setOnClickListener(this);
        save.setOnClickListener(this);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

    }

    private void initMapView() {

        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setOnMapClickListener(this);// add the listener for click for amap object
        }

        LatLng lanzhou = new LatLng(36.093519, 103.714286);
        aMap.addMarker(new MarkerOptions().position(lanzhou).title("Marker in Lanzhou"));
        aMap.moveCamera(CameraUpdateFactory.newLatLng(lanzhou));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //转180度
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
        //全屏 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        initMapView();
        initUI();
        addListener();

        mReceivedVideoDataListener = new VideoFeeder.VideoDataListener() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

        Camera camera = DJIDemoApplication.getCameraInstance();

        if (camera != null) {

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemState) {
                    if (null != cameraSystemState) {

                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;

                        final String timeString = String.format("%02d:%02d", minutes, seconds);
                        final boolean isVideoRecording = cameraSystemState.isRecording();

                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                recordingTime.setText(timeString);

                                /*
                                 * Update recordingTime TextView visibility and mRecordBtn's check state
                                 */
                                if (isVideoRecording) {
                                    recordingTime.setVisibility(View.VISIBLE);
                                } else {
                                    recordingTime.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }
            });

        }
    }
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange()
    {
        initFlightController();
        loginAccount();
    }

    private void loginAccount(){

        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }
                    @Override
                    public void onFailure(DJIError error) {
                        setResultToToast("Login Error:"
                                + error.getDescription());
                    }
                });
    }

    private void initFlightController() {

        BaseProduct product = DJIDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {

            mFlightController.setStateCallback(
                    new FlightControllerState.Callback() {
                        @Override
                        public void onUpdate(FlightControllerState
                                                     djiFlightControllerCurrentState) {
                            droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                            droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                            updateDroneLocation();
                        }
                    });

        }
    }

    //Add Listener for WaypointMissionOperator
    private void addListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent) {

        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent) {

        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {

        }

        @Override
        public void onExecutionStart() {

        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            setResultToToast("Execution finished: " + (error == null ? "Success!" : error.getDescription()));
        }
    };

    public WaypointMissionOperator getWaypointMissionOperator() {
        if (instance == null) {
            instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        }
        return instance;
    }

    @Override
    public void onMapClick(LatLng point) {
        if (isAdd == true){
            markWaypoint(point);
            Waypoint mWaypoint = new Waypoint(point.latitude, point.longitude, altitude);
            //Add Waypoints to Waypoint arraylist;
            if (waypointMissionBuilder != null) {
                waypointList.add(mWaypoint);
                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
            }else
            {
                waypointMissionBuilder = new WaypointMission.Builder();
                waypointList.add(mWaypoint);
                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
            }
        }else{
            setResultToToast("Cannot Add Waypoint");
        }
    }
//增加 set方法 class FlightControllerState
//    public void setWaypointList(LatLng point) {
//        if (isAdd == true){
//            markWaypoint(point);
//            Waypoint mWaypoint = new Waypoint(point.latitude, point.longitude, altitude);
//            //Add Waypoints to Waypoint arraylist;
//            if (waypointMissionBuilder != null) {
//                waypointList.add(mWaypoint);
//                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
//            }else
//            {
//                waypointMissionBuilder = new WaypointMission.Builder();
//                waypointList.add(mWaypoint);
//                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
//            }
//        }else{
//            setResultToToast("Cannot Add Waypoint");
//        }
//    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    // Update the drone location based on states from MCU.
    private void updateDroneLocation(){

        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    droneMarker = aMap.addMarker(markerOptions);
                }
            }
        });
    }
//在地图上标记点
    private void markWaypoint(LatLng point){
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = aMap.addMarker(markerOptions);
        mMarkers.put(mMarkers.size(), marker);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate:{
                updateDroneLocation();
                cameraUpdate(); // Locate the drone's place
                break;
            }
            case R.id.add:{
                enableDisableAdd();
                break;
            }
            case R.id.clear: {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        aMap.clear();
                    }

                });
                waypointList.clear();
                waypointMissionBuilder.waypointList(waypointList);
                updateDroneLocation();
                break;
            }
            case R.id.config:{
                showSettingDialog();
                for(int i = 0;i < waypointList.size();i++){
                    System.out.println(waypointList.get(i)+"###########");
                }
                break;
            }
            case R.id.load:{
                jobString(v);
                break;
            }
            case R.id.save: {
                showJobSaveDialog();
                break;
            }
            case R.id.upload:{
                uploadWayPointMission();

                break;
            }

            case R.id.start:{
                startWaypointMission();
                break;
            }

            //TODO take_picture switch_mode and record
            case R.id.Pause:

                // Example of pausing an executing Mission
                instance.pauseMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        showResultToast(djiError);
                    }
                });
                break;
            case R.id.Resume:
                // Example of resuming a paused Mission
                instance.resumeMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        showResultToast(djiError);
                    }
                });
                break;

            case R.id.stop:{
                stopWaypointMission();
                break;
            }
            case R.id.Live:{
                startLiveShow();
                break;
            }

            case R.id.set: {
                //TODO
                LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
                //暂时取小数点后五位
                BigDecimal a = new BigDecimal(pos.latitude);
                double a1 = a.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
                BigDecimal b = new BigDecimal(pos.latitude);
                double b1 = b.setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();

                //取相对高度A float value of the relative altitude of the aircraft relative to take off location.
                LocationCoordinate3D alt = new LocationCoordinate3D(a1, b1, altitude);
                altitude = alt.getAltitude();
                Waypoint mWaypoint = new Waypoint(pos.latitude, pos.longitude, altitude);
                //Add Waypoints to Waypoint arraylist;
                if (waypointMissionBuilder != null) {
                    waypointList.add(mWaypoint);
                    waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                    setResultToToast("AddPoint Success!"+pos.latitude+altitude);
                    //打印
                    for(int i = 0;i < waypointList.size();i++){
                        System.out.println(waypointList.get(i));
                    }
                }else
                {
                    waypointMissionBuilder = new WaypointMission.Builder();
                    waypointList.add(mWaypoint);
                    setResultToToast("NewMissson Success!");
                    waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                    setResultToToast("AddPoint Success!");
                }
                break;
            }
            default:
                break;
        }
    }

    private void cameraUpdate(){
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        float zoomlevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        aMap.moveCamera(cu);

    }

    private void enableDisableAdd(){
        if (isAdd == false) {
            isAdd = true;
            add.setText("Exit");
        }else{
            isAdd = false;
            add.setText("Add");
        }
    }

    private void showSettingDialog(){
        LinearLayout wayPointSettings = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_waypointsetting, null);

        final TextView wpAltitude_TV = (TextView) wayPointSettings.findViewById(R.id.altitude);
        RadioGroup speed_RG = (RadioGroup) wayPointSettings.findViewById(R.id.speed);
        RadioGroup actionAfterFinished_RG = (RadioGroup) wayPointSettings.findViewById(R.id.actionAfterFinished);
        RadioGroup heading_RG = (RadioGroup) wayPointSettings.findViewById(R.id.heading);

        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.lowSpeed){
                    mSpeed = 3.0f;
                } else if (checkedId == R.id.MidSpeed){
                    mSpeed = 5.0f;
                } else if (checkedId == R.id.HighSpeed){
                    mSpeed = 10.0f;
                }
            }

        });

        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select finish action");
                if (checkedId == R.id.finishNone){
                    mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
                } else if (checkedId == R.id.finishGoHome){
                    mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
                } else if (checkedId == R.id.finishAutoLanding){
                    mFinishedAction = WaypointMissionFinishedAction.AUTO_LAND;
                } else if (checkedId == R.id.finishToFirst){
                    mFinishedAction = WaypointMissionFinishedAction.GO_FIRST_WAYPOINT;
                }
            }
        });

        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select heading");

                if (checkedId == R.id.headingNext) {
                    mHeadingMode = WaypointMissionHeadingMode.AUTO;
                } else if (checkedId == R.id.headingInitDirec) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_INITIAL_DIRECTION;
                } else if (checkedId == R.id.headingRC) {
                    mHeadingMode = WaypointMissionHeadingMode.CONTROL_BY_REMOTE_CONTROLLER;
                } else if (checkedId == R.id.headingWP) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING;
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(wayPointSettings)
                .setPositiveButton("Finish",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {

                        String altitudeString = wpAltitude_TV.getText().toString();
                        altitude = Integer.parseInt(nulltoIntegerDefalt(altitudeString));
                        Log.e(TAG,"altitude "+altitude);
                        Log.e(TAG,"speed "+mSpeed);
                        Log.e(TAG, "mFinishedAction "+mFinishedAction);
                        Log.e(TAG, "mHeadingMode "+mHeadingMode);
                        configWayPointMission();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }

                })
                .create()
                .show();
    }

    private String showJobSaveDialog(){
        LinearLayout wayPointSave = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_waypointjobsave, null);

        final TextView wpJobname_TV = (TextView) wayPointSave.findViewById(R.id.jobname);

        new AlertDialog.Builder(this)
                .setTitle("任务名称：")
                .setView(wayPointSave)
                .setPositiveButton("Finish",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {

                        Listname = wpJobname_TV.getText().toString();
                        Log.e(TAG,"任务名称： "+Listname);
//                        configWayPointMission();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }

                })
                .create()
                .show();
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        Toast.makeText(MainActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 1: Toast.makeText(MainActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
                        //注册成功跳转到登录页面
//                                startActivity( new Intent(MainActivity.this, MainActivity.class));
//                                MainActivity.this.finish();
                        break;
                    case 3:
                        Log.e("input error", "url为空");
                        break;
                    case 4:Toast.makeText(MainActivity.this, "连接超时", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                }
            }
        };
        //list转json
//                JSONArray array= JSONArray.parseArray(JSON.toJSONString(waypointList));
        //发送数据
        System.out.print(waypointList);

        URL url = null;
        try {
            url = new URL(URLSAVELIST);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        OperateData operateData = new OperateData();
        String[] data =new String[4];
        for(int i = 0;i < waypointList.size();i++){

            data[0] = Listname ;
            data[1] = String.valueOf(waypointList.get(i));
            data[2] = String.valueOf(i);
            data[3] = String.valueOf(waypointList.size());

//                    data[0] = "username:" + "\""+Listname +"\"";
//                    data[1] = "password:" + "\""+waypointList.get(i) +"\"";
//                    data[2] = "i:" + "\""+i+"\"";
            String jsonString = operateData.savestringTojson(data);
            operateData.sendData(jsonString, handler, url);
            System.out.println(jsonString);
        }
        return Listname;

    }

    private void showJobnameDialog(){
        LinearLayout wayPointSettings = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_waypointsetting, null);

        final TextView wpAltitude_TV = (TextView) wayPointSettings.findViewById(R.id.altitude);
        RadioGroup speed_RG = (RadioGroup) wayPointSettings.findViewById(R.id.speed);
        RadioGroup actionAfterFinished_RG = (RadioGroup) wayPointSettings.findViewById(R.id.actionAfterFinished);
        RadioGroup heading_RG = (RadioGroup) wayPointSettings.findViewById(R.id.heading);

        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.lowSpeed){
                    mSpeed = 3.0f;
                } else if (checkedId == R.id.MidSpeed){
                    mSpeed = 5.0f;
                } else if (checkedId == R.id.HighSpeed){
                    mSpeed = 10.0f;
                }
            }

        });

        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select finish action");
                if (checkedId == R.id.finishNone){
                    mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
                } else if (checkedId == R.id.finishGoHome){
                    mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
                } else if (checkedId == R.id.finishAutoLanding){
                    mFinishedAction = WaypointMissionFinishedAction.AUTO_LAND;
                } else if (checkedId == R.id.finishToFirst){
                    mFinishedAction = WaypointMissionFinishedAction.GO_FIRST_WAYPOINT;
                }
            }
        });

        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select heading");

                if (checkedId == R.id.headingNext) {
                    mHeadingMode = WaypointMissionHeadingMode.AUTO;
                } else if (checkedId == R.id.headingInitDirec) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_INITIAL_DIRECTION;
                } else if (checkedId == R.id.headingRC) {
                    mHeadingMode = WaypointMissionHeadingMode.CONTROL_BY_REMOTE_CONTROLLER;
                } else if (checkedId == R.id.headingWP) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING;
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(wayPointSettings)
                .setPositiveButton("Finish",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {

                        String altitudeString = wpAltitude_TV.getText().toString();
                        altitude = Integer.parseInt(nulltoIntegerDefalt(altitudeString));
                        Log.e(TAG,"altitude "+altitude);
                        Log.e(TAG,"speed "+mSpeed);
                        Log.e(TAG, "mFinishedAction "+mFinishedAction);
                        Log.e(TAG, "mHeadingMode "+mHeadingMode);
                        configWayPointMission();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }

                })
                .create()
                .show();
    }

    String nulltoIntegerDefalt(String value){
        if(!isIntValue(value)) value="0";
        return value;
    }

    boolean isIntValue(String val)
    {
        try {
            val=val.replace(" ","");
            Integer.parseInt(val);
        } catch (Exception e) {return false;}
        return true;
    }

    private void configWayPointMission(){

        if (waypointMissionBuilder == null){

            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                                                                  .headingMode(mHeadingMode)
                                                                  .autoFlightSpeed(mSpeed)
                                                                  .maxFlightSpeed(mSpeed)
                                                                  .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        }else
        {
            waypointMissionBuilder.finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        }

        if (waypointMissionBuilder.getWaypointList().size() > 0){

            for (int i=0; i< waypointMissionBuilder.getWaypointList().size(); i++){
                waypointMissionBuilder.getWaypointList().get(i).altitude = altitude;
            }

            setResultToToast("Set Waypoint attitude successfully");
        }

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            setResultToToast("loadWaypoint succeeded");
        } else {
            setResultToToast("loadWaypoint failed " + error.getDescription());
        }

    }

    private void uploadWayPointMission(){

        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    setResultToToast("Mission upload successfully!");
                } else {
                    setResultToToast("Mission upload failed, error: " + error.getDescription() + " retrying...");
                    getWaypointMissionOperator().retryUploadMission(null);
                }
            }
        });

    }
    //waypoint action
    //todo
    public void WaypointAction(){
        WaypointAction cruiserAction = new WaypointAction(START_TAKE_PHOTO,1);
    };

    private void startWaypointMission(){

        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {

            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
            }
        });

    }

    private void stopWaypointMission(){

        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
            }
        });

    }

    private void showResultToast(DJIError djiError) {
        ToastUtils.setResultToToast(djiError == null ? "Action started!" : djiError.getDescription());
    }

    private void initPreviewer() {

        BaseProduct product = DJIDemoApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(mReceivedVideoDataListener);
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = DJIDemoApplication.getCameraInstance();
        if (camera != null){
            // Reset the callback
            VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }

        return false;
    }
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    //Live
    private boolean isLiveStreamManagerOn() {
        if (DJISDKManager.getInstance().getLiveStreamManager() == null) {
            ToastUtils.setResultToToast("No live stream manager!");
            return false;
        }
        return true;
    }
    public void startLiveShow() {
//        ToastUtils.setResultToToast("Start Live Show");
        if (!isLiveStreamManagerOn()) {
            return;
        }
        if (DJISDKManager.getInstance().getLiveStreamManager().isStreaming()) {
//            ToastUtils.setResultToToast("already started!");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                DJISDKManager.getInstance().getLiveStreamManager().setLiveUrl(liveShowUrl);
                int result = DJISDKManager.getInstance().getLiveStreamManager().startStream();
                DJISDKManager.getInstance().getLiveStreamManager().setStartTime();
//                ToastUtils.setResultToToast("startLive:" + result +
//                        "\n isVideoStreamSpeedConfigurable:" + DJISDKManager.getInstance().getLiveStreamManager().isVideoStreamSpeedConfigurable() +
//                        "\n isLiveAudioEnabled:" + DJISDKManager.getInstance().getLiveStreamManager().isLiveAudioEnabled());
            }
        }.start();
    }

    //任务选择菜单
    public void jobString(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.alert_dark_frame);
        builder.setTitle("请选择");

        //从String 转为net.url
        URL url = null;
        try {
            url = new URL(URL1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

//        List<Map<String, Object>> list = operateData.receiveJson(handler, url);
        String[] items;

        Handler handler=new Handler(){
            public void handleMessage(Message msg) {
                jobMap=((LinkedHashMap<String, Object>)msg.obj);
                aa(jobMap);
            }
        };

        JsonToHashMap j=new JsonToHashMap(url.toString(),handler);

        items = aa(jobMap);
        System.out.print(items);

        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "选择的是：" + items[which], Toast.LENGTH_SHORT).show();
                System.out.println(items[which] + "choice");
                //从后台取到该list数据并放入Waylist
                //todo
                URL url = null;
                try {
                    url = new URL(URL2);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

//        List<Map<String, Object>> list = operateData.receiveJson(handler, url);
                String temp = items[which];


                Handler handler=new Handler(){
                    public void handleMessage(Message msg) {
                        String temp1 = msg.obj.toString();
                        System.out.println(temp1+"后台已经获取！"+ temp1.length());
                        StringtoJobList(temp1);
                    }
                };
                JsonToHashMap j=new JsonToHashMap(url.toString(),handler,temp);

//                Timer timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    public void run() {
//
//                        System.out.println("延迟5s");
//                        //延迟特定时间后执行该语句（public void run()的花括号里的语句）
//                        int a = temp1.length();
//                        System.out.println(a+"a大小");
//                        int i = (int) Math.floor((temp1.length())/76);
//                        System.out.println(i+"大小");
//                        ArrayList<String> tempjoblist = new ArrayList();
//                        for (int k = 0; k <i ; k++) {
//                            tempjoblist.add(temp1.substring(44+k*77,53+k*77));
//                            tempjoblist.add(temp1.substring(65+k*77,74+k*77));
//                        }
////                List<String> newd = Arrays.asList(Waypointlisttemp);
//                        System.out.println(tempjoblist+"打印list!");
//                        System.out.println(tempjoblist.size()+"K");
//                        for (int k = 0; k < tempjoblist.size(); k-=2) {
//
//                            Waypoint mWaypoint = new Waypoint(Double.parseDouble(tempjoblist.get(k-1)),Double.parseDouble(tempjoblist.get(k-2)), altitude);
//                            //Add Waypoints to Waypoint arraylist;
//                            if (waypointMissionBuilder != null) {
//                                waypointList.add(mWaypoint);
//                                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
//                                setResultToToast("AddPoint Success!");
//                                //打印
//                                for(int l = 0;l < waypointList.size();l++){
//                                    System.out.println(waypointList.get(l)+"加载List！！");
//                                }
//                            }else
//                            {
//                                waypointMissionBuilder = new WaypointMission.Builder();
//                                waypointList.add(mWaypoint);
//                                setResultToToast("NewMissson Success!");
//                                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
//                                setResultToToast("AddPoint Success!");
//                            }
//                            break;
//
//                        }
//                    } }, 10000);


                dialog.dismiss();
            }
 });
        builder.show();
    }
    Object jobs,id;
    public String[] aa(LinkedHashMap<String, Object> map){
        id=map.get("data");
        List<String> myList = new ArrayList<>();
        LinkedHashMap<String,Object> temp=(LinkedHashMap<String, Object>) id;
        Iterator ir=map.keySet().iterator();
        while (ir.hasNext()){
            Object key= ir.next();
            myList.add(map.get(key).toString());
        }

        String[] tempitems = myList.toArray(new String[myList.size()]);
        String tempString;
        tempString = Arrays.toString(tempitems);
        //截取
        List<String> ret = new ArrayList<>();
        int ch = 0, start, end;
        while (ch < tempString.length()) {
            // 索引出现负数，说明在源字符串指定位置之后已经没有 '=' 或者 '}'
            start = tempString.indexOf("=", ch);
            end = tempString.indexOf("}", ch);
            // substring 内部索引禁止出现负数
            if (start == -1 || end == -1) {
                break;
            }
            String items = tempString.substring(start + 1, end);
            //保存上一次截取时的索引
            ch = end + 1;
            ret.add(items);
        }
        String[] items = ret.toArray(new String[ret.size()]);
//        System.out.println(Arrays.toString(items)+"++++++++");
//        for (int i = 0; i < items.length; i++) {
//            items = items[i].split("\\=(.*?)\\}",1);
//        }
//        System.out.println(Arrays.toString(items)+"-------");
        return items;
    }
    public void StringtoJobList(String temp1){
        int a = temp1.length();
        System.out.println(a+"a大小");
        int i = (int) Math.floor((temp1.length())/76);
        System.out.println(i+"大小");
        ArrayList<String> tempjoblist = new ArrayList();
        for (int k = 0; k < i ; k++) {
            tempjoblist.add(temp1.substring(44+k*78,52+k*78));
            tempjoblist.add(temp1.substring(65+k*78,74+k*78));
        }
//                List<String> newd = Arrays.asList(Waypointlisttemp);
        System.out.println(tempjoblist+"打印list!");
        System.out.println(tempjoblist.size()+"list大小");
        for (int k = 0; k < i*2; k+=2) {
            Waypoint mWaypoint = new Waypoint(Double.parseDouble(tempjoblist.get(k)), Double.parseDouble(tempjoblist.get(k+1)), altitude);
            //Add Waypoints to Waypoint arraylist;
            if (waypointMissionBuilder != null) {
                waypointList.add(mWaypoint);
                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                setResultToToast("AddPoint Success!");
                //打印
                for (int l = 0; l < waypointList.size(); l++) {
                    System.out.println(waypointList.get(l) + "加载List！！");
                }
            } else {
                waypointMissionBuilder = new WaypointMission.Builder();
                waypointList.add(mWaypoint);
                setResultToToast("NewMissson Success!");
                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
                setResultToToast("AddPoint Success!");
                for (int l = 0; l < waypointList.size(); l++) {
                    System.out.println(waypointList.get(l) + "加载List！！");
                }
            }
        }
    }
}
