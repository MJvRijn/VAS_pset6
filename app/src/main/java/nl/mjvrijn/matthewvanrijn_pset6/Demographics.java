package nl.mjvrijn.matthewvanrijn_pset6;


import java.util.ArrayList;

public class Demographics {
    public static final String[] POP_CATEGORY_NAMES = {"0-15", "15-25", "25-45", "45-65", "65+"};
    public static final String[] MARRIAGE_CATEGORY_NAMES= {"Ongehuwd", "Gehuwd", "Verweduwd"};

    private int pop;
    private int[] pop_categories;
    private int men;
    private int women;
    private double[] pop_categories_pct;
    private int[] marriage;
    private int households;
    private double households_avg_size;
    private int births;
    private double births_per_1000;
    private int deaths;
    private double deaths_per_1000;

    public Demographics(ArrayList<String> d) {
        ArrayList<Integer> data = new ArrayList<>();

        for(String val : d) {
            data.add(Integer.parseInt(val));
        }

        pop = data.get(0);
        men = data.get(1);
        women = data.get(2);
        pop_categories = new int[]{data.get(3), data.get(4), data.get(5), data.get(6), data.get(7)};

        pop_categories_pct = new double[pop_categories.length];
        for(int i = 0; i < pop_categories.length; i++) {
            pop_categories_pct[i] = ((double) pop_categories[i]/pop) * 100;
        }

        marriage = new int[]{data.get(8)+data.get(10), data.get(9), data.get(11)};

        households = data.get(12);
        households_avg_size = (double) pop/households;

        births = data.get(13);
        births_per_1000 = ((double) births / pop) * 1000;
        deaths = data.get(12);
        deaths_per_1000 = ((double) deaths / pop) * 1000;
    }

    // Auto generated getters


    public int getPop() {
        return pop;
    }

    public int[] getPop_categories() {
        return pop_categories;
    }

    public double[] getPop_categories_pct() {
        return pop_categories_pct;
    }

    public int[] getMarriage() {
        return marriage;
    }

    public int getHouseholds() {
        return households;
    }

    public double getHouseholds_avg_size() {
        return households_avg_size;
    }

    public int getBirths() {
        return births;
    }

    public double getBirths_per_1000() {
        return births_per_1000;
    }

    public int getDeaths() {
        return deaths;
    }

    public double getDeaths_per_1000() {
        return deaths_per_1000;
    }
}
