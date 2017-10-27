package com.fukaimei.gaodemaptest1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.fukaimei.gaodemaptest1.util.MapFixUtil;

import java.util.ArrayList;
import java.util.List;

public class NavMapActivity extends Activity implements View.OnClickListener
        , OnGeocodeSearchListener, OnRouteSearchListener {
    private static final int WRITE_COARSE_LOCATION_REQUEST_CODE = 0;
    private static final String TAG = "NavMapActivity";
    GeocodeSearch search;
    RouteSearch routeSearch;
    Context context;
    private MapView mapView;
    private AMap aMap;
    private LocationManager locMgr;
    private EditText targetAddressEt;
    private Button navBn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_map_layout);
        targetAddressEt = (EditText) findViewById(R.id.address);
        mapView = (MapView) findViewById(R.id.map);
        // 必须回调MapView的onCreate()方法
        mapView.onCreate(savedInstanceState);
        navBn = (Button) findViewById(R.id.nav);
        navBn.setOnClickListener(this);
        init();
        // 创建GeocodeSearch对象
        search = new GeocodeSearch(this);
        search.setOnGeocodeSearchListener(this);
        routeSearch = new RouteSearch(this);
        routeSearch.setRouteSearchListener(this);
        locMgr = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
        //          SDK在Android 6.0以上的版本需要进行运行检测的动态权限如下：
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.READ_PHONE_STATE

        //这里用到ACCESS_FINE_LOCATION与ACCESS_COARSE_LOCATION权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        }
        // 通过监听器监听GPS提供的定位信息的改变
        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                300, 8, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // 使用GPS提供的定位信息来更新位置
                        updatePosition(location);
                    }

                    @Override
                    public void onStatusChanged(String provider
                            , int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            //申请WRITE_EXTERNAL_STORAGE权限
                            ActivityCompat.requestPermissions((Activity) context, new String[]{
                                            Manifest.permission.ACCESS_FINE_LOCATION},
                                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
                        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            //申请WRITE_EXTERNAL_STORAGE权限
                            ActivityCompat.requestPermissions((Activity) context,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
                        }
                        // 使用GPS提供的定位信息来更新位置
                        updatePosition(locMgr.getLastKnownLocation(provider));
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                });
    }

    // 初始化AMap对象
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            // 创建一个设置放大级别的CameraUpdate
            CameraUpdate cu = CameraUpdateFactory.zoomTo(16);
            // 设置地图的默认放大级别
            aMap.moveCamera(cu);
        }
    }

    private void updatePosition(Location location) {
        // 纠偏前的经度
        double lon = location.getLongitude();
        // 纠偏前的纬度
        double lat = location.getLatitude();
        double point[] = MapFixUtil.transform(lat, lon);
        // 纠偏后的经度
        double correctLon = point[1];
        // 纠偏后的纬度
        double correctLat = point[0];
//        Log.d(TAG, "纠偏前的经度：" + lon + ",纠偏前的纬度：" + lat);
//        Log.d(TAG, "纠偏后的经度：" + correctLon + ",纠偏后的纬度：" + correctLat);
        LatLng pos = new LatLng(correctLat, correctLon);
        // 创建一个设置经纬度的CameraUpdate
        CameraUpdate cu = CameraUpdateFactory.changeLatLng(pos);
        // 更新地图的显示区域
        aMap.moveCamera(cu);
        // 清除所有Marker等覆盖物
        aMap.clear();
        MarkerOptions markerOptions = new MarkerOptions()
                .position(pos)
                // 设置使用自定义图标
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                .draggable(true);
        // 添加Marker
        aMap.addMarker(markerOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 必须回调MapView的onResume()方法
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 必须回调MapView的onPause()方法
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 必须回调MapView的onSaveInstanceState()方法
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 必须回调MapView的onDestroy()方法
        mapView.onDestroy();
    }

    @Override
    public void onClick(View v) {
        String address = targetAddressEt.getText().toString().trim();
        if (address.equals("")) {
            Toast.makeText(this, "请输入有效的地址"
                    , Toast.LENGTH_LONG).show();
        } else {
            GeocodeQuery query = new GeocodeQuery(address, "中国");
            // 根据地址执行异步查询
            search.getFromLocationNameAsyn(query);
        }
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult
                                            regeocodeResult, int i) {
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        GeocodeAddress addr = geocodeResult.getGeocodeAddressList().get(0);
        // 获取目标前的经纬度
        LatLonPoint latlng = addr.getLatLonPoint();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    WRITE_COARSE_LOCATION_REQUEST_CODE);//自定义的code
        }
        // 获取用户当前的位置
        Location loc = locMgr.getLastKnownLocation(
                LocationManager.GPS_PROVIDER);
        // 纠偏前的经度
        double lon = loc.getLongitude();
        // 纠偏前的纬度
        double lat = loc.getLatitude();
        double point[] = MapFixUtil.transform(lat, lon);
        // 纠偏后的经度
        double correctLon = point[1];
        // 纠偏后的纬度
        double correctLat = point[0];
//        Log.d(TAG, "纠偏前的经度：" + lon + ",纠偏前的纬度：" + lat);
//        Log.d(TAG, "纠偏后的经度：" + correctLon + ",纠偏后的纬度：" + correctLat);
        // 创建路线规划的起始点
        RouteSearch.FromAndTo ft = new RouteSearch.FromAndTo(
                new LatLonPoint(correctLat, correctLon), latlng);
        // 创建自驾车的查询条件
        RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch
                .DriveRouteQuery(ft // 定义道路规划的起始点
                , RouteSearch.DrivingDefault
                , null  // 该参数指定必须经过的多个点
                , null  // 该参数指定必须避开的多个区域
                , null  // 该参数指定必须避开的道路
        );
        routeSearch.calculateDriveRouteAsyn(driveRouteQuery);
    }

    @Override
    public void onBusRouteSearched(BusRouteResult
                                           busRouteResult, int i) {
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        // 获取系统规划第一条路线（实际应用中可提供多条路线供用户选择）
        DrivePath drivePath = driveRouteResult.getPaths().get(0);
        // 获取该规划路线所包含的多条路段
        List<DriveStep> steps = drivePath.getSteps();
        for (DriveStep step : steps) {
            // 获取组成该路段的多个点
            List<LatLonPoint> points = step.getPolyline();
            List<LatLng> latLngs = new ArrayList<>();
            for (LatLonPoint point : points) {
                latLngs.add(new LatLng(point.getLatitude()
                        , point.getLongitude()));
            }
            // 创建一个PolylineOptions（用于向地图添加多线段）
            PolylineOptions ployOptions = new PolylineOptions()
                    // 添加多个点
                    .addAll(latLngs)
                    .color(0xffff0000)
                    .width(8);
            aMap.addPolyline(ployOptions);
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult
                                            walkRouteResult, int i) {
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }
}
