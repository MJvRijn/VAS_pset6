package nl.mjvrijn.matthewvanrijn_pset6;

import org.json.JSONObject;

/* APIListener
 *
 * Provide a callback interface for the APIManager.
 */

public interface APIListener {
    void onAPIResult(JSONObject b);
}
