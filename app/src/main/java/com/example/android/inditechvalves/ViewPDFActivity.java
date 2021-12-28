package com.example.android.inditechvalves;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ViewPDFActivity extends AppCompatActivity {
    private String pdfUrl;

    private ProgressDialog progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdf);

        pdfUrl=getIntent().getStringExtra("pdfUrl");
        progressBar=new ProgressDialog(this);
        progressBar.setMessage("Loading...");
        progressBar.show();
        new RetrivedPdffromFirebase().execute(pdfUrl);

    }
    class RetrivedPdffromFirebase extends AsyncTask<String, Void, InputStream> {
        // we are calling async task and performing
        // this task to load pdf in background.
        @Override
        protected InputStream doInBackground(String... strings) {
            // below line is for declaring
            // our input stream.
            InputStream pdfStream = null;
            try {
                // creating a new URL and passing
                // our string in it.
                URL url = new URL(strings[0]);

                // creating a new http url connection and calling open
                // connection method to open http url connection.
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                if (httpURLConnection.getResponseCode() == 200) {
                    // if the connection is successful then
                    // we are getting response code as 200.
                    // after the connection is successful
                    // we are passing our pdf file from url
                    // in our pdfstream.
                    pdfStream = new BufferedInputStream(httpURLConnection.getInputStream());
                }

            } catch (IOException e) {
                // this method is
                // called to handle errors.
                return null;
            }
            // returning our stream
            // of PDF file.
            return pdfStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            // after loading stream we are setting
            // the pdf in your pdf view.
            progressBar.dismiss();

        }
    }
}