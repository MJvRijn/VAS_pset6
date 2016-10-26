package nl.mjvrijn.matthewvanrijn_pset6;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

/* Utils
 *
 * The Utils class contains general purpose static utility methods, which are used throughout the
 * application.
 */

public class Utils {
    private static final String TAG = "Utils";

    public static final int MODE_GEO = 0;
    public static final int MODE_CBS = 1;

    /* This function gets the response from a URL and attempts to turn it into a JSONObject. Returns
     * null if an error occurs.
     */
    public static JSONObject getJsonFromServer(String url, int mode) {
        JSONObject output = null;

        try {
            InputStream is = new URL(url).openStream();
            output = new JSONObject(inputStreamToString(is));

            // The JSON from the CBS API is nested in an array called value
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

    /* An easy method of getting the entire String given by an InputStream. One of the methods from
     * http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
     */
    public static String inputStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
