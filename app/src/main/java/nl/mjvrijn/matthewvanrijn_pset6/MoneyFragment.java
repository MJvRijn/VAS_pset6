package nl.mjvrijn.matthewvanrijn_pset6;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Scanner;

public class MoneyFragment extends StatsFragment {

    protected void readTemplate() {
        InputStream is = getResources().openRawResource(R.raw.money);
        Scanner s = new Scanner(is).useDelimiter("\\A");
        template = s.hasNext() ? s.next() : "";
    }

    public void setData(JSONObject json) {
        if(template == null) {
            readTemplate();
        }

        try {
            html = String.format(template,
                    json.getInt("AantalInkomensontvangers_64"),
                    (double) json.getInt("AantalInkomensontvangers_64") / json.getInt("AantalInwoners_5") * 100,
                    json.getDouble("GemiddeldInkomenPerInkomensontvanger_65") * 1000,
                    json.getInt("k_40PersonenMetLaagsteInkomen_67"),
                    json.getInt("k_20PersonenMetHoogsteInkomen_68"),
                    json.getInt("k_40HuishoudensMetLaagsteInkomen_70"),
                    json.getInt("k_20HuishoudensMetHoogsteInkomen_71"),
                    json.getInt("PersonenPerSoortUitkeringBijstand_74"),
                    (double) json.getInt("PersonenPerSoortUitkeringBijstand_74") / json.getInt("AantalInwoners_5") * 100,
                    json.getInt("PersonenPerSoortUitkeringAO_75"),
                    (double) json.getInt("PersonenPerSoortUitkeringAO_75") / json.getInt("AantalInwoners_5") * 100,
                    json.getInt("PersonenPerSoortUitkeringWW_76"),
                    (double) json.getInt("PersonenPerSoortUitkeringWW_76") / json.getInt("AantalInwoners_5") * 100,
                    json.getInt("PersonenPerSoortUitkeringAOW_77"),
                    (double) json.getInt("PersonenPerSoortUitkeringAOW_77") / json.getInt("AantalInwoners_5") * 100
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(webView != null) {
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }
}
