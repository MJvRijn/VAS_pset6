package nl.mjvrijn.matthewvanrijn_pset6;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/* HousingFragment
 *
 * This class is a fragment used to display housing statistics. To display the statistics it
 * inserts the stats into an HTML template and inserts the stats before displaying it in a WebView.
 */

public class HousingFragment extends StatsFragment {

    /* Read the HTML template from the raw resource folder.
     */
    protected void readTemplate() {
        InputStream is = getResources().openRawResource(R.raw.housing);
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
                    json.getInt("Woningvoorraad_34"),
                    json.getInt("GemiddeldeWoningwaarde_35") * 1000,
                    json.getInt("Huurwoning_61"),
                    json.getInt("Koopwoning_62"),
                    json.getInt("BouwjaarVoor2000_45"),
                    json.getInt("BouwjaarVanaf2000_46"),
                    json.getDouble("AfstandTotHuisartsenpraktijk_95"),
                    json.getDouble("AfstandTotGroteSupermarkt_96"),
                    json.getDouble("AfstandTotKinderdagverblijf_97"),
                    json.getDouble("AfstandTotSchool_98"),
                    json.getDouble("ScholenBinnen3Km_99")
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
