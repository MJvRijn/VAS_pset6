package nl.mjvrijn.matthewvanrijn_pset6;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Created by matthew on 21-10-16.
 */

public class APIManager {
    private APIListener callback;
    private boolean locked;

    public APIManager() {
        locked = false;
    }

    public void getBuurtfromLocation(Location l, APIListener c) {
        if(!locked) {
            callback = c;
            locked = true;
            new APITask().execute(l.getLatitude(), l.getLongitude());
        } else {
            // todo: throw exception?
        }
    }

    private class APITask extends AsyncTask<Double, String, Integer> {
        private static final String GEO_API = "https://mjvrijn.nl/api/buurt.php?lat=%f&lon=%f";
        private static final String CBS_API = "http://opendata.cbs.nl/ODataApi/odata/%s/TypedDataSet?$filter=WijkenEnBuurten+eq+'%s'";
        private static final String ID_2016 = "83487NED";
        private static final String ID_2015 = "83220NED";
        private static final String ID_2014 = "82931NED";

        private static final String TAG = "APIManager";

        private JSONObject result;

        @Override
        protected Integer doInBackground(Double... params) {
            double latitude = params[0];
            double longitude = params[1];

            /* Get data from API 1 */
            String geolocator_url = String.format(Locale.ENGLISH, GEO_API, latitude, longitude);

            JSONObject geolocator = Utils.getJsonFromServer(geolocator_url, Utils.MODE_GEO);

            if(geolocator == null) {
                return 1;
            } else {
                /* Get data from API 2 */
                String url_2016;
                String url_2015;
                String url_2014;

                try {
                    url_2016 = String.format(CBS_API, ID_2016, geolocator.getString("id_2016"));
                    url_2015 = String.format(CBS_API, ID_2015, geolocator.getString("id_2015"));
                    url_2014 = String.format(CBS_API, ID_2014, geolocator.getString("id_2014"));
                } catch (JSONException e) {
                    Log.e(TAG, "The JSON received from the geo api is incomplete");
                    return 1;
                }

                JSONObject json_2016 = Utils.getJsonFromServer(url_2016, Utils.MODE_CBS);
                JSONObject json_2015 = Utils.getJsonFromServer(url_2015, Utils.MODE_CBS);
                JSONObject json_2014 = Utils.getJsonFromServer(url_2014, Utils.MODE_CBS);

                /* Merge JSONObjects */
                Iterator<String> iterator2016 = json_2016.keys();
                Map<String, String> replacements = new HashMap<>();

                // Find missing values in 2016 data
                while (iterator2016.hasNext()) {
                    String key = iterator2016.next();

                    if (json_2016.isNull(key)) {
                        String[] parts = key.split("_");
                        replacements.put(parts[parts.length - 2], key);
                    }
                }

                // Try to replace values with older data
                for (String missing : replacements.keySet()) {
                    boolean replaced = false;

                    // Search 2015 data
                    Iterator<String> iterator2015 = json_2015.keys();
                    while(iterator2015.hasNext()) {
                        String key = iterator2015.next();

                        try {
                            if (key.contains(missing) && !json_2015.isNull(key)) {
                                json_2016.put(replacements.get(missing), json_2015.get(key));
                                replaced = true;
                                break;
                            }
                        } catch (JSONException e) {
                            Log.w(TAG, "Failed to update data with " + key);
                        }
                    }

                    if(replaced) {
                        continue;
                    }

                    // Search 2014 data
                    Iterator<String> iterator2014 = json_2014.keys();
                    while(iterator2014.hasNext()) {
                        String key = iterator2014.next();

                        try {
                            if (key.contains(missing) && !json_2014.isNull(key)) {
                                json_2016.put(replacements.get(missing), json_2014.get(key));
                                break;
                            }
                        } catch (JSONException e) {
                            Log.w(TAG, "Failed to update data with " + key);
                        }
                    }
                }

                result = json_2016;
            }

            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            locked = false;

            if(integer == 0) {
                callback.onAPIResult(result);
            }

        }
    }

}
