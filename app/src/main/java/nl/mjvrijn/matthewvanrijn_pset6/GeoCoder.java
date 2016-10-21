package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class GeoCoder {
    private Context context;

    public GeoCoder(Context c) {
        context = c;
    }

    public void requestBuurtID(double lat, double lon) {
        Double[] coords = {lat,lon};

        new LocationTask().execute(coords);
    }

    private class LocationTask extends AsyncTask<Double, Integer, String> {
        @Override
        protected String doInBackground(Double... params) {
            double latitude = params[0];
            double longitude = params[1];

            try {
                String url = String.format(Locale.ENGLISH, "https://mjvrijn.nl/api/buurt.php?lat=%f&lon=%f", latitude, longitude);

                System.out.println(url);
                Scanner s = new Scanner(new URL(url).openStream()).useDelimiter("\\A");
                return s.hasNext() ? s.next() : "";
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Map<String, String> id = new HashMap<>();
            String name = null;

            try {
                System.out.println(result);
                JSONObject response = new JSONObject(result);

                id.put("2014", response.getString("id_2014"));
                id.put("2015", response.getString("id_2015"));
                id.put("2016", response.getString("id_2016"));

                name = response.getString("name");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            ((MainActivity) context).onDBResult(id, name);
        }
    }

}
