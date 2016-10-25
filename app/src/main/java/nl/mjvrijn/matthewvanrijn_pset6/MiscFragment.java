package nl.mjvrijn.matthewvanrijn_pset6;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Scanner;

public class MiscFragment extends StatsFragment {

    protected void readTemplate() {
        InputStream is = getResources().openRawResource(R.raw.misc);
        Scanner s = new Scanner(is).useDelimiter("\\A");
        template = s.hasNext() ? s.next() : "";
    }

    public void setData(JSONObject json) {
        if(template == null) {
            readTemplate();
        }

        try {
            html = String.format(template,
                    json.getInt("PersonenautoSTotaal_86"),
                    (double) json.getInt("PersonenautoSTotaal_86") / json.getInt("AantalInwoners_5"),
                    json.getDouble("PersonenautoSPerHuishouden_91"),
                    json.getInt("PersonenautoSJongerDan6Jaar_87"),
                    (double) json.getInt("PersonenautoSJongerDan6Jaar_87") / json.getInt("PersonenautoSTotaal_86") * 100,
                    json.getInt("PersonenautoS6JaarEnOuder_88"),
                    (double) json.getInt("PersonenautoS6JaarEnOuder_88") / json.getInt("PersonenautoSTotaal_86") * 100,
                    json.getInt("PersonenautoSBrandstofBenzine_89"),
                    (double) json.getInt("PersonenautoSBrandstofBenzine_89") / json.getInt("PersonenautoSTotaal_86") * 100,
                    json.getInt("PersonenautoSOverigeBrandstof_90"),
                    (double) json.getInt("PersonenautoSOverigeBrandstof_90") / json.getInt("PersonenautoSTotaal_86") * 100,
                    json.getInt("Bedrijfsmotorvoertuigen_93"),
                    (double) json.getInt("Bedrijfsmotorvoertuigen_93") / json.getInt("AantalInwoners_5"),
                    json.getInt("Motortweewielers_94"),
                    (double) json.getInt("Motortweewielers_94") / json.getInt("AantalInwoners_5"),
                    json.getInt("OppervlakteTotaal_100"),
                    json.getInt("OppervlakteLand_101"),
                    (double) json.getInt("OppervlakteLand_101") / json.getInt("OppervlakteTotaal_100") * 100,
                    json.getInt("OppervlakteWater_102"),
                    (double) json.getInt("OppervlakteWater_102") / json.getInt("OppervlakteTotaal_100") * 10
            );

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(webView != null) {
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }
}
