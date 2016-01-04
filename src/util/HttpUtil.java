package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
//创建http连接，获取url端的数据
public class HttpUtil {
	
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener)
	{
		new Thread(new Runnable(){
			public void run()
			{
				//create a http connection
				HttpURLConnection connection=null;
				try
				{
					URL url=new URL(address);
					//open connection with url
					connection=(HttpURLConnection)url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);          
					InputStream in=connection.getInputStream();
					BufferedReader reader =new BufferedReader(new InputStreamReader(in)); 
					StringBuilder response=new StringBuilder();
					String line;
					while((line=reader.readLine())!=null)
					{
						response.append(line);
					}
					if(listener!=null)
					{
						listener.onFinish(response.toString());
					}
				}
				catch(Exception e)
				{
					if(listener!=null)
					{
						listener.onError(e);
					}
				}
				finally
				{
					if(connection!=null)
					{
						connection.disconnect();
					}
				}
			}
		}).start();
	}

}
