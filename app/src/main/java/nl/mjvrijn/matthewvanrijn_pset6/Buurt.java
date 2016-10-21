package nl.mjvrijn.matthewvanrijn_pset6;

import java.io.Serializable;
import java.util.Map;

public class Buurt implements Serializable {
    private Map<String, String> id;
    private String name;
    private Demographics demographics;

    public Buurt(Map<String, String> i, String n) {
        id = i;
        name = n;
    }

    public String getId(String year) {
        return id.get(year);
    }

    public String getName() {
        return name;
    }

    public void setDemographics(Demographics d) {
        demographics = d;
    }

    public Demographics getDemographics() {
        return demographics;
    }
}
