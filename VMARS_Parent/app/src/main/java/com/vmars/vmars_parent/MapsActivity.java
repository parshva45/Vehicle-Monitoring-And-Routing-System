package com.vmars.vmars_parent;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Geocoder;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    public double Latitude;
    public double Longitude;
    public static Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (!haveNetworkConnection()) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG).show();
        } else {
            ctx = getApplicationContext();
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            callAsynchronousTask();
            //Toast.makeText(getApplicationContext(),"BeforeTask", Toast.LENGTH_LONG).show();
/*
        BackgroundTask task1=new BackgroundTask();
        Toast.makeText(getApplicationContext(),"BeforeTask111", Toast.LENGTH_LONG).show();
        task1.parseJSON();
        Toast.makeText(getApplicationContext(),"afterTask", Toast.LENGTH_LONG).show();*/

        }
    }


    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.d("Doctor","executing");
                            BackgroundTask backgroundTask = new BackgroundTask();
                            backgroundTask.execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000); //execute in every 10 sec
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng remote_loc = new LatLng(Latitude, Longitude);
        //mMap.addMarker(new MarkerOptions().position(remote_loc).title("Remote Location"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(remote_loc));
        try {
            mMap.setMyLocationEnabled(true);
        }catch(SecurityException e){}
    }
}

class BackgroundTask extends AsyncTask<Void,Void,String> {
    Context ctx=MapsActivity.ctx;

    public double Latitude,Longitude;
    public String JSON_String="";

    public String jsonData="";
    @Override
    protected void onPreExecute()
    {

        //Toast.makeText(ctx,"Onpost", Toast.LENGTH_LONG).show();

    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            URL loc_url = new URL("http://vmars.pe.hu/get_latlong.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection) loc_url.openConnection();
            InputStream inputStream=httpURLConnection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder=new StringBuilder();
            while((JSON_String=bufferedReader.readLine())!=null)
            {
                stringBuilder.append(JSON_String);
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

            return stringBuilder.toString().trim();
        }catch (Exception e){}
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {

        //Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
        //Toast.makeText(ctx,"Onpost", Toast.LENGTH_LONG).show();
        jsonData=result;
        //Log.d("Doctor jsondata", jsonData);
        parseJSON();
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.d("loction address", "" + strReturnedAddress.toString());
            } else {
                Log.d("loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.d("loction address", "Canont get Address!");
        }
        return strAdd;
    }

    public void parseJSON(){
        // System.out.println("File Content: \n" + jsonData);
        //Toast.makeText(ctx,"insideparseJson", Toast.LENGTH_LONG).show();

        /*try {

            JSONObject obj = new JSONObject(jsonData);
            JSONArray array=obj.getJSONArray("server_response");

            Latitude= Double.valueOf(array.getJSONObject(0).getString("Latitude"));
            Longitude=Double.valueOf(array.getJSONObject(0).getString("Longitude"));
            //Toast.makeText(ctx,"Latitude"+Latitude+"Longitude"+Longitude, Toast.LENGTH_LONG).show();
            MapsActivity mpa=new MapsActivity();
            mpa.sendLatLong(Latitude, Longitude);*/
            String Latstr,Longstr;
            try{
                JSONObject jsonResult = new JSONObject(jsonData);
                //Log.d("Doctor jsonresult", jsonResult.toString());
                JSONArray data = jsonResult.getJSONArray("server_response");
                //Log.d("Doctor data", data.toString());
                if(data != null) {
                    JSONObject data1=data.getJSONObject(0);
                  //  Log.d("VMARS lat", data1.toString());

                    Latstr = data1.getString("Latitude");
                    Longstr=data1.getString("Longitude");
                    Latitude=Double.parseDouble(Latstr);
                    Longitude=Double.parseDouble(Longstr);

                    //Log.d("VMARS Latstr", Latstr);
                    //Log.d("VMARS Longstr", Longstr);
                    //Log.d("VMARS Latitude", String.valueOf(Latitude));
                    //Log.d("VMARS Longitude", String.valueOf(Longitude));
                    //Latitude=19.096602;
                    //Longitude=72.912356;
                    LatLng latlong=new LatLng(Latitude,Longitude);
                    MapsActivity.mMap.clear();
                    String remote_location=getCompleteAddressString(Latitude,Longitude);
                    Marker marker = MapsActivity.mMap.addMarker(new MarkerOptions().position(latlong).title("Remote Location")
                            .snippet(remote_location));

                    MapsActivity.mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                        @Override
                        public View getInfoWindow(Marker arg0) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {

                            LinearLayout info = new LinearLayout(ctx);
                            info.setOrientation(LinearLayout.VERTICAL);

                            TextView title = new TextView(ctx);
                            title.setTextColor(Color.BLACK);
                            title.setGravity(Gravity.CENTER);
                            title.setTypeface(null, Typeface.BOLD);
                            title.setText(marker.getTitle());

                            TextView snippet = new TextView(ctx);
                            snippet.setTextColor(Color.GRAY);
                            snippet.setText(marker.getSnippet());

                            info.addView(title);
                            info.addView(snippet);

                            return info;
                        }
                    });
                    marker.showInfoWindow();

                    MapsActivity.mMap.moveCamera(CameraUpdateFactory.newLatLng(latlong));
                    //Toast.makeText(ctx,"Updated...", Toast.LENGTH_LONG).show();
                    //Toast.makeText(ctx,"School bus is at:\n"+remote_location, Toast.LENGTH_LONG).show();


                }
                /*
                JSONObject json = (JSONObject) new JSONTokener(jsonData).nextValue();
                JSONObject json2 = json.getJSONObject("server_response");
                Latitude = json2.getDouble("Latitude");
                Longitude = json2.getDouble("Longitude");*/


        }catch(JSONException e){Log.d("Doctor", "Exception");}
    }



}
