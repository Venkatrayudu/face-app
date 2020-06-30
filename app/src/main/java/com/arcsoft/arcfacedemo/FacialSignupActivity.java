package com.arcsoft.arcfacedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.arcsoft.arcfacedemo.util.AwsS3Client;
import com.arcsoft.arcfacedemo.util.HttpUtils;
import com.arcsoft.arcfacedemo.util.face.Helper;
import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class FacialSignupActivity extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText idNumberEditText;
    private EditText passwordEditText;
    private ImageView imageView;

    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facial_signup);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firstNameEditText = findViewById(R.id.firstName);
        lastNameEditText = findViewById(R.id.lastName);
        idNumberEditText = findViewById(R.id.idNumber);
        passwordEditText = findViewById(R.id.password);
        imageView = findViewById(R.id.imageViewSignup);

    }

    public void onClickSigninButton(View view) {
        Intent signinIntent = new Intent(this, FacialSigninActivity.class);
        startActivity(signinIntent);
    }

    public void onClickSignupButton(View view) {
        final String firstName = firstNameEditText.getText().toString();
        final String lastName = lastNameEditText.getText().toString();
        final String idNumber = idNumberEditText.getText().toString();
        final String pwd = passwordEditText.getText().toString();

        Log.i("First Name: ", firstName);
        Log.i("Last Name: ", lastName);
        Log.i("ID Number: ", idNumber);
        Log.i("Password", pwd);

        //Calling REST service
        HttpUtils.post("signin", idNumber, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                super.onSuccess(statusCode, headers, jsonArray);
                Log.i("statusCode", String.valueOf(statusCode));
                Log.d("jsonArray", "---------------- this is response : " + jsonArray);
                Log.i("-jsonArray.length()", jsonArray.length()+ "]");

                if (jsonArray.length() > 0 && !jsonArray.isNull(0)) {
                    Log.i("", "User already exists in DynamoDB table");
                    Toast.makeText(getApplicationContext(), "User already exists in DynamoDB table!", Toast.LENGTH_LONG).show();
                } else {
                    Log.i("", "calling signup service.");
                    String imageURLFromS3 = null;
                    try {
                        imageURLFromS3 = (String) new AwsS3Client().execute("storeImageInS3", imageBitmap, idNumber).get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i("+imageURLFromS3", imageURLFromS3);
                    StringEntity entity = null;
                    try {
                        JSONObject jsonParams = new JSONObject();
                        jsonParams.put("id", idNumber);
                        jsonParams.put("firstName", firstName);
                        jsonParams.put("lastName", lastName);
                        jsonParams.put("imageURL", imageURLFromS3);
                        jsonParams.put("password", pwd);
                        entity = new StringEntity(jsonParams.toString());
                    } catch (JSONException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    HttpUtils.post(getApplicationContext(), "signup", entity, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            Log.i("response", response.toString());
                            Log.i("Signup success:", "statusCode - " + statusCode);
                            Toast.makeText(getApplicationContext(), "Signup successful!", Toast.LENGTH_LONG).show();

                            Intent signinIntent = new Intent(getApplicationContext(), FacialSigninActivity.class);
                            startActivity(signinIntent);
                        }
                    });
                }
            }
        });


    }

    static final int REQUEST_TAKE_PHOTO = 1;

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }
}