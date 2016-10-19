package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class CBSAPI {
    private static final String TAG = "CBSAPI";
    private static final String ID_2016 = "83487NED";

    private Context context;

    public CBSAPI(Context c) {
        context = c;
    }

    public void getData(Buurt b) {
        new CBSTask().execute(b);
    }

    private void parseJson(String json, Buurt b) {
        try {
            System.out.println(json);
            JSONObject jso = new JSONObject(json);

        } catch (JSONException e) {
            Log.e(TAG, "Data retrieved from server is not valid JSON.");
            e.printStackTrace();
        }

        ((MainActivity) context).updateDisplay();
    }

    private class CBSTask extends AsyncTask<Buurt, Integer, String> {
        private Buurt buurt;

        @Override
        protected String doInBackground(Buurt... params) {
            try {
                buurt = params[0];
                String url = String.format("http://opendata.cbs.nl/ODataApi/odata/83487NED/TypedDataSet?$filter=WijkenEnBuurten+eq+'%s'", buurt.getId());

                Scanner s = new Scanner(new URL(url).openStream()).useDelimiter("\\A");
                return s.hasNext() ? s.next() : "";
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /* Then update the data set and notify the adapter. */
        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);

            parseJson(json, buurt);
        }
    }
}
