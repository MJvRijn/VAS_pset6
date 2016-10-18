package nl.mjvrijn.matthewvanrijn_pset6;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import jsqlite.Exception;
import jsqlite.Stmt;

public class GeoCoder {
    private Context context;

    public GeoCoder(Context c) {
        context = c;
        checkDB();
    }

    private void checkDB() {
        File f = new File(context.getFilesDir(), "buurten.sqlite");

        if(!f.exists()) {
            try {
                InputStream is = context.getResources().openRawResource(R.raw.buurt_2016);
                FileOutputStream os = new FileOutputStream(f);
                byte[] buffer = new byte[1024];
                int read;

                while((read = is.read(buffer)) > 0) {
                    os.write(buffer, 0, read);
                }

                is.close();
                os.close();
            } catch (java.lang.Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void requestBuurtID(double lat, double lon) {
        Double[] coords = {lat,lon};

        new DBTask().execute(coords);
    }

    public class DBTask extends AsyncTask<Double, Integer, String> {
        @Override
        protected String doInBackground(Double... params) {
            String id = null;
            double latitude = params[0];
            double longitude = params[1];

            File f = new File(context.getFilesDir(), "buurten.sqlite");

            try {
                jsqlite.Database db = new jsqlite.Database();
                db.open(f.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE);

                String query = "select bu_code from buurt_2016 where Within(Transform(MakePoint("+longitude+","+latitude+", 4326), 28992), Geometry)=1;";
                Stmt statement = db.prepare(query);

                if (statement.step()) {
                    id = statement.column_string(0);
                }

                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return id;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ((MainActivity) context).onDBResult(result);
        }
    }

}
