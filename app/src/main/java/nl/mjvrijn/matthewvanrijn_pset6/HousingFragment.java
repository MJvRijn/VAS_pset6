package nl.mjvrijn.matthewvanrijn_pset6;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Scanner;

public class HousingFragment extends StatsFragment {

    protected void readTemplate() {
        InputStream is = getResources().openRawResource(R.raw.housing);
        Scanner s = new Scanner(is).useDelimiter("\\A");
        template = s.hasNext() ? s.next() : "";
    }

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

        if(webView != null) {
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }
}
