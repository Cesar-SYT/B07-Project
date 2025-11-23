package com.example.smartair;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import com.example.smartair.model.Child;
import com.example.smartair.model.Parent;
import com.example.smartair.model.Provider;
import com.example.smartair.r3.R3Service;
import com.example.smartair.r3.R3ServiceImpl;
import com.example.smartair.r3.BreathStatus;
import com.example.smartair.r3.B;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FB_TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Child testChild = new Child("child001");
        R3Service r3 = new R3ServiceImpl();
        r3.logRescueDose(
                testChild,
                2,                          // doseCount
                BreathStatus.Feeling.NORMAL,        // pre
                BreathStatus.Feeling.NORMAL         // post
        );


        // ⭐ 用你现在唯一正常的布局：activity_test.xml
        setContentView(R.layout.activity_test);

        // ⭐ Firebase 测试代码（按你的地址来）
        FirebaseDatabase db = FirebaseDatabase.getInstance(
                "https://smart-air-61888-default-rtdb.firebaseio.com/"
        );
        DatabaseReference ref = db.getReference("test_connection");

        ref.setValue("Hello Firebase from Java!")
                .addOnSuccessListener(unused ->
                        Log.d(TAG, "写入成功"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "写入失败: " + e.getMessage(), e));
    }
}
