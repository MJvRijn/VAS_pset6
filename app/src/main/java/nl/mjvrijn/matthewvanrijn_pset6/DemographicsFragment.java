package nl.mjvrijn.matthewvanrijn_pset6;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Formatter;
import java.util.Locale;
import java.util.Scanner;

public class DemographicsFragment extends Fragment {
    private WebView webView;

    private String template;
    private String html;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_demographics, container, false);

        webView = (WebView) view.findViewById(R.id.demographics_webview);
        webView.setBackgroundColor(Color.TRANSPARENT);

        if(html != null) {
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }

        return view;
    }

    public void setData(JSONObject json) {
        if(template == null) {
            InputStream is = getResources().openRawResource(R.raw.demographics);
            Scanner s = new Scanner(is).useDelimiter("\\A");
            template = s.hasNext() ? s.next() : "";
        }

        System.out.println(template);

        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.ENGLISH);

        try {
            formatter.format(template,
                    // Population
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

            html = sb.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(webView != null) {
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }


    }
}
