package com.example.assignmentflamrnd;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.example.assignmentflamrnd.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("assignmentflamrnd");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        tv.setText(stringFromJNI());

        // Call the OpenCV version logger
        logOpenCVVersion();  // <-- This will print the version to Logcat
    }

    public native String stringFromJNI();

    // New native method to log OpenCV version
    public native void logOpenCVVersion();
}
