An ORM mapping and converting JSON to Objects in Java, supporting RESTful API access, dirty tracks and Parcelable implementation:

    public class Item extends Model {
      public String title;
      public Double price;
      public String cover_url;
      public Integer count;
      
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
    }