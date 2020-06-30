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
import android.widget.Switch;
import android.widget.Toast;
import com.arcsoft.arcfacedemo.util.AwsS3Client;
import com.arcsoft.arcfacedemo.util.FaceUtil;
import com.arcsoft.arcfacedemo.util.HttpUtils;
import com.arcsoft.face.FaceFeature;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.ExecutionException;
import cz.msebera.android.httpclient.Header;

public class FacialSigninActivity extends AppCompatActivity {

    private EditText userNameEditText;
    private EditText pwdEditText;
    private ImageView imageView;
    private Bitmap signinImageBitmap;

    private Switch switch1;
    private Switch switch2;
    private Switch switch3;
    private Switch switch4;

    private final String TAG = "FacialSigninActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facial_signin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userNameEditText = findViewById(R.id.userName);
        pwdEditText = findViewById(R.id.password);
        imageView = findViewById(R.id.imageViewSignin);

        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        switch3 = findViewById(R.id.switch3);
        switch4 = findViewById(R.id.switch4);
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
            signinImageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(signinImageBitmap);
        }
    }

    public int onClickSigninButton(View view) {

        final String userName = userNameEditText.getText().toString();
        final String pwd = pwdEditText.getText().toString();
        Log.i("userName", userName);
        Log.i("password", pwd);
        if (userName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "User name is empty", Toast.LENGTH_SHORT).show();
            return 0;
        }
        if (pwd.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Password is empty", Toast.LENGTH_SHORT).show();
            return 0;
        }

        Log.i("switch1", "" + switch1.isChecked());
        Log.i("switch2", "" + switch2.isChecked());
        Log.i("switch3", "" + switch3.isChecked());
        Log.i("switch4", "" + switch4.isChecked());

        if (switch1.isChecked() || switch2.isChecked() || switch3.isChecked() || switch4.isChecked()) {
            Toast.makeText(getApplicationContext(), "You are not allowed to access the system based on your answers to screening questions.",
                    Toast.LENGTH_SHORT).show();
            return 0;
        }

        HttpUtils.post("signin", userName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                super.onSuccess(statusCode, headers, jsonArray);
                Log.i("statusCode", String.valueOf(statusCode));
                Log.i("headers", headers.toString());
                Log.d("jsonArray", "---------------- this is response : " + jsonArray);
                Log.i("-jsonArray.length()", jsonArray.length()+ "]");
                Log.i("-jsonArray.isNull()", jsonArray.isNull(0) + "]");

                if (jsonArray.length() == 1 && !jsonArray.isNull(0)) {
                    try {
                        JSONObject empJson = new JSONObject(jsonArray.get(0).toString());
                        String password = empJson.getString("password");
                        String imageURL = empJson.getString("imageURL");
                        String firstName = empJson.getString("firstName");
                        String lastName = empJson.getString("lastName");
                        Log.i("getPassword", password);
                        Log.i("imageURL from S3", imageURL);
                        if (pwd.equals(password)) {
                            //extract face feature of image captured during SIGNUP
                            Bitmap bitmapSignup = (Bitmap) new AwsS3Client().execute("getBitmapFromURL", imageURL).get();
                            Log.i("bitmap", "bitmap read successfully!");
                            FaceFeature faceFeatureSignup = FaceUtil.extractFaceFeatureFromBitmap(getApplicationContext(), bitmapSignup);
                            Log.i("SIGNUP face feature", faceFeatureSignup.getFeatureData().toString());

                            //extract face feature of image captured during SIGNIN
                            FaceFeature faceFeatureSignin = FaceUtil.extractFaceFeatureFromBitmap(getApplicationContext(), signinImageBitmap);
                            Log.i("SIGNIN face feature", faceFeatureSignup.getFeatureData().toString());

                            float score = FaceUtil.compareFacialFeatures(getApplicationContext(), faceFeatureSignin, faceFeatureSignup);
                            if (score >= 0.8) {
                                Toast.makeText(getApplicationContext(), "Login success!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                                intent.putExtra("id", userName);
                                intent.putExtra("imageURL", imageURL);
                                intent.putExtra("firstName", firstName);
                                intent.putExtra("lastName", lastName);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Login failed due to the less matching score!", Toast.LENGTH_SHORT).show();
                                Log.i("TAG", "Login failed due to the less matching score!");
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid password.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException | InterruptedException | ExecutionException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Log.i(TAG, "Username does not exist.");
                    Toast.makeText(getApplicationContext(), "Username does not exist.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return 0;
    }
}