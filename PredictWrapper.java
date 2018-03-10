import org.json.*;
import java.util.Random;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.lang.IllegalArgumentException;

public class PredictWrapper {
  static {
    System.loadLibrary("wrapper");
    feature_map = new HashMap<String, Integer>() {
      {
        put("foo", 0);
        put("bar", 2);
      }
    };
  }

  private native int GetNumFeature();
  private native float Predict(boolean[] bitmap, float[] data, boolean pred_margin);
  private static Map<String, Integer> feature_map;

  public static float predict(String json_query) throws IllegalArgumentException {
    PredictWrapper jni = new PredictWrapper();
    int num_feature = jni.GetNumFeature();
    float[] data = new float[num_feature];
    boolean[] bitmap = new boolean[num_feature];
    for (int i = 0; i < num_feature; ++i) {
      bitmap[i] = false;
    }

    //System.out.println(json_query);
    JSONObject query = new JSONObject(json_query.trim());
    Iterator<?> keys = query.keys();
    while (keys.hasNext()) {
      String key = (String)keys.next();
      int feature_id;
      float feature_value;
      Integer o = feature_map.get(key);
      if (o == null) {
        // attempt to render as integer
        try {
          o = Integer.parseInt(key);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("no feature exists with name `"
                                             + key + "`'");
        }
      }
      feature_id = o.intValue();

      if (feature_id < 0 || feature_id >= num_feature) {
        throw new IllegalArgumentException("feature id must be between 0 and "
                                           + (num_feature - 1));
      }

      Object value = query.get((String)key);
      if (value instanceof String) {
        throw new IllegalArgumentException("categorical features not yet " 
                                           + "supported");
      } else if (value instanceof Integer) {
        feature_value = (float)((Integer)value).intValue();
      } else if (value instanceof Float) {
        feature_value = ((Float)value).floatValue();
      } else if (value instanceof Double) {
        feature_value = ((Double)value).floatValue();
      } else {
        throw new IllegalArgumentException("unrecognized value type");
      }

      bitmap[feature_id] = true;
      data[feature_id] = feature_value;
      //System.out.println("Feature " + feature_id
      //                   + " has value " + feature_value);
    }

    return jni.Predict(bitmap, data, false);
  }
}
