package nl.mjvrijn.matthewvanrijn_pset6;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BevolkingFragment extends Fragment {

    private TextView textView;

    public BevolkingFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bevolking, container, false);

        textView = (TextView) view.findViewById(R.id.bevolking_text);

        return view;
    }

    public void setData(Buurt b) {
        textView.setText(b.getId());
    }
}
