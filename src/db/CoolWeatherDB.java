package db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import model.City;
import model.County;
import model.Province;
/*
 * CoolWeatherDB是个单例类，我们将它的构造方法私有化，并提供了一个getInstance（）方法来获取CoolWeatherDB的实例，这样就可以保证全局范围内只会有一个coolWeather的实例，接下来我们在CoolWeatherDB
 * 中提供了六组方法：
 */
public class CoolWeatherDB {

	//数据库名
	public static final String DB_NAME="cool_weather";

	//数据库版本
	public static final int VERSION=1;

	private static CoolWeatherDB coolWeatherDB;

	private SQLiteDatabase db;

	//构造函数私有化，防止该类在类外部被实例化，单例模式（程序中只有一个这样的对象），全局范围内只有一个这样的实例
	private CoolWeatherDB(Context context)
	{
		CoolWeatherOpenHelper dbHelper=new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
		db=dbHelper.getWritableDatabase();

	}

	//获取CoolWeatherDB的实例，synchronezed防止多个线程同时访问这个方法
	public synchronized static CoolWeatherDB getInstance(Context context)
	{
		if(coolWeatherDB==null)
		{
			coolWeatherDB=new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}
	/*
	 * 省份
	 */
	//将Province实例存储到数据库=============================>"存储"
	public void saveProvince(Province province)
	{
		if(province!=null)
		{
			//数据库的值存储，用ContentValues实例
			ContentValues values=new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());

			//在数据库db中插入表Province 
			db.insert("Province",null,values);

		}
	}

	//从数据库读取全国所有的省份信息============================>"读取"
	public List<Province> loadProvinces()
	{
		List<Province> list=new ArrayList<Province>();
		//cursor位数据行的集合
		Cursor cursor=db.query("Province", null, null, null, null, null,null);
		//取出每一行的数据
		if(cursor.moveToFirst())
		{
			//逐行获取“province"信息，并存入list
			do{
				//表“province”对象
				Province province=new Province();
				//主键id设置值
				province.setId(cursor.getInt(cursor.getColumnIndex("Id")));
				//provinceName设置值
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				//provinceCode设置值
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_Code")));
				list.add(province);
			}while(cursor.moveToNext());
		}
		if(cursor!=null)
		{
			//关闭游标释放资源
			cursor.close();
		}
		//返回list
		return list;
	}
	/*
	 * 城市
	 */
	//将City实例存储到数据库
	public void saveCity(City city)
	{
		if(city!=null)
		{
			ContentValues values=new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			//province_id为外键，County表关联Province表
			values.put("province_id",city.getProvinceId());
			db.insert("City", null, values);
		}
	}

	//从数据库读取全国所有城市信息
	public List<City> loadCities(int provinceId)
	{
		List<City> list=new ArrayList<City>();
		//根据省的键值province_id来读取城市信息,执行查询语句后，如果有相关信息，会自动进入”cursor“的第一行
		Cursor cursor=db.query("City", null, "province_id=?", new String[]{String.valueOf(provinceId)}, null, null, null);

		if(cursor.moveToFirst())
		{
			//逐行读取对应“provinceId”的城市的信息
			do{
				City city=new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			}while(cursor.moveToNext());
		}
		if(cursor!=null)
		{
			//关闭游标，释放资源
			cursor.close();
		}
		return list;
	}

	/*
	 * 县城
	 */
	//将县城信息存储到数据库
	public  void saveCounty(County county)
	{
		ContentValues values=new ContentValues();
		values.put("county_name", county.getCountyName());
		values.put("county_code",county.getCountyCode());
		values.put("city_id", county.getCityId());
		db.insert("County", null, values);
	}

	//从数据库读取对应城市的县城信息
	public List<County> loadCounties(int cityId)
	{
		List<County> list=new ArrayList<County>();
		//查询表“county”,获取游标cursor
		Cursor cursor=db.query("County", null, "city_Id=?", new String[]{String.valueOf(cityId)}, null, null, null);
		//逐行读取对应“cityId”的县城
		if(cursor.moveToFirst())
		{
			do{
				County county=new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
				county.setCityId(cityId);
				list.add(county);
			}while(cursor.moveToNext());
		}
		if(cursor!=null)
		{
			cursor.close();
		}
		return list;
	}

}
