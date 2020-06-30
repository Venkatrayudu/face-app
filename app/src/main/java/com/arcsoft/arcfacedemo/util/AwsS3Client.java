package com.arcsoft.arcfacedemo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Year;
import java.util.Date;

public class AwsS3Client extends AsyncTask {

    public static String storeImageInS3 (Bitmap bitmapImage, String id) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(bitmapData);

        getS3Client().putObject("face-app-pictures", id + ".jpg", bis, new ObjectMetadata());

        return getImageURLFromS3(id);
    }

    public static String getImageURLFromS3(String id) {
        ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
        overrides.setContentType("image/jpeg");

        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest("face-app-pictures", id + ".jpg");
//        urlRequest.setExpiration(new Date(System.currentTimeMillis() + 3600000));
//        urlRequest.setExpiration(new Date(2020, 12, 25));
        urlRequest.setExpiration(new Date(Long.parseLong("1609409532000")));
        urlRequest.setResponseHeaders(overrides);

        try {
            URL url = getS3Client().generatePresignedUrl(urlRequest);
            Log.i("url.toURI().toString()", url.toURI().toString());
            return url.toURI().toString();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
        }
        return "";
    }

    private static AmazonS3 getS3Client() {
        AmazonS3Client s3Client = new AmazonS3Client(new BasicAWSCredentials("AKIAJ6ERHNULQGZK3SOA", "AHDgDJZzXADYb9IQx3B8oKWzjr6kXv7YElVxt13K"));
        s3Client.createBucket("face-app-pictures");
        return s3Client;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.i("Method called: ", (String) objects[0]);
        switch ((String) objects[0]) {
            case "storeImageInS3":
                return storeImageInS3((Bitmap) objects[1], (String) objects[2]);

            case "getImageURLFromS3":
                return getImageURLFromS3((String) objects[1]);

            case "getBitmapFromURL":
                return getBitmapFromURL((String) objects[1]);

        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
