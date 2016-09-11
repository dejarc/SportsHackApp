package com.example.root.sportshacks;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        userId = sharedPref.getString(getString(R.string.user_id), "none");
        if(userId.equals("none")) { //this is a new user, start prompt to create an account
            setContentView(R.layout.create_account);
            createUserHelper(sharedPref);
        } else {
            createTabsHelper();
        }

    }
    private void createTabsHelper() {
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
    private void createUserHelper(final SharedPreferences myPreferences) {
        final Button login_button = (Button) findViewById(R.id.submit);
        assert login_button != null;
        login_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText username = (EditText)findViewById(R.id.name);
                String name_input = username.getText().toString();
                Log.i("information", "the field was" + name_input);
                EditText age = (EditText)findViewById(R.id.age);
                String age_input = age.getText().toString();
                Log.i("information", "the field was" + age_input);
                EditText email = (EditText)findViewById(R.id.email);
                String email_input = email.getText().toString();
                Log.i("information", "the field was" + email_input);
                new CreateUserActivity(myPreferences).execute(name_input, age_input, email_input);
            }
        });

    }
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Videos(), "Videos");
        adapter.addFragment(new Metrics(), "Metrics");
        adapter.addFragment(new Rankings(), "Rankings");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
    public class CreateUserActivity extends AsyncTask<String, Boolean, JSONObject> {
        private InputStream is = null;
        private BufferedReader br = null;
        private JSONObject jObj = null;
        private ProgressDialog dialog;
        private SharedPreferences myPreferences;
        static final String DB_URL = "http://10.0.3.2:3000/createLifter";
        public CreateUserActivity(SharedPreferences myPreferences) {
            this.myPreferences = myPreferences;
        }
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("logging in...");
            dialog.show();

        }
        protected JSONObject doInBackground(String...strings) {
            try {
                ArrayList<String> myKeys = new ArrayList<String>();
                String myQuery = URLEncoder.encode("user_name", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8");
                myQuery += "&" + URLEncoder.encode("age", "UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8");
                myQuery += "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(strings[2], "UTF-8");
                URL url = new URL(DB_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setUseCaches(false);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                OutputStreamWriter user_input = new OutputStreamWriter(conn.getOutputStream());
                user_input.write(myQuery);
                user_input.flush();
                user_input.close();
                int code = conn.getResponseCode();
                String msg = "Connection Code: " + code;
                Log.e("MYSQL", msg);
                is = conn.getInputStream();
                //conn.connect();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                try {

                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    jObj = new JSONObject(sb.toString());
                    try {
                        if(jObj.getString("_id") != null) {
                            publishProgress(true);
                        } else if(jObj.getString("error") != null){
                            publishProgress(false);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    is.close();

                } catch (Exception e) {
                    Log.e("Buffer Error", "Error converting result " + e.toString());
                }
            }  catch (IOException e) {
                Log.e("MYSQL", "Couldn't connect!");
                //e.printStackTrace();
            }


            return jObj;
        }
        protected void onProgressUpdate(Boolean...progress) {
            if(progress[0]) {
                dialog.setMessage("account created!");
            } else {
                dialog.setMessage("create account failed");
            }

        }
        protected void onPostExecute(JSONObject response) {
            try {
                if(response != null && response.getString("_id") != null) {//account created successfully
                    SharedPreferences.Editor editor = myPreferences.edit();
                    editor.putString(getString(R.string.user_id), response.getString("_id"));//save the users id
                    editor.apply();
                    dialog.setMessage("redirecting to home page...");
                    createTabsHelper();
                }
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}