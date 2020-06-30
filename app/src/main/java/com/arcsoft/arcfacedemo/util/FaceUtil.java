package com.arcsoft.arcfacedemo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;

import java.util.ArrayList;
import java.util.List;

public class FaceUtil {

    private static final String TAG = "FaceUtil";

    public static FaceFeature extractFaceFeatureFromBitmap(Context context, Bitmap imageBitmap) {

        //ArcFace SDK code
        imageBitmap = ArcSoftImageUtil.getAlignedBitmap(imageBitmap, true);

        byte[] bgr24 = ArcSoftImageUtil.createImageData(imageBitmap.getWidth(), imageBitmap.getHeight(), ArcSoftImageFormat.NV21);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(imageBitmap, bgr24, ArcSoftImageFormat.NV21);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            Toast.makeText(context, "failed to transform bitmap to imageData, code is " + transformCode, Toast.LENGTH_LONG).show();
            return null;
        }

        FaceEngine faceEngine = getFaceEngine(context);

        FaceFeature faceFeature = new FaceFeature();
        List<FaceInfo> faceInfoList = new ArrayList<>();
        int codeDetectFaces = faceEngine.detectFaces(bgr24, imageBitmap.getWidth(), imageBitmap.getHeight(), FaceEngine.CP_PAF_NV21, faceInfoList);
        if (codeDetectFaces == ErrorInfo.MOK && faceInfoList.size() > 0) {
            Log.i("TAG", "detectFaces, face num is : "+ faceInfoList.size());


            int extractCode = faceEngine.extractFaceFeature(bgr24, imageBitmap.getWidth(), imageBitmap.getHeight(), FaceEngine.CP_PAF_NV21, faceInfoList.get(0), faceFeature);
            if (extractCode == ErrorInfo.MOK){
                Log.i("TAG", "extract face feature success");
                Log.i("getFeatureData()", faceFeature.getFeatureData().toString());
            }else{
                Log.i("TAG", "extract face feature failed, code is : " + extractCode);
            }

        } else {
            Log.i("TAG", "no face detected, code is : " + codeDetectFaces);
        }
        return faceFeature;
    }

    private static FaceEngine getFaceEngine(Context context) {
        int scale = 32;
        int maxFaceNum = 5;

        int initMask = FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_LIVENESS | FaceEngine.ASF_AGE |
                FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE;
        FaceEngine faceEngine = new FaceEngine();
        int code = faceEngine.init(context, DetectMode.ASF_DETECT_MODE_VIDEO, DetectFaceOrientPriority.ASF_OP_0_ONLY, scale, maxFaceNum, initMask);
        if (code != ErrorInfo.MOK)
            Log.w(TAG, "init failed, code is : " + code);
        else
            Log.i(TAG, "init success");
        return faceEngine;
    }

    public static float compareFacialFeatures(Context context, FaceFeature feature1, FaceFeature feature2) {

        FaceSimilar faceSimilar = new FaceSimilar();
        int compareCode = getFaceEngine(context).compareFaceFeature(feature1, feature2, faceSimilar);
        if (compareCode == ErrorInfo.MOK){
            //Similarity of two faces
            Log.i("score", "[" + faceSimilar.getScore() + "]");
            return faceSimilar.getScore();
        }else{
            Log.i(TAG, "compare failed, code is : " + compareCode);
        }
        return 0;
    }
}
