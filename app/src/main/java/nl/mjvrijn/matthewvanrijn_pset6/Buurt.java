package nl.mjvrijn.matthewvanrijn_pset6;

import java.io.Serializable;

public class Buurt implements Serializable {
    private String id;
    private String name;
    private Demographics demographics;

    public Buurt(String i, String n) {
        id = i;
        name = n;
    }

    public String getId() {
        return id;
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
