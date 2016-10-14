package com.sidzi.circleofmusic;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.sidzi.circleofmusic.adapters.TrackListAdapter;
import com.sidzi.circleofmusic.entities.Track;
import com.sidzi.circleofmusic.helpers.AudioEventHandler;
import com.sidzi.circleofmusic.helpers.OrmHandler;
import com.sidzi.circleofmusic.helpers.VerticalSpaceDecorationHelper;

import net.gotev.uploadservice.UploadService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {
    private static String com_url = "http://circleofmusic-sidzi.rhcloud.com/";
    private AudioEventHandler audioEventHandler;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
                requestPermissions(perms, 202);
            }
        }

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest eosCheck = new JsonObjectRequest(Request.Method.GET, com_url + "checkEOSVersion", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if ((int) response.get("eos_version") > BuildConfig.VERSION_CODE) {
                        startActivity(new Intent(MainActivity.this, EosActivity.class));
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(eosCheck);
        audioEventHandler = new AudioEventHandler();
        registerReceiver(audioEventHandler, new IntentFilter("com.sidzi.circleofmusic.PLAY_TRACK"));
        File music_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File[] list = music_dir.listFiles();
        String local_path;
        String local_name;
        OrmHandler orm = OpenHelperManager.getHelper(MainActivity.this, OrmHandler.class);
        try {
            Dao<Track, String> mTrack = orm.getDao(Track.class);
            for (File aList : list) {
                if (aList.isFile()) {
                    local_path = aList.getAbsolutePath();
                    local_name = local_path.substring(local_path.lastIndexOf("/") + 1);
                    mTrack.createIfNotExists(new Track(local_name, local_path, "local"));
                } else {
//                    TODO implement recursive function
                }
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(audioEventHandler);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            final View homeView = inflater.inflate(R.layout.fragment_home, container, false);
            final RecyclerView mRecyclerView;
            final RecyclerView.LayoutManager mLayoutManager;
            mRecyclerView = (RecyclerView) homeView.findViewById(R.id.rVTrackList);
            mLayoutManager = new LinearLayoutManager(getContext());
            if (mRecyclerView != null) {
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.addItemDecoration(new VerticalSpaceDecorationHelper(getContext()));
            }
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    TrackListAdapter mAdapter = new TrackListAdapter(getContext(), "local");
                    if (mRecyclerView != null) {
                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case 2:
                    break;
                case 3:
                    JsonArrayRequest trackRequest = new JsonArrayRequest(Request.Method.GET, com_url + "getTrackList", null, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            OrmHandler orm = OpenHelperManager.getHelper(getContext(), OrmHandler.class);
                            try {
                                Dao<Track, String> mTrack = orm.getDao(Track.class);
                                for (int i = 0; i < response.length(); i++) {
                                    mTrack.createIfNotExists(new Track(response.get(i).toString(), com_url + "streamTrack" + response.get(i).toString(), "remote"));
                                }
                                TrackListAdapter mAdapter = new TrackListAdapter(getContext(), "remote");

                                if (mRecyclerView != null) {
                                    mRecyclerView.setAdapter(mAdapter);
                                    mAdapter.notifyDataSetChanged();
                                }
                            } catch (SQLException | JSONException e) {
                                e.printStackTrace();
                            }
                            OpenHelperManager.releaseHelper();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            TrackListAdapter mAdapter = new TrackListAdapter(getContext(), "remote");

                            if (mRecyclerView != null) {
                                mRecyclerView.setAdapter(mAdapter);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    requestQueue.add(trackRequest);
                    break;
            }
            return homeView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Local";
                case 1:
                    return "Bucket";
                case 2:
                    return "Remote";
            }
            return null;
        }
    }
}