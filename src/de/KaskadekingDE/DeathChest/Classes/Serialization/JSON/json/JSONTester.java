package de.KaskadekingDE.DeathChest.Classes.Serialization.JSON.json;

/**
 * Created by Kaskadeking on 19.05.2015.
 */
public class JSONTester {
    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
