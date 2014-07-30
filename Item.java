package cn.creatist.gift.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class Item extends Model {
	public String title;
	public Double price;
	public String small_image;
	public String large_image;
	
	public Item(JSONObject obj) {
		super(obj);
	}
	
	public Item (Map<String, Object> values){
		super(values);
	}
	
	public Item (String json){
		super(json);
	}

	@Override
	protected String getPlural() {
		return "items";
	}
	
	public static List<Item> list(JSONArray ja){
		List<Item> list = new ArrayList<Item>();
		for(int i = 0; i< ja.length(); i++){
			try {
				Item value = new Item(ja.getJSONObject(i));
				list.add(value);
			} catch (Exception e) {}
		}
		return list;
	}
}
