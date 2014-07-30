package cn.creatist.gift.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cn.creatist.gift.util.ApiClient;

// like models in Rails with Restful API
public abstract class Model implements Parcelable{ // HashMap implemented Serializable
	public String id;
	
	/**
	 * track fields changed or not
	 * */
	protected Map<String, Boolean> dirtyTracks;
	
	/**
	 * Construct a model from JSONObject
	 * */
	public Model (JSONObject obj){
		dirtyTracks = new HashMap<String, Boolean>();
		Iterator<?> keys = obj.keys();
		Map<String, Field> map = this.properties();
		while(keys.hasNext()){
	      String key = (String)keys.next();
	      try {
	        if(obj.get(key) != JSONObject.NULL && map.containsKey(key)){
	        	Field f = map.get(key);
	        	Class<?> klass = f.getType();
	        	if(klass.isAssignableFrom(Model.class)){// model <= JSONObject
	        		Object value = klass.getDeclaredConstructor(klass).newInstance(obj.getJSONObject(key));
	        		this.setObject(key, value);
    			}else if(List.class.isAssignableFrom(klass)){// list type field <= JSONArray
    				JSONArray ja = obj.getJSONArray(key);
    				Class<?> genericType = (Class<?>)  ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
    				List<Model> list = new ArrayList<Model>();
    				list = Model.list(genericType, ja);
    				this.setObject(key, list);
    			}else{// String/Integer/Float... <= Object
    				this.setObject(key, obj.get(key));
    			}
	        	dirtyTracks.put(key, false);// default false
		      }
	      } catch (Exception e){e.printStackTrace();}
		}
	}
	
	/**
	 * Construct a model from JSON string
	 * */
	public Model(String json){
		this((JSONObject) parseJSON(json));
	}
	
	/**
	 * Construct a model from HashMap
	 * */
	public Model (Map<String, Object> values){
		dirtyTracks = new HashMap<String, Boolean>();
		for(Map.Entry<String, Object> entry : values.entrySet()){
			try{
				String key = entry.getKey();
				setObject(key, entry.getValue());
				dirtyTracks.put(key, false);
			} catch (NoSuchFieldException e) {
			} catch (IllegalAccessException e) {}
		}
	}

	/**
	 * return a list of Model instance from a JSONArray
	 * */
	public static List<Model> list(Class<?> klass, JSONArray ja){
		List<Model> list = new ArrayList<Model>();
		for(int i = 0; i< ja.length(); i++){
			Model value;
			try {
				value = (Model) klass.getDeclaredConstructor(JSONObject.class).newInstance(ja.get(i));
				list.add(value);
			} catch (Exception e) {}
		}
		return list;
	}
	
//	@SuppressWarnings("unchecked")
//	public static <T extends Model> List<T> list(JSONArray ja) {
//		List<T> list = new ArrayList<T>();
//		for(int i = 0; i< ja.length(); i++){
//			T value;
//			try {
//				value = new T((JSONObject) ja.get(i));
//				list.add(value);
//			} catch (Exception e) {}
//		}
//        return list;
//    }
	
	/**
	 * whether a field is dirty
	 * */
	public boolean isDirty(String key){
		return dirtyTracks.get(key);
	}
	
	/**
	 * whether the object is dirty
	 * */
	public boolean isDirty(){
		for(Map.Entry<String, Boolean> entry : dirtyTracks.entrySet()){
			if(entry.getValue())return true;
		}
		return false;
	}
	
	protected abstract String getPlural();
	
	protected String memberUri(){
		return getPlural() + "/" + id;
	}
	
	//public static ArrayList<?> list(Class<?> klass, JSONArray arr){	}
	
	/**
	 * Fetch index list of models through API
	 * */
	public void fetchList(int page, int limit, JsonHttpResponseHandler responseHandler){
		RequestParams params = new RequestParams();
		params.put("page", page);
		params.put("limit", limit);
		ApiClient.get(getPlural(), params,responseHandler);
	}
	
	/**
	 * Fetch a model from API
	 * */
	public void fetch(JsonHttpResponseHandler responseHandler){
		ApiClient.get(memberUri(), responseHandler);
	}
	
	/**
	 * Delete a remote model
	 * */
	public void delete(JsonHttpResponseHandler responseHandler){
		ApiClient.delete(memberUri(), responseHandler);
	}
	
	/**
	 * Push the changes of field to remote
	 * */
	public void save(JsonHttpResponseHandler responseHandler) throws NoSuchFieldException, IllegalAccessException{
		RequestParams params = new RequestParams();
		boolean dirty = false;
		Iterator<String> it = this.dirtyTracks.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			if(this.dirtyTracks.get(key)){
				dirty = true;
				params.put(key, this.getObject(key).toString());
			}
		}
		if(dirty) ApiClient.put(memberUri(), params, responseHandler);
	}
	
	/**
	 * Get a field with a key
	 * */
	public Object getObject(String key) throws NoSuchFieldException, IllegalAccessException {
		return getClass().getField(key).get(this);
	}
	
	/**
	 * Set a field with a key
	 * */
	public void setObject(String key, Object obj) throws NoSuchFieldException, IllegalAccessException{
		getClass().getField(key).set(this, obj);
		this.dirtyTracks.put(key, true);
	}
	
	/**
	 * Declared fields in subclass with an id
	 * */
	public Map<String, Field> properties(){
		Field[] fs = getClass().getDeclaredFields();
		Map<String, Field> map = new HashMap<String, Field>();
		try {
			map.put("id", Model.class.getDeclaredField("id"));
		} catch (NoSuchFieldException e) {}
		for(int i = 0; i < fs.length; i++)map.put( fs[i].getName(), fs[i]);
		return map;
	}
	
	/**
	 * Get a values HashMap of properties
	 * */
	public Map<String, Object> values(){
		Map<String, Object> map = new HashMap<String, Object>();
		Iterator<String> iter = properties().keySet().iterator();
		while(iter.hasNext()){
			String key = iter.next();
			try {
				map.put(key, getObject(key));
			} catch (NoSuchFieldException e) {
			} catch (IllegalAccessException e) {}
		}
		return map;
	}
	
	/** Implements the Parcelable interface */
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flag) {
		out.writeString(getClass().getName());
		out.writeSerializable((Serializable) values());
	}
	
	public static final Parcelable.Creator<Model> CREATOR = new Parcelable.Creator<Model>() {  
        @Override  
        public Model createFromParcel(Parcel in) {
        	String klass = in.readString();
        	Serializable s = in.readSerializable();
            try {
				return (Model) Class.forName(klass).getDeclaredConstructor(Map.class).newInstance(s);
			} catch (Exception e) {
				Log.e("createFromParcel error", e.getMessage());
				return null;
			}
        }
  
        @Override  
        public Model[] newArray(int size) {  
            return new Model[size];  
        }  
    };
	
	/**
	 * render as a String
	 * */
	public String toString(){
		Map<String, Object> values = values();
		Iterator<String> iter = values.keySet().iterator();
		String result = "";
		while(iter.hasNext()){
			String key = iter.next();
			Object value = values.get(key);
			result += (", " + key + ":" +  (value == null ? " " : value.toString()) );
		}
		return result.replaceFirst(", ", "{") + "}";
	}
	
	/**
	 * Parse a string to JSONObject / JSONArray
	 * */
	public static Object parseJSON(String json){
		try {
			if(json.charAt(0) == '['){
				return new JSONArray(json);
			}else{
				return new JSONObject(json);
			}
		} catch (JSONException e) {
			return JSONObject.NULL;
		}
	}
}
