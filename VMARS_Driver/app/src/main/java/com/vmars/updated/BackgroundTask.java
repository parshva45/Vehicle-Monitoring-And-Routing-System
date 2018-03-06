package com.vmars.updated;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by mit on 2/3/16.
 */
public class BackgroundTask extends AsyncTask<Double,Void,String> {
    Context ctx;
    BackgroundTask(Context ctx)
    {
        this.ctx=ctx;
    }

    @Override
    protected void onPreExecute()
    {

        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Double... params) {
        String loc_url = "http://vmars.pe.hu/latlong.php";
        Double latitude = params[0];
        Double longitude = params[1];
        try {
            Log.d("Mit", "Hiii123");
            URL url = new URL(loc_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            OutputStream OS = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(OS, "UTF-8"));
            String data = URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode("" + latitude, "UTF-8") + "&" +
                    URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode("" + longitude, "UTF-8");

            if (latitude != 0) {
                bufferedWriter.write(data);
                bufferedWriter.flush();

                bufferedWriter.close();
                OS.close();
                InputStream IS = httpURLConnection.getInputStream();
                IS.close();
                if(latitude==0) {
                    Toast.makeText(ctx,"GPS not in range", Toast.LENGTH_LONG).show();
                    return "GPS not in range";
                }

                return "Stored to server...";
            }

            }catch(MalformedURLException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {

        //Toast.makeText(ctx,result, Toast.LENGTH_LONG).show();

        //Toast.makeText(ctx,"Onpost", Toast.LENGTH_LONG).show();
    }
}
