package nl.mjvrijn.matthewvanrijn_pset6;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

public class DemographicsFragment extends Fragment {

    private WebView webView;
    private String html;

    public DemographicsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("bob", "Fragment saved");
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d("bob", "Fragment restored");
    }

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

    public void setData(Buurt b) {
        Demographics d = b.getDemographics();

        // Pop
        String popText = String.format("<h2>Inwonersaantallen</h2><table><tr><td><b>Inwoners:</b></td><td>%d</td><td></td></tr>", d.getPop());
        popText += String.format("<tr><td><b>Mannen:</b></td><td>%d</td><td>(%.1f%%)</td></tr>", d.getMen(), d.getMenpct());
        popText += String.format("<tr><td><b>Vrouwen:</b></td><td>%d</td><td>(%.1f%%)</td></tr>", d.getWomen(), d.getWomenpct());

        for (int i = 0; i < 5; i++) {
            popText += String.format("<tr><td><b>%s:</b></td><td>%d</td><td>(%.1f%%)</td></tr>", Demographics.POP_CATEGORY_NAMES[i], d.getPop_categories()[i], d.getPop_categories_pct()[i]);
        }


        popText += "</table>";

        // Changes
        String changeText = "<h2>Veranderingen</h2><table>";
        changeText += String.format("<tr><td><b>Geboorte:</b></td><td>%d</td><td>(%.1f per 1000)</td></tr>", d.getBirths(), d.getBirths_per_1000());
        changeText += String.format("<tr><td><b>Sterfte:</b></td><td>%d</td><td>(%.1f per 1000)</td></tr></table>", d.getDeaths(), d.getDeaths_per_1000());

        // Households
        String housetext = "<h2>Huishoudens</h2><table>";

        for (int i = 0; i < 3; i++) {
            housetext += String.format("<tr><td><b>%s:</b></td><td>%d</td><td>(%.1f%%)</td></tr>", Demographics.MARRIAGE_CATEGORY_NAMES[i], d.getMarriage()[i], d.getMarriage_pct()[i]);
        }

        housetext += String.format("<tr><td><b>Huishoudens:</b></td><td>%d</td><td></td></tr>", d.getHouseholds());
        housetext += String.format("<tr><td><b>Gem. Huishoudengrootte:</b></td><td>%.1f</td><td></td></tr></table>", d.getHouseholds_avg_size());

        html = popText + changeText + housetext;

        if(webView != null) {
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            webView.setBackgroundColor(Color.TRANSPARENT);
        }


    }
}
