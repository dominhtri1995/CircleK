package com.financialwhirlpool.cvs.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.financialwhirlpool.cvs.Class.ClassGoogleMap;
import com.financialwhirlpool.cvs.Class.MapDirection;
import com.financialwhirlpool.cvs.Class.PolylineEncoding;
import com.financialwhirlpool.cvs.Class.Store;
import com.financialwhirlpool.cvs.MainActivity;
import com.financialwhirlpool.cvs.R;
import com.financialwhirlpool.cvs.VivzAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by an vo on 6/11/2016.
 */
public class MainFragment extends Fragment implements OnMapReadyCallback, android.location.LocationListener
        , GoogleApiClient.ConnectionCallbacks {

    GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Location cacheLocation;
    Marker currentMarker;
    LocationManager locationManager;
    String provider;
    String currentAddress;
    AlertDialog alertDialog;
    AdView mAdView;
    boolean adShown = false;

    public static List<Store> stores;
    Store newStore;
    ProgressDialog progress;
    FloatingActionButton locateStore;

    public MainFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.content_main, container, false);

        // setup floating button
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        FloatingActionButton gps = (FloatingActionButton) rootView.findViewById(R.id.gps);
        locateStore = (FloatingActionButton) rootView.findViewById(R.id.store);


        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adShown)
                        notShowAd();
                    EventBus.getDefault().post(new MessageEvent("Floating Button Clicked"));
                }
            });
        }

        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onConnected(savedInstanceState);
            }
        });

        locateStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastLocation != null)
                    checkDistanceOrigin();
                else {
                    Toast.makeText(getContext(), "No Location", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Support maps
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        // Suport Location
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        mAdView = (AdView) rootView.findViewById(R.id.adView);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.i("trido", "Onmapready");
        Circle circle;
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!adShown)
            showAd();
        Log.i("trido", "in OnConnected");
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                Log.i("trido", "in OnConnected get last");
                LatLng lastLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                if (currentMarker != null)
                    currentMarker.remove();
                currentMarker = mMap.addMarker(new MarkerOptions().position(lastLocation).title("I'm here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15));
                getAddress();
            } else {
                checkLocationService();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                Log.i("trido", "in OnConnected get last");
                LatLng lastLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                if (currentMarker != null)
                    currentMarker.remove();
                currentMarker = mMap.addMarker(new MarkerOptions().position(lastLocation).title("I'm here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15));
                getAddress();
            } else {
                locationManager.requestLocationUpdates(provider, 3000, 5, this);
            }
        }
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getContext(), "The app won't function normally", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        Double lat = location.getLatitude();
        Double lon = location.getLongitude();
        Log.i("trido", "onLocation changed called");
        mLastLocation = location;
        System.out.println("trido: Update mLastLocation Lat" + mLastLocation.getLatitude() + "  long " + mLastLocation.getLongitude());
        if (currentMarker != null) {
            currentMarker.remove();
        }
        LatLng currentLocation = new LatLng(lat, lon);
        if (mMap != null) {
            if (currentMarker != null)
                currentMarker.remove();
            currentMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("I'm here"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        }
        getAddress();
    }

    @Subscribe
    public void onEvent(VivzAdapter.PositionClickMessageEvent event) {
        Log.i("trido", "Recylcer view click on Event Position click" + event.message);
        mMap.clear();
        findShop(event.message);
    }

    // check distance between origins
    public void checkDistanceOrigin() {
        if (!adShown)
            showAd();
        if (cacheLocation != null) {
            double startLat = mLastLocation.getLatitude();
            double startLng = mLastLocation.getLongitude();
            double cacheLat = cacheLocation.getLatitude();
            double cacheLng = cacheLocation.getLongitude();
            String urlAPI = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins="
                    + startLat + "," + startLng + "&destinations="
                    + cacheLat + "," + cacheLng + "&key=" + R.string.key_API1;

            if (getOriginDistance(urlAPI)) {
                getDistance();
            } else {
                for (Store s : stores) {
                    s.setOptions(null);
                }
                Log.i("trido", "no new call from Distance Origin");
                findShop(0);
            }
        } else if (mLastLocation != null) {
            cacheLocation = mLastLocation;
            getDistance();
        } else {
            Toast.makeText(getContext(), "Can't find your location", Toast.LENGTH_SHORT).show();
        }
    }

    // handle get Distance
    public void getDistance() {
        DownloadTask downloadTask = new DownloadTask();
        double startLat = mLastLocation.getLatitude();
        double startLng = mLastLocation.getLongitude();

        String urlAPI = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" + startLat + "," + startLng + "&destinations=";
        String urlAPI1 = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=" + startLat + "," + startLng + "&destinations=";
        int lengthAPI1 = urlAPI1.length();
        stores = new ArrayList<Store>();
        stores = MainActivity.dataSource.findAll(currentAddress);
        if (stores != null) {
            for (int i = 0; i < stores.size(); i++) {
                Store newStore = stores.get(i);
                String lat = String.valueOf(newStore.getLat());
                String lng = String.valueOf(newStore.getLng());
                if (urlAPI.length() < 1800) {
                    urlAPI += lat + "," + lng;
                    if (i != stores.size() - 1) {
                        urlAPI += "|";
                    }
                } else {
                    urlAPI1 += lat + "," + lng;
                    if (i != stores.size() - 1) {
                        urlAPI1 += "|";
                    }
                }
            }

            urlAPI += R.string.key_API1;
            if (urlAPI1.length() > lengthAPI1) {
                int ind = urlAPI.lastIndexOf("|");
                urlAPI = new StringBuilder(urlAPI).replace(ind, ind + 1, "").toString();
                urlAPI1 += R.string.key_API1;
                downloadTask.execute(urlAPI, urlAPI1);
                System.out.println("trido- ul1: " + urlAPI1);
                System.out.println("trido-length " + urlAPI1.length());
            } else {
                downloadTask.execute(urlAPI);
            }
            System.out.println("trido- " + urlAPI);
            System.out.println("trido-length " + urlAPI.length());
            mMap.clear();
            // No stores returned from Find ALL
        } else {
            Toast.makeText(getContext(), "No shop nearby", Toast.LENGTH_LONG).show();
        }
    }

    public void findShop(int position) {

        newStore = stores.get(position);
        Log.i("trido", "findShop from list -- " + newStore.getDistance());
        LatLng closestStore = new LatLng(newStore.getLat(), newStore.getLng());

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(closestStore).title(newStore.getAddress())
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("circlek", 80, 80))))
                .showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(closestStore, 15));

        currentMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .title("I'm here"));
        // get from cache
        if (newStore.getOptions() != null) {
            mMap.addPolyline(newStore.getOptions());
            Log.i("trido", "add Polyline from cache");
            return;
        }

        // Direction
        DirectionTask directionTask = new DirectionTask();
        String urlDirection = "https://maps.googleapis.com/maps/api/directions/json?origin=" + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude() + "&destination="
                + newStore.getLat() + "," + newStore.getLng()
                + R.string.server_key_API;
        directionTask.execute(urlDirection);

        System.out.println("trido url-direction:" + urlDirection);
        EventBus.getDefault().post(new MessageEvent("Distance Data For List Ready"));
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }


    //*********** Task
    // ASYNC   *************///
    public boolean getOriginDistance(final String rawUrl) {
        final ArrayList<Integer> distanceOrigin = new ArrayList<>();

        Observable<Integer> distanceObservable = Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                try {
                    URL url = new URL(rawUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = urlConnection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in);

                    ClassGoogleMap values = new Gson().fromJson(reader, ClassGoogleMap.class);
                    if (values.getStatus().equals("OK")) {
                        List<ClassGoogleMap.Elements> combine = values.getRows().get(0).getElements();
                        for (ClassGoogleMap.Elements combine1 : combine) {
                            if (combine1.getStatus().equals("OK")) {
                                int dis = combine1.getDistance().getValue();
                                subscriber.onNext(dis);
                                subscriber.onCompleted();
                            }
                        }
                    }
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

        distanceObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .takeFirst(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return integer > 300;
                    }
                }).subscribe(new Observer<Integer>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Integer integer) {
                distanceOrigin.add(integer);
            }
        });

        if (distanceOrigin.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public class DownloadTask extends AsyncTask<String, Integer, List<String>> {
        String result1 = "";
        String result2 = "";
        List<String> result = new ArrayList<String>();
        URL url;
        HttpURLConnection urlConnection = null;
        List<Integer> distanceList = new ArrayList();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progress == null)
                progress = ProgressDialog.show(getContext(), "CVStore",
                        "Locating the closest shop", true);
        }

        @Override
        protected List<String> doInBackground(String... params) {
            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                ClassGoogleMap values = new Gson().fromJson(reader, ClassGoogleMap.class);
                if (values.getStatus().equals("OK")) {
                    List<ClassGoogleMap.Elements> combine = values.getRows().get(0).getElements();
                    for (ClassGoogleMap.Elements combine1 : combine) {
                        if (combine1.getStatus().equals("OK")) {
                            int dis = combine1.getDistance().getValue();
                            publishProgress(dis, combine.indexOf(combine1));
                        }
                    }
                } else {
                    Log.i("trido", "Err" + values.getStatus());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (params.length == 2) {
                try {
                    url = new URL(params[1]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = urlConnection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(in);

                    ClassGoogleMap values = new Gson().fromJson(reader, ClassGoogleMap.class);
                    List<ClassGoogleMap.Elements> combine = values.getRows().get(0).getElements();
                    for (ClassGoogleMap.Elements combine1 : combine) {
                        int dis = combine1.getDistance().getValue();
                        System.out.println("GSON trido=-" + dis);
                        if (combine1.getStatus().equals("OK")) {
                            publishProgress(dis, combine.indexOf(combine1));
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... dis) {
            super.onProgressUpdate(dis);
            stores.get(dis[1]).setDistance(dis[0]);
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            if (progress != null)
                progress.dismiss();
            Collections.sort(stores, new Comparator<Store>() {
                @Override
                public int compare(Store lhs, Store rhs) {
                    return lhs.getDistance() - rhs.getDistance();
                }
            });
            findShop(0);
        }
    }

    // Get Direction
    public class DirectionTask extends AsyncTask<String, String, String> {
        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progress == null) {
                progress = ProgressDialog.show(getContext(), "CVStore",
                        "Getting Direction", true);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                String encodedString = new Gson().fromJson(reader, MapDirection.class).getRoutes().get(0).getOverview_polyline().getPoints();
                publishProgress(encodedString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... encodedString) {
            super.onProgressUpdate(encodedString);

            PolylineEncoding decodeTool = new PolylineEncoding();
            List<LatLng> list = decodeTool.decode(encodedString[0]);
            PolylineOptions options = new PolylineOptions().width(15).color(Color.rgb(66, 133, 244)).geodesic(true);
            for (int z = 0; z < list.size(); z++) {
                LatLng point = list.get(z);
                options.add(point);
            }
            newStore.setOptions(options);
            Polyline line = mMap.addPolyline(options);

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (progress != null)
                progress.dismiss();
        }
    }


//Linh tinh

    public void getAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getSubAdminArea();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            currentAddress = "";
            currentAddress += address + " " + city + " " + state + " " + country;
            if (currentAddress.contains("District")) {
                currentAddress = currentAddress.replaceAll("District", "Quận");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void checkLocationService() {
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean network_enabled = false;
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!network_enabled) {
            // notify user
            if (alertDialog == null || !alertDialog.isShowing()) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                alertDialog = dialog.create();

                alertDialog.setMessage("Please turn on Location Service");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        getActivity().startActivity(myIntent);
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Toast.makeText(getContext(), "The app won't function normally", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
            }
        } else {
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    public void showAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setVisibility(View.VISIBLE);
        adShown = true;
        Log.i("trido", "showAd");
    }

    public void notShowAd() {
        mAdView.setVisibility(View.INVISIBLE);
        mAdView.destroy();
        adShown = false;
        Log.i("trido", "turn off Ad");
    }

    public void onStart() {
        mGoogleApiClient.connect();
        EventBus.getDefault().register(this);
        super.onStart();
    }

    public void onStop() {

        mGoogleApiClient.disconnect();
        locationManager.removeUpdates(this);
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onPause() {
        if (progress != null) {
            progress.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("trido", "onResume()");
        checkLocationService();
        locationManager.requestLocationUpdates(provider, 7000, 10, this);
    }


    public class MessageEvent {
        public final String message;

        public MessageEvent(String message) {
            this.message = message;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
