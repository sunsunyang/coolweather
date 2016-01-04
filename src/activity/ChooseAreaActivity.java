package activity;

import java.util.ArrayList;
import java.util.List;

import util.HttpUtil;
import util.Utility;

import com.example.coolweather.R;

import util.HttpCallbackListener;
import model.City;
import model.County;
import model.Province;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import db.CoolWeatherDB;
import android.view.*;
import util.HttpUtil;
import util.HttpCallbackListener;

@SuppressWarnings("unused")
public class ChooseAreaActivity extends Activity
{
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();

	/*
	 * 省列表
	 */
	private List<Province> provinceList;

	/*
	 * 市列表
	 */
	private List<City> cityList;

	/*
	 * 县列表
	 */
	private List<County> countyList;

	/*
	 * 选中的省份
	 */
	private Province selectedProvince;

	/*
	 * 选中的城市
	 */
	private City selectedCity;

	/*
	 * 选中的县
	 */
	private County selectedCounty;

	/*
	 * 当前选中的级别
	 */
	private int currentLevel;
	
	
	/*
	 * jump here from WeatherActivity or not
	 */
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather", false);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		//if having  choosed city but not jumping from WeatherActivity,jump to WeatherActivity 
		if(prefs.getBoolean("city_selected",false)&&!isFromWeatherActivity)
		{
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choosed_area);
		//初始化控件
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		//实例化数组适配器,数组适配器是为列表控件配置数据的，dataList提供数据
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		//为列表控件添加适配器
		listView.setAdapter(adapter);

		coolWeatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View view,int index, long arg3)
			{
				if(currentLevel==LEVEL_PROVINCE)
				{
					selectedProvince=provinceList.get(index);
					queryCities();

				}
				else if(currentLevel==LEVEL_CITY)
				{
					selectedCity=cityList.get(index);
					queryCounties();
				}
				else if(currentLevel==LEVEL_COUNTY)
				{
					String countyCode=countyList.get(index).getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
	}

	/*
	 * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器查询。
	 */
	private void queryProvinces()
	{
		provinceList=coolWeatherDB.loadProvinces();
		if(provinceList.size()>0)
		{
			dataList.clear();
			for(Province province : provinceList)
			{
				//dataList更新了数据
				dataList.add(province.getProvinceName());
			}
			//动态改变adapter已绑定的列表信息
			adapter.notifyDataSetChanged();
			//列表定位到第一位
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;

		}
		else 
		{
			queryFromServer(null,"province");
		}

	}

	/*
	 * 查询选中省内所有的市，优先从数据库查询，如果没有就从服务器查询
	 */
	private void queryCities()
	{
		cityList=coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0)
		{
			dataList.clear();
			for(City city : cityList)
			{
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;

		}
		else
		{
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}

	}

	/*
	 * 查询选中城市的所有县，优先从数据库查询，如果没有就从服务器查询
	 */
	private void queryCounties()
	{
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0)
		{
			for(County county : countyList)
			{
				dataList.add(county.getCountyName());

			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		}
		else
		{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}


	/*
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void  queryFromServer(final String code,final String type)
	{
		String address;
		if(!TextUtils.isEmpty(code))
		{
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}
		else
		{
			address="http://www.weathe.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		
		//根据url发送HTTP请求并将返回数据存入数据库
        HttpUtil.sendHttpRequest(address,new HttpCallbackListener(){
        	//实现接口,处理从url端返回的数据，存入数据库
        	public void onFinish(String response)
        	{
        		boolean result =false;
        		if("province".equals(type))
        		{
        			//省级信息查询结果存入数据库
        			result=Utility.handleProvincesResponse(coolWeatherDB, response);
        		}
        		else if("city".equals(type))
        		{
        			//市级信息查询结果存入数据库
        			result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
        		}
        		else if("county".equals(type))
        		{
        			//县级信息查询结果存入数据库
        			result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
        		}
        		if(result)
        		{
        			//通过runOnUiThread()方法回到主线程处理逻辑
        			runOnUiThread(new Runnable(){
        				@Override
        				public void run(){
        					closeProgressDialog();
        					if("province".equals(type))
        					{
        						//再在数据库中进行省级数据查询
        						queryProvinces();
        					}
        					else if("city".equals(type))
        					{
        						//再在数据库中进行市级数据查询
        						queryCities();
        					}
        					else if("county".equals(type))
        					{
        						//再在数据库中进行县级数据查询
        						queryCounties();
        					}
        				}
        			});
        		}
        	}
        	
        	public void onError(Exception e){
        		//通过runOnUiThread()方法回到主线程处理逻辑
        		runOnUiThread(new Runnable(){
        			@Override
        			public void run()
        			{
        				closeProgressDialog();
        				Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
        			}
        		});
        	}
        	
        });                     
	}
	
	/*
	 * 显示进度对话框
	 */
	private void showProgressDialog()
	{
		if(progressDialog==null)
		{
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	/*
	 * 关闭进度对话框
	 */
	private void closeProgressDialog()
	{
		if(progressDialog!=null)
		{
			progressDialog.dismiss();
		}
	}
	
	/*
	 * 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出
	 */
	@Override
	public void onBackPressed()
	{
		if(currentLevel==LEVEL_COUNTY)
		{
			queryCities();
		}
		else if(currentLevel==LEVEL_CITY)
		{
			queryProvinces();
		}
		else
		{
			if(isFromWeatherActivity)
			{
				Intent intent=new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
