package nl.mjvrijn.matthewvanrijn_pset6;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.json.JSONObject;

/* StatsFragment
 *
 * StatsFragment is an abstract class for fragments which show statistics in a webview. Such fragments
 * must implement the functions readTemplate and setData.
 */

public abstract class StatsFragment extends Fragment {
    protected WebView webView;
    protected String template;
    protected String html;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        webView = (WebView) view.findViewById(R.id.fragment_webview);
        webView.setBackgroundColor(Color.TRANSPARENT);

        // When the view is created set the HTML is it has already been formatted
        if(html != null) {
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }

        return view;
    }

    protected abstract void readTemplate();

    public abstract void setData(JSONObject json);
}
