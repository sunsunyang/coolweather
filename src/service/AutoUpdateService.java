package service;

import util.HttpUtil;
import util.Utility;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import util.HttpCallbackListener;
import receiver.AutoUpdateReceiver;


public class AutoUpdateService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		new Thread(new Runnable(){
		public void run()
		{
			updateWeather();
		}
		}).start();
		//获取系统提醒服务
		AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
		int anHour=8*60*60*1000;//更新时长为8小时
		//系统启动以来的时长
		long triggerAtTime=SystemClock.elapsedRealtime()+anHour;
		//绑定意图
		Intent i=new Intent(this,AutoUpdateReceiver.class);
		//初始化延迟意图,注册广播
		PendingIntent pi=PendingIntent.getBroadcast(this,0,i,0);
		//提醒并启动绑定的意图
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
		return super.onStartCommand(intent, flags, startId);
	}
	
	/*
	 * 更新天气
	 */
	public void updateWeather()
	{
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode=prefs.getString("weather_code","");
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){
			public void onFinish(String response)
			{
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}
			
			public void onError(Exception e)
			{
				e.printStackTrace();
			}
		});
		
	}
	 

}
