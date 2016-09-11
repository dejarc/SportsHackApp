package com.example.root.sportshacks;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by root on 9/10/16.
 */
public class Rankings extends Fragment{
    public Rankings() {

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.speed_set, container, false);
        return rootView;
    }
}
