package com.two.channelmyanmar.activity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.two.channelmyanmar.R;

/*
 * Created by Toewaioo on 4/1/25
 * Description: [Add class description here]
 */
public class Test extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();

    public Test() {
        // constructor code
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        showDialog();

    }
    public void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.play_download_dialog,null);

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        if(alertDialog.getWindow()!=null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        }
        alertDialog.show();

    }
}
