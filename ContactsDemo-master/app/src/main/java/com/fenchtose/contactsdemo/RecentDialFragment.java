package com.fenchtose.contactsdemo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fenchtose.contactsdemo.adapter.RecentDialAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jay Rambhia on 11/06/15.
 */
public class RecentDialFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private List<String> items;

    private final String SAVED_ITEMS = "saved_items";

    public static RecentDialFragment newInstance() {
        return new RecentDialFragment();
    }

    public RecentDialFragment() {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstaceState) {
        mRecyclerView = (RecyclerView)inflater
                .inflate(R.layout.oaetest, parent, false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return mRecyclerView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            items = savedInstanceState.getStringArrayList(SAVED_ITEMS);
        }

        RecentDialAdapter mAdapter = new RecentDialAdapter(getActivity(), getItems());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (items != null) {
            outState.putStringArrayList(SAVED_ITEMS, new ArrayList<String>(items));
        }
    }

    private List<String> getItems() {
        if (items == null) {

            items = new ArrayList<>();

            for (char c = 'A'; c <= 'Z'; c++) {
                if (c % 5 == 2 || c % 3 == 1 || c % 6 == 4) {
                    items.add(String.valueOf(c));
                }
            }

            for (char c = 'A'; c <= 'Z'; c++) {
                if (c % 5 == 1 || c % 3 == 2 || c % 6 == 4) {
                    items.add(String.valueOf(c));
                }
            }

            Collections.shuffle(items);
        }

        return items;
    }
}
