package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import model.City; 
import model.County;
import model.Province;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import db.CoolWeatherDB;

public class Utility {
	
	/*========================-======》主要解析的是省、城、县的一些信息
	/*
	 * 解析和处理服务器返回的"省级"数据
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response)
	{
		if(!TextUtils.isEmpty(response))
		{
			//将各个省的“编号+名字”保存在字符串数组
			String[] allProvinces=response.split(",");
			if(allProvinces!=null&&allProvinces.length>0)
			{
				//遍历数组
				for(String p : allProvinces)
				{
					//将编号和名字分开
					String[] array=p.split("\\|");//01|北京，02|上海......
					Province province=new Province();
					province.setProvinceName(array[1]);
					province.setProvinceCode(array[0]);
					//将解析出来的数据存储在Province表
					coolWeatherDB.saveProvince(province);
					
				}
				return true;
			}
		}
		return false;
	}
	
	
	/*
	 * 解析和处理服务器返回的城市数据
	 */
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] allCities=response.split(",");
			if(allCities!=null&&allCities.length>0)
			{
				for(String p : allCities)
				{
					String[] array=p.split("\\|");
					City city=new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					coolWeatherDB.saveCity(city);
				}
				
			}
			return true;
			
		}
		return false;
	}
	
	
	/*
	 * 解析和处理服务器返回的县城数据
	 */
	public synchronized static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] allCounties=response.split(",");
			for(String p : allCounties)
			{
				String[] array=p.split("\\|");//转义字符“|”，\本身具有转义的作用，\\表示\,"\\|"的意思是|不用做转义字符使用
				County county=new County();
				county.setCountyCode(array[0]);
				county.setCountyName(array[1]);
				county.setCityId(cityId);
				coolWeatherDB.saveCounty(county);
			}
			return true;		
		}
		return false;
	}
	
	/*=============================================》主要是天气信息
	 * 解析服务器返回的json数据(主要是城市名称和代号对应的天气数据)，并将解析出的数据存储到本地
	 */
	public static void handleWeatherResponse(Context context,String response)
	{
		try
		{
			//json对象
			JSONObject jsonObject=new JSONObject(response);
			JSONObject weatherInfo=jsonObject.getJSONObject("weatherInfo");
			String cityName=weatherInfo.getString("city");
			String weatherCode=weatherInfo.getString("cityid");
			String temp1=weatherInfo.getString("temp1");
			String temp2=weatherInfo.getString("temp2");
			String weatherDesp=weatherInfo.getString("weather");
			String publishTime=weatherInfo.getString("ptime");
			saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
			
			
		}catch(JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * 将服务器返回的所有"天气信息"存储到SharePreference,
	 */
	public static void saveWeatherInfo(Context context,String cityName,String weatherCode,String temp1,String temp2,String weatherDesp,String publishTime)
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy年M月d日",Locale.CHINA);
		//创建配置编辑器
		SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(context).edit();
		//配置中存入参数
		editor.putBoolean("city_selected",true);
		editor.putString("city_name",cityName);
		editor.putString("weather_code",weatherCode);
		editor.putString("temp1",temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp",weatherDesp);
		editor.putString("publish_time", publishTime);
		//new Date()是获取当前系统时间
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
		
	}
}
