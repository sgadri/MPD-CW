
package com.example.equakes.ui.timeline;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.equakes.ui.MainActivity;
import com.example.equakes.R;
import com.example.equakes.helpers.adapters.RssFeedListAdapter;
import com.example.equakes.helpers.RssFeedModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.equakes.ui.MainActivity.backFlag;





public class TimelineFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeLayout;

    private TimelineViewModel homeViewModel;
    public static List<RssFeedModel> mFeedModelList;
    private String mFeedTitle;
    private String mFeedLink;
    private String mFeedDescription;
    private String mFeedpubdate;
    private String mFeedcategory;
    private String mFeedlatitude;
    private String mFeedlongitude;
    public RssFeedListAdapter rssFeedListAdapter;
    String dateString;
    private GoogleMap mMap;
    MapView mMapView;

    private int spincount=0;
    private String urlLink;
    private TextView magTxt,depthTxt,originTimeTxt,latLangsTxt,gmtTxt,nameOfCityTxt;

    Spinner spinnerLoc;
    List<String> loclist;
    ArrayAdapter<String> dataAdapter;
    private LinearLayout llViewOnListItemClick;
    FragmentActivity activity;



    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.mene_filter,menu);
        menu.getItem(0).setIcon(R.drawable.filter);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.all_item:
                new FetchFeedTask().execute((Void) null);
                return true;

            case R.id.bydate_item:
                dateString="";
                new FetchFeedTask().execute((Void) null);
                final AlertDialog.Builder mydialog1=new AlertDialog.Builder(getContext());
                LayoutInflater inflater1=LayoutInflater.from(getContext());
                View myview1=inflater1.inflate(R.layout.custom_filter_date,null);
                mydialog1.setView(myview1);
                final AlertDialog dialog1=mydialog1.create();
                dialog1.show();
                final TextView datetxt=myview1.findViewById(R.id.date_txt);

                datetxt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Calendar c = Calendar.getInstance();
                        int mYear = c.get(Calendar.YEAR);

                        final int mMonth = c.get(Calendar.MONTH);
                        int mDay = c.get(Calendar.DAY_OF_MONTH);


                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                    SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
                                    c.set(year,monthOfYear,dayOfMonth);
                                    dateString = format.format(c.getTime());
                                    datetxt.setText(dateString);
                                    }
                                }, mYear, mMonth, mDay);
                        datePickerDialog.show();
                    }
                });

                Button datefilterbtn=myview1.findViewById(R.id.filterbtn);
                datefilterbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TextUtils.isEmpty(dateString))
                        {
                            datetxt.setError("select date");
                        }
                        else {
                            rssFeedListAdapter.getFilter().filter(dateString);
                            dialog1.dismiss();
                        }
                    }
                });

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel = ViewModelProviders.of(this).get(TimelineViewModel.class);

        activity = getActivity();
        ((MainActivity)activity).setOnBackPressedListener(new BaseBackPressedListener(activity));

        View root = inflater.inflate(R.layout.fragment_timeline, container, false);

        mMapView = (MapView) root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
            }
        });

        setHasOptionsMenu(true);
        loclist=new ArrayList<String>();

        llViewOnListItemClick = root.findViewById(R.id.ll_view_on_list_item_click);
        magTxt = root.findViewById(R.id.magnitude_txt);
        depthTxt = root.findViewById(R.id.depth_txt);
        originTimeTxt = root.findViewById(R.id.origin_time_txt);
        latLangsTxt = root.findViewById(R.id.item_detail_txt);
        gmtTxt = root.findViewById(R.id.gmt_txt);
        nameOfCityTxt = root.findViewById(R.id.name_of_city_txt);


        mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        mSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefreshLayout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        new FetchFeedTask().execute((Void) null);

        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute((Void) null);
            }
        });
        if (spincount>1)
        {
            spinnerLoc.setAdapter(dataAdapter);
        }

        dataAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, loclist);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        return root;
    }


    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        String pubdate=null;
        String category=null;
        String latitude=null;
        String longitude=null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d("MainActivity", "Parsing name ==> " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                }
                else if (name.equalsIgnoreCase("description")) {
                    description = result;
                }
                else if (name.equalsIgnoreCase("pubDate")) {
                    pubdate = result;
                }
                else if (name.equalsIgnoreCase("category")) {
                    category = result;
                }
                else if (name.equalsIgnoreCase("geo:lat")) {
                    latitude = result;
                }
                else if (name.equalsIgnoreCase("geo:long")) {
                    longitude = result;
                }


                if (title != null && link != null && description != null && pubdate!=null && category!=null && latitude!=null
                        && longitude!=null
                ) {
                    if(isItem) {
                        RssFeedModel item = new RssFeedModel(title, link, description,pubdate,category,latitude,longitude);
                        items.add(item);
                    }
                    else {
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = description;
                        mFeedpubdate=pubdate;
                        mFeedcategory=category;
                        mFeedlatitude=latitude;
                        mFeedlongitude=longitude;
                    }

                    title = null;
                    link = null;
                    description = null;
                    pubdate=null;
                    category=null;
                    latitude=null;
                    longitude=null;
                    isItem = false;
                }
            }
            System.out.println("items = "+items);
            System.out.println("items = "+items.get(2).title);


            return items;
        } finally {
            inputStream.close();
        }
    }


    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            mFeedTitle = null;
            mFeedLink = null;
            mFeedDescription = null;
            mFeedpubdate=null;
            mFeedcategory=null;
            mFeedlatitude=null;
            mFeedlongitude=null;



            urlLink = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
        }

        @Override
        protected Boolean doInBackground(Void... voids) {


            if (isNetworkAvailable()==false)
            {
                return false;
            }

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                mFeedModelList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e("backgrounterror", "Error", e);
            } catch (XmlPullParserException e) {
                Log.e("backgrounterror", "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);

            if (success) {


                rssFeedListAdapter=new RssFeedListAdapter(mFeedModelList);

                rssFeedListAdapter.onClick = new RssFeedListAdapter.OnItemClicked() {
                    @Override
                    public void onItemClick(int position) {
                        backFlag = false;
                        mMap.clear();
                        originTimeTxt.setText(mFeedModelList.get(position).pubdate);
                        nameOfCityTxt.setText(mFeedModelList.get(position).getLocation());
                        depthTxt.setText(mFeedModelList.get(position).getDepth());
                        magTxt.setText(mFeedModelList.get(position).getMagnitude());
                        latLangsTxt.setText(mFeedModelList.get(position).latitude+","+mFeedModelList.get(position).longitude);

                        LatLng sydney = new LatLng(Double.parseDouble(mFeedModelList.get(position).latitude), Double.parseDouble(mFeedModelList.get(position).longitude));
                        mMap.addMarker(new MarkerOptions().position(sydney).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_redicon)).title(mFeedModelList.get(position).getLocation()).snippet(mFeedModelList.get(position).pubdate+" "+mFeedModelList.get(position).getDepth()+" "+mFeedModelList.get(position).getMagnitude()));


                        CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(10).build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        llViewOnListItemClick.setVisibility(View.VISIBLE);
                        mSwipeLayout.setVisibility(View.GONE);
                    }
                };

                mRecyclerView.setAdapter(rssFeedListAdapter);

                for (int i=0;i<mFeedModelList.size();i++)
                {
                    String title=mFeedModelList.get(i).description;

                    String[] magnitude = new String[2];
                    String[] depth = new String[2];
                    String[] location1 = new String[2];

                    String[] parts = title.split(";");
                    magnitude = parts[4].split(":");
                    depth = parts[3].split(":");
                    location1 = parts[1].split(":");

                    mFeedModelList.get(i).setLocation(location1[1].trim());
                    mFeedModelList.get(i).setDepth(depth[1].trim());
                    mFeedModelList.get(i).setMagnitude(magnitude[1].trim());

                    String location="";
                    Pattern pattern = Pattern.compile("Location:(.*?);");
                    Matcher matcher = pattern.matcher(title);
                    if(matcher.find()){
                        location=matcher.group(1);
                    }
                    if (!loclist.contains(location))
                    {
                        loclist.add(location);
                    }

                    System.out.println("listss : "+loclist);
                }
            } else {
                Toast.makeText(getContext(),
                        "No Internet Connection",
                        Toast.LENGTH_LONG).show();
            }
        }
    }



    private boolean isNetworkAvailable() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            connected = true;
        }
        else
            connected = false;

        return connected;
    }
    private void getspinneradapter(Spinner spinner,ArrayAdapter<String> rssFeedListAdapter) {
        spinner.setAdapter(rssFeedListAdapter);
    }



    public class BaseBackPressedListener implements OnBackPressed {
        private final FragmentActivity activity;
        public BaseBackPressedListener(FragmentActivity activity) {
            this.activity = activity;
        }
        @Override
        public void onBackPressed() {
            backFlag = true;
            activity.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            llViewOnListItemClick.setVisibility(View.GONE);
            mSwipeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}