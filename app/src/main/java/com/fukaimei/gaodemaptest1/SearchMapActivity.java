package com.fukaimei.gaodemaptest1;

import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;

/**
 * Created by 傅开煤 on 2017/10/7.
 */

public class SearchMapActivity extends Activity implements GeocodeSearch.OnGeocodeSearchListener {

    private MapView mapView = null;
    private AMap aMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_map_layout);
        //获取地图控件引用
        mapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        init();
        Button bn = (Button) findViewById(R.id.loc);
        final TextView addrTv = (TextView) findViewById(R.id.address);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addr = addrTv.getText().toString();
                if (addr.equals("")) {
                    Toast.makeText(SearchMapActivity.this, "地址不能为空！", Toast.LENGTH_LONG).show();
                } else {
                    GeocodeSearch search = new GeocodeSearch(SearchMapActivity.this);
                    search.setOnGeocodeSearchListener(SearchMapActivity.this);
                    GeocodeQuery query = new GeocodeQuery(addr, "中国");
                    // 根据地址执行异步地址解析
                    search.getFromLocationNameAsyn(query);
                }
            }
        });
    }

    // 初始化AMap对象
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            // 创建一个设置放大级别的CameraUpdate
            CameraUpdate cu = CameraUpdateFactory.zoomTo(15);
            // 设置地图的默认放大级别
            aMap.moveCamera(cu);
            // 创建一个更改地图倾斜度的CameraUpdate
            CameraUpdate tiltUpdate = CameraUpdateFactory.changeTilt(30);
            // 改变地图的倾斜度
            aMap.moveCamera(tiltUpdate);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        // 获取解析得到的第一个地址
        GeocodeAddress geo = geocodeResult.getGeocodeAddressList().get(0);
        // 获取解析得到的经纬度
        LatLonPoint pos = geo.getLatLonPoint();
        LatLng targetPos = new LatLng(pos.getLatitude(), pos.getLongitude());
        // 创建一个设置经纬度的CameraUpdate
        CameraUpdate cu = CameraUpdateFactory.changeLatLng(targetPos);
        // 更新地图的显示区域
        aMap.moveCamera(cu);
        // 创建一个GroundOverlayOptions（用于向地图上添加图片)
        GroundOverlayOptions options = new GroundOverlayOptions()
                // 设置GroundOverlayOptions包装的图片
                .image(BitmapDescriptorFactory.fromResource(R.drawable.icon_geo)).position(targetPos , 64);
        // 添加图片
        aMap.addGroundOverlay(options);
        // 创建一个CircleOptions（用于向地图上添加圆形）
        CircleOptions cOptions = new CircleOptions().center(targetPos)  // 设置圆心
        .fillColor(0x80ffff00)  // 设置圆形的填充颜色
        .radius(200)  // 设置圆形的半径
        .strokeWidth(1)  // 设置圆形的线条宽度
        .strokeColor(0xff000000);  // 设置圆形的线条颜色
        aMap.addCircle(cOptions);
    }

}















