
package com.example.equakes.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.equakes.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.equakes.ui.timeline.TimelineFragment.mFeedModelList;




public class MapFragment extends Fragment {

    private MapViewModel dashboardViewModel;
    private GoogleMap mMap;
    MapView mMapView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(MapViewModel.class);
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        mMapView = root.findViewById(R.id.mapView2);
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
                mMap.clear();

                LatLng sydney = null;
                LatLngBounds.Builder builder = LatLngBounds.builder();
                for (int i = 0 ; i < mFeedModelList.size() ; i++) {
                    sydney = new LatLng(Double.parseDouble(mFeedModelList.get(i).latitude), Double.parseDouble(mFeedModelList.get(i).longitude));
                    mMap.addMarker(new MarkerOptions().position(sydney).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_redicon)).title(mFeedModelList.get(i).getLocation()).snippet(mFeedModelList.get(i).pubdate+" "+mFeedModelList.get(i).getDepth()+" "+mFeedModelList.get(i).getMagnitude()));
                    builder.include(sydney);
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
            }
        });

        return root;
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