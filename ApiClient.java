package cn.creatist.gift.util;

import com.loopj.android.http.*;

//http://loopj.com/android-async-http/
public class ApiClient {
	private static final String BASE_URL = "http://10.0.2.2/";
	private static AsyncHttpClient client = new AsyncHttpClient();
	
	 public static void get(String api, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		 client.get(fullUrl(api), params, responseHandler);
	 }
	 
	 public static void get(String api, AsyncHttpResponseHandler responseHandler){
		 client.get(fullUrl(api), null, responseHandler);
	 }
	 
	 public static void get(String api, String token, AsyncHttpResponseHandler responseHandler){
		 RequestParams params = new RequestParams("access_token", token);
		 client.get(api, params,  responseHandler);
	 }
	 
	  public static void post(String api, RequestParams params, AsyncHttpResponseHandler responseHandler) {
	    client.post(fullUrl(api), params, responseHandler);
	  }
	  
	  public static void put(String api, RequestParams params, AsyncHttpResponseHandler responseHandler){
	  	client.put(fullUrl(api), params, responseHandler);
	  }
	  
	  public static void delete(String api, AsyncHttpResponseHandler responseHandler){
	  	client.delete(fullUrl(api), responseHandler);
	  }

//	  private static String fullUrl(String api, int version) {
//	    return BASE_URL + "v" + version + "/" + api + ".json";
//	  }
	  
	  // default v1 version
	  private static String fullUrl(String api){
		  //return fullUrl(api, 1);
		  return BASE_URL + api + ".json";
	  }
}
