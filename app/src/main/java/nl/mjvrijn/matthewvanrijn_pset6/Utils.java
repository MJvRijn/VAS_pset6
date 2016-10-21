package nl.mjvrijn.matthewvanrijn_pset6;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by matthew on 21-10-16.
 */

public class Utils {
    private static final String TAG = "Utils";

    public static final int MODE_GEO = 0;
    public static final int MODE_CBS = 1;

    public static JSONObject getJsonFromServer(String url, int mode) {
        JSONObject output = null;

        try {
            Scanner s = new Scanner(new URL(url).openStream()).useDelimiter("\\A");
            output = new JSONObject(s.hasNext() ? s.next() : "");

            if(mode == MODE_CBS) {
                output = output.getJSONArray("value").getJSONObject(0);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to the server at " + url);
        } catch (JSONException e) {
            Log.e(TAG, "The server at " + url + " did not return valid JSON");
        }

        return output;
    }
}
