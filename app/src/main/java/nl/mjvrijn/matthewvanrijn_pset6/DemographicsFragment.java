package nl.mjvrijn.matthewvanrijn_pset6;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/* DemographicsFragment
 *
 * This class is a fragment used to display demographic statistics. To display the statistics it
 * inserts the stats into an HTML template and inserts the stats before displaying it in a WebView.
 */

public class DemographicsFragment extends StatsFragment {

    /* Read the HTML template from the raw resource folder.
     */
    protected void readTemplate() {
        InputStream is = getResources().openRawResource(R.raw.demographics);
        template = Utils.inputStreamToString(is);
    }

    /* Format the stats in a json file into the template and display the result in a webview.
     */
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
                    json.getDouble("GemiddeldeHuishoudensgrootte_32"),
                    json.getInt("Bevolkingsdichtheid_33")
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Do not load into the webview is the fragment view hasn't been made yet.
        if(webView != null) {
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }
}
