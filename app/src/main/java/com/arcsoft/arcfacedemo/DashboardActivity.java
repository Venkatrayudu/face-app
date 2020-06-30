package com.arcsoft.arcfacedemo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.util.AwsS3Client;
import com.arcsoft.arcfacedemo.util.face.Helper;

import java.util.concurrent.ExecutionException;

public class DashboardActivity extends AppCompatActivity {

    private Bitmap savedImageBitmap;
    private ImageView imageViewDb;
    private TextView textViewWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageViewDb = findViewById(R.id.imageViewDb);
        textViewWelcome = findViewById(R.id.textView3);

        String id = getIntent().getExtras().getString("id");
        String firstName = getIntent().getExtras().getString("firstName");
        String lastName = getIntent().getExtras().getString("lastName");
        textViewWelcome.setText("Welcome " + firstName + " " + lastName + "!");
//        String imageURL = AwsS3Client.getImageURLFromS3(id);
        String imageURL = null;
        try {
            imageURL = (String) new AwsS3Client().execute("getImageURLFromS3", id).get();

//        String imageURL = getIntent().getExtras().getString("imageURL");
            savedImageBitmap = (Bitmap) new AwsS3Client().execute("getBitmapFromURL", imageURL).get();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        imageViewDb.setImageBitmap(savedImageBitmap);
    }
}