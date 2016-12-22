package com.xiaohai.coolweather.activity;


import java.util.ArrayList;
import java.util.List;

import com.xiaohai.coolweather.R;
import com.xiaohai.coolweather.db.CoolWeatherDB;
import com.xiaohai.coolweather.model.City;
import com.xiaohai.coolweather.model.County;
import com.xiaohai.coolweather.model.Province;
import com.xiaohai.coolweather.util.HttpCallbackListener;
import com.xiaohai.coolweather.util.HttpUtil;
import com.xiaohai.coolweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity{
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	private int currentLevel;
	private ListView listView;
	private TextView titleText;
	private ArrayAdapter adapter;
	private List<String> dateList = new ArrayList<String>();
	private CoolWeatherDB coolWeatherDB;
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	private Province selectedProvince;
	private City selectedCity;
	private ProgressDialog progressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, dateList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(position);
					queryCounties();
				}
			}
		});
		queryProvinces();
	}
	
	private void queryProvinces(){
		provinceList = coolWeatherDB.provinceList();
		currentLevel = LEVEL_PROVINCE;
		// 优先从数据库查询
		if(provinceList.size()>0){
			dateList.clear();
			for (Province province : provinceList) {
				dateList.add(province.getName());
			}
			adapter.notifyDataSetChanged();// 重绘ListView
			listView.setSelection(0);// 让ListView定位到指定Item的位置
			titleText.setText("中国");
		} else{
			// 去服务器上查询
			queryFromServer(null);
		}
	}

	private void queryCities(){
		cityList = coolWeatherDB.cityList(selectedProvince.getId());
		currentLevel = LEVEL_CITY;
		if(cityList.size()>0){
			dateList.clear();
			for (City city : cityList) {
				dateList.add(city.getName());
			}
			adapter.notifyDataSetInvalidated();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getName());
		}else{
			queryFromServer(selectedProvince.getCode());
		}
	}

	private void queryCounties(){
		countyList = coolWeatherDB.countyList(selectedCity.getId());
		currentLevel = LEVEL_COUNTY;
		if(countyList.size()>0){
			dateList.clear();
			for (County county : countyList) {
				dateList.add(county.getName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getName());
		}else{
			queryFromServer(selectedCity.getCode());
		}
	}

	private void queryFromServer(final String code){
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if(currentLevel==LEVEL_PROVINCE){
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if(currentLevel==LEVEL_CITY){
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if(currentLevel==LEVEL_COUNTY){
					result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(result){
					// 回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						@Override
						public void run() { 
							closeProgressDialog();
							if(currentLevel==LEVEL_PROVINCE){
								queryProvinces();
							}else if(currentLevel==LEVEL_CITY){
								queryCities();
							}else if(currentLevel==LEVEL_COUNTY){
								queryCounties();
							}
						}
					});
				}
			}
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载出错", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在ing...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
}
