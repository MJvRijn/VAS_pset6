package nl.mjvrijn.matthewvanrijn_pset6;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by matthew on 24-10-16.
 */

public class DemographicsFragment extends StatsFragment {

    protected void readTemplate() {
        InputStream is = getResources().openRawResource(R.raw.demographics);
        Scanner s = new Scanner(is).useDelimiter("\\A");
        template = s.hasNext() ? s.next() : "";
    }

    public void setData(JSONObject json) {
        if(template == null) {
            readTemplate();
        }

        try {
            html = String.format(template,
                    json.getInt("AantalInwoners_5"),
                    json.getInt("Mannen_6"),
                    ((double) json.getInt("Mannen_6")/json.getInt("AantalInwoners_5")) * 100,
                    json.getInt("Vrouwen_7"),
                    ((double) json.getInt("Vrouwen_7")/json.getInt("AantalInwoners_5")) * 100,
                    json.getInt("k_0Tot15Jaar_8"),
                    ((double) json.getInt("k_0Tot15Jaar_8")/json.getInt("AantalInwoners_5")) * 100,
                    json.getInt("k_15Tot25Jaar_9"),
                    ((double) json.getInt("k_15Tot25Jaar_9")/json.getInt("AantalInwoners_5")) * 100,
                    json.getInt("k_25Tot45Jaar_10"),
                    ((double) json.getInt("k_25Tot45Jaar_10")/json.getInt("AantalInwoners_5")) * 100,
                    json.getInt("k_45Tot65Jaar_11"),
                    ((double) json.getInt("k_45Tot65Jaar_11")/json.getInt("AantalInwoners_5")) * 100,
                    json.getInt("k_65JaarOfOuder_12"),
                    ((double) json.getInt("k_65JaarOfOuder_12")/json.getInt("AantalInwoners_5")) * 100,
                    json.getInt("GeboorteTotaal_24"),
                    ((double) json.getInt("GeboorteTotaal_24")/json.getInt("AantalInwoners_5")) * 1000,
                    json.getInt("SterfteTotaal_26"),
                    ((double) json.getInt("SterfteTotaal_26")/json.getInt("AantalInwoners_5")) * 1000,
                    json.getInt("Ongehuwd_13") + json.getInt("Gescheiden_15"),
                    ((double) (json.getInt("Ongehuwd_13") + json.getInt("Gescheiden_15"))/json.getInt("AantalInwoners_5")) * 100,
                    json.getInt("Gehuwd_14"),
                    ((double) json.getInt("Gehuwd_14")/json.getInt("AantalInwoners_5")) * 100,
                    json.getInt("Verweduwd_16"),
                    ((double) json.getInt("Verweduwd_16")/json.getInt("AantalInwoners_5")) * 100,
                    json.getInt("HuishoudensTotaal_28"),
                    json.getDouble("GemiddeldeHuishoudensgrootte_32")
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(webView != null) {
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }
}
