package com.example.root.sportshacks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by root on 9/10/16.
 */
public class Metrics extends Fragment {
    public Metrics() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.drawer_view, container, false);
        fetchUserMetrics(getActivity(), rootView);
        return rootView;
    }

    private void fetchUserMetrics(Activity myActivity, View myView) {
        new FetchMetrics(myActivity, myView).execute();
    }

    public class FetchMetrics extends AsyncTask<Void, Void, ArrayList<String>> {
        private InputStream is = null;
        private BufferedReader br = null;
        private JSONArray jArr = null;
        private String json = "";
        private ProgressDialog dialog;
        private Activity myActivity;
        private View myView;
        static final String DB_URL = "http://10.0.3.2:3000/";

        public FetchMetrics(Activity activity, View rootView) {
            myActivity = activity;
            myView = rootView;
        }

        protected void onPreExecute() {
            dialog = new ProgressDialog(myActivity);
            dialog.setMessage("Finding your metrics...");
            dialog.show();

        }

        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> myLifts = new ArrayList<String>();
            try {
                //HttpClient httpClient = HttpClientBuilder.create().build();
                URL url = new URL(DB_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                int response = conn.getResponseCode();
                Log.i("infor", "the response code was " + response);
                if(response != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                try {
                    jArr = new JSONArray(sb.toString());
                    Log.i("infor", "the length of the array is " + jArr.length());
                    for (int i = 0; i < jArr.length(); i++) {
                        JSONObject objectInArray = jArr.getJSONObject(i);
                        JSONArray lifts = objectInArray.getJSONArray("lifts");
                        for(int index = 0; index < lifts.length(); index++) {
                            JSONObject objectInLifts = lifts.getJSONObject(index);
                            double suc_percent = ((double)objectInLifts.getInt("success") / (double)objectInLifts.getInt("total"));
                            String label = objectInLifts.getString("exercise") + " " + (int)(suc_percent * 100) + "%";
                            myLifts.add(label);
                        }
                        //myParks.add(temp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                Log.e("MONGO", "Couldn't connect!");
                //e.printStackTrace();
            }

            return myLifts;
        }

        protected void onPostExecute(ArrayList<String> lifts) {
            String[] allLifts = new String[lifts.size()];
            for (int i = 0; i < lifts.size(); i++) {
                allLifts[i] = lifts.get(i);
            }
            //super.onPostExecute(parks);
            ListView temp = (ListView) myView.findViewById(R.id.left_drawer);
            // Set the adapter for the list view
            temp.setAdapter(new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.drawer_list_item, allLifts));
            // dismiss progress dialog and update ui
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}

