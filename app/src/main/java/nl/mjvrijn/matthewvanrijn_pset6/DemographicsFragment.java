package nl.mjvrijn.matthewvanrijn_pset6;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DemographicsFragment extends Fragment {

    private TextView title;
    private TextView population;
    private TextView marriage;
    private TextView changes;

    public DemographicsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_demographics, container, false);

        title = (TextView) view.findViewById(R.id.demographics_title);
        population = (TextView) view.findViewById(R.id.demographics_population);
        marriage = (TextView) view.findViewById(R.id.demographics_marriage);
        changes = (TextView) view.findViewById(R.id.demographics_changes);

        return view;
    }

    public void setData(Buurt b) {
        Demographics d = b.getDemographics();

        String popText = String.format("<b>Inwoners:</b> %d<br>", d.getPop());

        for (int i = 0; i < 5; i++) {
            popText += String.format("<b>%s: </b>%d (%.1f%%)<br>", Demographics.POP_CATEGORY_NAMES[i], d.getPop_categories()[i], d.getPop_categories_pct()[i]);
        }

        title.setText("Bevolking");
        population.setText(Html.fromHtml(popText));


    }
}
