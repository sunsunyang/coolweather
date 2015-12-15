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
 * CoolWeatherDB�Ǹ������࣬���ǽ����Ĺ��췽��˽�л������ṩ��һ��getInstance������������ȡCoolWeatherDB��ʵ���������Ϳ��Ա�֤ȫ�ַ�Χ��ֻ����һ��coolWeather��ʵ����������������CoolWeatherDB
 * ���ṩ�����鷽����
 */
public class CoolWeatherDB {

	//���ݿ���
	public static final String DB_NAME="cool_weather";

	//���ݿ�汾
	public static final int VERSION=1;

	private static CoolWeatherDB coolWeatherDB;

	private SQLiteDatabase db;

	//���캯��˽�л�����ֹ���������ⲿ��ʵ����������ģʽ��������ֻ��һ�������Ķ��󣩣�ȫ�ַ�Χ��ֻ��һ��������ʵ��
	private CoolWeatherDB(Context context)
	{
		CoolWeatherOpenHelper dbHelper=new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
		db=dbHelper.getWritableDatabase();

	}

	//��ȡCoolWeatherDB��ʵ����synchronezed��ֹ����߳�ͬʱ�����������
	public synchronized static CoolWeatherDB getInstance(Context context)
	{
		if(coolWeatherDB==null)
		{
			coolWeatherDB=new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}
	/*
	 * ʡ��
	 */
	//��Provinceʵ���洢�����ݿ�=============================>"�洢"
	public void saveProvince(Province province)
	{
		if(province!=null)
		{
			//���ݿ��ֵ�洢����ContentValuesʵ��
			ContentValues values=new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());

			//�����ݿ�db�в����Province 
			db.insert("Province",null,values);

		}
	}

	//�����ݿ��ȡȫ�����е�ʡ����Ϣ============================>"��ȡ"
	public List<Province> loadProvinces()
	{
		List<Province> list=new ArrayList<Province>();
		//cursorλ�����еļ���
		Cursor cursor=db.query("Province", null, null, null, null, null,null);
		//ȡ��ÿһ�е�����
		if(cursor.moveToFirst())
		{
			//���л�ȡ��province"��Ϣ��������list
			do{
				//��province������
				Province province=new Province();
				//����id����ֵ
				province.setId(cursor.getInt(cursor.getColumnIndex("Id")));
				//provinceName����ֵ
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				//provinceCode����ֵ
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_Code")));
				list.add(province);
			}while(cursor.moveToNext());
		}
		if(cursor!=null)
		{
			//�ر��α��ͷ���Դ
			cursor.close();
		}
		//����list
		return list;
	}
	/*
	 * ����
	 */
	//��Cityʵ���洢�����ݿ�
	public void saveCity(City city)
	{
		if(city!=null)
		{
			ContentValues values=new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			//province_idΪ�����County�����Province��
			values.put("province_id",city.getProvinceId());
			db.insert("City", null, values);
		}
	}

	//�����ݿ��ȡȫ�����г�����Ϣ
	public List<City> loadCities(int provinceId)
	{
		List<City> list=new ArrayList<City>();
		//����ʡ�ļ�ֵprovince_id����ȡ������Ϣ,ִ�в�ѯ��������������Ϣ�����Զ����롱cursor���ĵ�һ��
		Cursor cursor=db.query("City", null, "province_id=?", new String[]{String.valueOf(provinceId)}, null, null, null);

		if(cursor.moveToFirst())
		{
			//���ж�ȡ��Ӧ��provinceId���ĳ��е���Ϣ
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
			//�ر��α꣬�ͷ���Դ
			cursor.close();
		}
		return list;
	}

	/*
	 * �س�
	 */
	//���س���Ϣ�洢�����ݿ�
	public  void saveCounty(County county)
	{
		ContentValues values=new ContentValues();
		values.put("county_name", county.getCountyName());
		values.put("county_code",county.getCountyCode());
		values.put("city_id", county.getCityId());
		db.insert("County", null, values);
	}

	//�����ݿ��ȡ��Ӧ���е��س���Ϣ
	public List<County> loadCounties(int cityId)
	{
		List<County> list=new ArrayList<County>();
		//��ѯ��county��,��ȡ�α�cursor
		Cursor cursor=db.query("County", null, "city_Id=?", new String[]{String.valueOf(cityId)}, null, null, null);
		//���ж�ȡ��Ӧ��cityId�����س�
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
