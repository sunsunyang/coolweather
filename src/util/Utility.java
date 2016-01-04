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
	
	/*========================-======����Ҫ��������ʡ���ǡ��ص�һЩ��Ϣ
	/*
	 * �����ʹ�����������ص�"ʡ��"����
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response)
	{
		if(!TextUtils.isEmpty(response))
		{
			//������ʡ�ġ����+���֡��������ַ�������
			String[] allProvinces=response.split(",");
			if(allProvinces!=null&&allProvinces.length>0)
			{
				//��������
				for(String p : allProvinces)
				{
					//����ź����ַֿ�
					String[] array=p.split("\\|");//01|������02|�Ϻ�......
					Province province=new Province();
					province.setProvinceName(array[1]);
					province.setProvinceCode(array[0]);
					//���������������ݴ洢��Province��
					coolWeatherDB.saveProvince(province);
					
				}
				return true;
			}
		}
		return false;
	}
	
	
	/*
	 * �����ʹ�����������صĳ�������
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
	 * �����ʹ�����������ص��س�����
	 */
	public synchronized static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] allCounties=response.split(",");
			for(String p : allCounties)
			{
				String[] array=p.split("\\|");//ת���ַ���|����\�������ת������ã�\\��ʾ\,"\\|"����˼��|������ת���ַ�ʹ��
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
	
	/*=============================================����Ҫ��������Ϣ
	 * �������������ص�json����(��Ҫ�ǳ������ƺʹ��Ŷ�Ӧ����������)�����������������ݴ洢������
	 */
	public static void handleWeatherResponse(Context context,String response)
	{
		try
		{
			//json����
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
	 * �����������ص�����"������Ϣ"�洢��SharePreference,
	 */
	public static void saveWeatherInfo(Context context,String cityName,String weatherCode,String temp1,String temp2,String weatherDesp,String publishTime)
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy��M��d��",Locale.CHINA);
		//�������ñ༭��
		SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(context).edit();
		//�����д������
		editor.putBoolean("city_selected",true);
		editor.putString("city_name",cityName);
		editor.putString("weather_code",weatherCode);
		editor.putString("temp1",temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp",weatherDesp);
		editor.putString("publish_time", publishTime);
		//new Date()�ǻ�ȡ��ǰϵͳʱ��
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
		
	}
}
