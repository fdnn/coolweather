package com.xiaohai.coolweather.db;

import java.util.ArrayList;
import java.util.List;

import com.xiaohai.coolweather.model.City;
import com.xiaohai.coolweather.model.County;
import com.xiaohai.coolweather.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CoolWeatherDB {
	public static final String DB_NAME = "cool_weather";
	public static final int VERSION = 1;
	private SQLiteDatabase db;
	private static CoolWeatherDB coolWeatherDB;

	private CoolWeatherDB(Context context) {
		DatabaseOpenHelper dbHelper = new DatabaseOpenHelper(context, DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}

	// µ¥ÀýÄ£Ê½
	public static synchronized CoolWeatherDB getInstance(Context context) {
		if (coolWeatherDB == null) {
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}

	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("name", province.getName());
			values.put("code", province.getCode());
			db.insert("Province", null, values);
		}
	}

	public List<Province> provinceList() {
		List<Province> provinceList = new ArrayList<Province>();
		Cursor cursor = db.query("Province", null, null, null, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setName(cursor.getString(cursor.getColumnIndex("name")));
				province.setCode(cursor.getString(cursor.getColumnIndex("code")));
				provinceList.add(province);
			}
			cursor.close();
		}
		return provinceList;
	}

	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("name", city.getName());
			values.put("code", city.getCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
		}
	}

	public List<City> cityList(int provinceId) {
		List<City> cityList = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id = ?", new String[] { String.valueOf(provinceId) }, null,
				null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setName(cursor.getString(cursor.getColumnIndex("name")));
				city.setCode(cursor.getString(cursor.getColumnIndex("code")));
				city.setProvinceId(provinceId);
				cityList.add(city);
			}
			cursor.close();
		}
		return cityList;
	}

	public void saveCounty(County county) {
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("name", county.getName());
			values.put("code", county.getCode());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
		}
	}

	public List<County> countyList(int cityId) {
		List<County> countyList = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id = ?", new String[] { String.valueOf(cityId) }, null, null,
				null);
		if (cursor!=null) {
			while(cursor.moveToNext()){
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setName(cursor.getString(cursor.getColumnIndex("name")));
				county.setCode(cursor.getString(cursor.getColumnIndex("code")));
				county.setCityId(cityId);
				countyList.add(county);
			};
		}
		return countyList;
	}
}
