package activity;

import com.example.coolweather.R;

import service.AutoUpdateService;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;

public class WeatherActivity extends Activity implements OnClickListener{

	private LinearLayout weatherInfoLayout;
	/*
	 * show cityName
	 */
	private TextView cityNameText;
	/*
	 * show publishTime
	 */
	private TextView publishText;
	/*
	 * show describe informations of weather  
	 */
	private TextView weatherDespText;
	/*
	 * show weatherInfo
	 */
	private TextView weatherInfoText;
	/*
	 * show temp1
	 */
	private TextView temp1Text;
	/*
	 * show temp2
	 */
	private TextView temp2Text;
	/*
	 * show local date
	 */
	private TextView currentDateText;
	/*
	 * switch city btn
	 */
	private Button switchCity;
	/*
	 * refresh Weather btn
	 */
	private Button refreshWeather;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//initialize controls
		switchCity=(Button)findViewById(R.id.switch_city);
		refreshWeather=(Button)findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
		cityNameText=(TextView)findViewById(R.id.city_name);
		publishText=(TextView)findViewById(R.id.publish_text);
		weatherInfoText=(TextView)findViewById(R.id.wether_desp);
		temp1Text=(TextView)findViewById(R.id.temp1);
		temp2Text=(TextView)findViewById(R.id.temp2);
		currentDateText=(TextView)findViewById(R.id.current_date);

		//获取ChooseAreaActivity传过来的数据，key=“county_code”
		String countyCode=getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countyCode))
		{
			//query weatherInfo with countyCode
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}
		else
		{
			showWeather();
		}

	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.switch_city:
			Intent intent=new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode=prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode))
			{
				queryWeatherInfo(weatherCode);

			}
			break;
		default:
			break;

		}
	}

	/*
	 * query weatherCode corresponding to cityCode
	 */
	private void queryWeatherCode(String countyCode){

		String address="http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		queryFromServer(address,"countyCode");
	}

	/*
	 * query weather corresponding to weatherCode
	 */
	private void queryWeatherInfo(String weatherCode){
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		queryFromServer(address,"weatherCode");
	}

	/*
	 * query weatherCode or weatherInfo according to address and type
	 */
	private void queryFromServer(final String address,final String type)
	{
		HttpUtil.sendHttpRequest(address,new HttpCallbackListener(){
			@Override
			public void onFinish(final String response)
			{
				if("countyCode".equals(type))
				{
					if(!TextUtils.isEmpty(response))
					{
						//analysis weatherCode from data coming from server
						String[] array=response.split("\\|");
						if(array!=null&&array.length==2)
						{
							String weatherCode=array[1];
							queryWeatherInfo(weatherCode);
						}

					}
				}
				else
					if("weatherCode".equals(type))
					{
						//manage weatherInfo coming from server
						Utility.handleWeatherResponse(WeatherActivity.this, response);
						runOnUiThread(new Runnable(){
							@Override
							public void run(){
								showWeather();
							}
						});

					}
			}
			@Override
			public void onError(Exception e)
			{
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						publishText.setText("同步失败");
					}
				});
			}
		});

	}

	/*
	 * get weatherInfo from SharedPreferences and show in UI
	 */
	private void showWeather()
	{
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1",""));
		temp2Text.setText(prefs.getString("temp2",""));
		weatherDespText.setText(prefs.getString("weather_desp",""));
		publishText.setText("今天"+prefs.getString("publish_time", "")+"发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		//启动更新天气的服务
		Intent intent=new Intent(this,AutoUpdateService.class);
		startService(intent);

	}

	/*
	 * 
	 */
}
