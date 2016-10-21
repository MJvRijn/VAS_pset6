package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class CBSAPI {
    private static final String TAG = "CBSAPI";
    private static final String ID_2016 = "83487NED";
    private static final String ID_2015 = "83220NED";
    private static final String ID_2014 = "82931NED";
    private static final String[] DATA_DEMOGRAPHICS_2016 = {"AantalInwoners_5", "Mannen_6", "Vrouwen_7",
            "k_0Tot15Jaar_8", "k_15Tot25Jaar_9", "k_25Tot45Jaar_10", "k_45Tot65Jaar_11",
            "k_65JaarOfOuder_12", "Ongehuwd_13", "Gehuwd_14", "Gescheiden_15", "Verweduwd_16",
            "HuishoudensTotaal_28"};
    private static final String[] DATA_DEMOGRAPHICS_2015 = {"GeboorteTotaal_24", "SterfteTotaal_26"};

    private Context context;

    public CBSAPI(Context c) {
        context = c;
    }

    public void getData(Buurt b) {
        new CBSTask().execute(b);
    }

    private void parseJson(ArrayList<String> json, Buurt b) {
        try {
            JSONObject data2016 = new JSONObject(json.get(0)).getJSONArray("value").getJSONObject(0);
            System.out.println(json.get(1));
            JSONObject data2015 = new JSONObject(json.get(1)).getJSONArray("value").getJSONObject(0);
            JSONObject data2014 = new JSONObject(json.get(2)).getJSONArray("value").getJSONObject(0);

            // Parse demographic data
            ArrayList<String> demographics = new ArrayList<>();
            for(String s : DATA_DEMOGRAPHICS_2016) {
                demographics.add(data2016.getString(s));
            }
            for(String s : DATA_DEMOGRAPHICS_2015) {
                demographics.add(data2015.getString(s));
            }
            b.setDemographics(new Demographics(demographics));
            System.out.println(demographics);

        } catch (JSONException e) {
            Log.e(TAG, "Data retrieved from server is not valid JSON.");
            e.printStackTrace();
        }

        ((MainActivity) context).updateDisplay();
    }

    private class CBSTask extends AsyncTask<Buurt, Integer, String> {
        private Buurt buurt;
        private ArrayList<String> responses = new ArrayList<>();

        @Override
        protected String doInBackground(Buurt... params) {
            try {
                buurt = params[0];
                String[] urls = new String[3];
                urls[0] = String.format("http://opendata.cbs.nl/ODataApi/odata/%s/TypedDataSet?$filter=WijkenEnBuurten+eq+'%s'", ID_2016, buurt.getId("2016"));
                urls[1] = String.format("http://opendata.cbs.nl/ODataApi/odata/%s/TypedDataSet?$filter=WijkenEnBuurten+eq+'%s'", ID_2015, buurt.getId("2015"));
                urls[2] = String.format("http://opendata.cbs.nl/ODataApi/odata/%s/TypedDataSet?$filter=WijkenEnBuurten+eq+'%s'", ID_2014, buurt.getId("2014"));

                for(String url : urls) {
                    Scanner s = new Scanner(new URL(url).openStream()).useDelimiter("\\A");
                    responses.add(s.hasNext() ? s.next() : "");
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        /* Then update the data set and notify the adapter. */
        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);

            parseJson(responses, buurt);
        }
    }
}
