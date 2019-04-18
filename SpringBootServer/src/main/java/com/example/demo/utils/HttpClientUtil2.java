package com.example.demo.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;



public class HttpClientUtil2 {

	public  String doPostupload(File file, String url,String deviceid,String ip,String macaddress) {
	    HttpClient httpClient = null;
	    HttpPost httpPost = null;
	 
	     // 把文件转换成流对象FileBody
	    FileBody bin = new FileBody(file);
	    String  result = null;
	    try {
	         //***************注意这里的代码******
	        httpClient = new SSLClient();
	        httpPost = new HttpPost(url);
	        MultipartEntity reqEntity = new MultipartEntity();
	      //封装其他参数到Stringbody（需要把int转成String再放入）
	        StringBody Videodeviceid = new StringBody(deviceid);
	        StringBody Videoip = new StringBody("192."+ip);
	        StringBody Videomacaddress = new StringBody(macaddress);//type为int 
	      //参数放入请求实体（包括文件和其他参数）
	        reqEntity.addPart("snapshot", bin);
	        reqEntity.addPart("deviceid", Videodeviceid);
	        reqEntity.addPart("ip", Videoip);
	        reqEntity.addPart("macaddress", Videomacaddress);
	  
	        httpPost.setEntity(reqEntity);
	 
	        HttpResponse httpResponse = httpClient.execute(httpPost);
	        if(httpResponse != null){  
                HttpEntity resEntity = httpResponse.getEntity();  
                if(resEntity != null){  
                    result = EntityUtils.toString(resEntity,"utf-8");  
                }  
            }  
	 
	          //String body = result.getResponseString(); // body即为服务器返回的内容
	 
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return result;
	}
	
	
	
	public String doPost(String url,Map<String,String> map,String charset){  
        HttpClient httpClient = null;  
        HttpPost httpPost = null;  
        String result = null;  
        try{  
            httpClient = new SSLClient();  
            httpPost = new HttpPost(url);  
            //设置参数
            
            List<NameValuePair> list = new ArrayList<NameValuePair>();  
            Iterator iterator = map.entrySet().iterator();  
            while(iterator.hasNext()){  
                Entry<String,String> elem = (Entry<String, String>) iterator.next();  
                list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));  
            }  
            if(list.size() > 0){  
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);  
                httpPost.setEntity(entity);  
            }  
            HttpResponse response = httpClient.execute(httpPost);  
            if(response != null){  
                HttpEntity resEntity = response.getEntity();  
                if(resEntity != null){  
                    result = EntityUtils.toString(resEntity,charset);  
                }  
            }  
        }catch(Exception ex){  
            ex.printStackTrace();  
        }  
        return result;  
    }  
    
    //生成menu
    public String doPost(String url,String param,String charset){
    	 HttpClient httpClient = null;  
         HttpPost httpPost = null;  
         String result = null;  
         try{  
             httpClient = new SSLClient();  
             httpPost = new HttpPost(url);  
             //设置参数  
             httpPost.setEntity(new StringEntity(param,charset));  
             HttpResponse response = httpClient.execute(httpPost);  
             if(response != null){  
                 HttpEntity resEntity = response.getEntity();  
                 if(resEntity != null){  
                     result = EntityUtils.toString(resEntity,charset);  
                 }  
             }  
         }catch(Exception ex){  
             ex.printStackTrace();  
         }  
         return result;  
    }
    
    
   
	
}
