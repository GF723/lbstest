package com.example.lbstest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class MainActivity extends AppCompatActivity {
    private LocationClient locationClient;
    private TextView textView;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.position_text_view);
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
//        baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.
                    size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }

    private void navigateTo(BDLocation location) {
        if(isFirstLocate) {
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFirstLocate =false;
        }

        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    private void requestLocation () {
        initLocation();
        locationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(3000);
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        locationClient.setLocOption(option);
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        super.onResume();

    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch ( requestCode) {
            case 1:
                if(grantResults.length > 0) {
                    for (int result: grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                    }
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }

                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType()
                            == BDLocation.TypeNetWorkLocation) {
                        navigateTo(location);
                    }

                    StringBuilder currentPosition = new StringBuilder();
                    currentPosition.append("纬度：").append(location.getLatitude()).
                            append("\n");
                    currentPosition.append("经线：").append(location.getLongitude()).
                            append("\n");
                    currentPosition.append("国家:").append(location.getCountry()).
                            append("\n");
                    currentPosition.append("省:").append(location.getProvince()).
                            append("\n");
                    currentPosition.append("市:").append(location.getCity()).
                            append("\n");
                    currentPosition.append("区:").append(location.getDistrict()).
                            append("\n");
                    currentPosition.append("街道:").append(location.getStreet()).
                            append("\n");
                    currentPosition.append("定位方式：");
                    if (location.getLocType() == BDLocation.TypeGpsLocation) {
                        currentPosition.append("GPS");
                    } else if (location.getLocType() ==
                            BDLocation.TypeNetWorkLocation) {
                        currentPosition.append("网络");
                    }
                    textView.setText(currentPosition);
                }
            });
        }

    }
}