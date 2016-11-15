package com.droid.demomultiselector;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.droid.mediamultiselector.activity.MediaSelectorActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvMediaPaths;
    private Button btnSelectMedia;

    private ArrayList<String> arrayMediaPath = new ArrayList<>();

    private final int REQUEST_CODE_MEDIA_SELECT = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvMediaPaths = (TextView) findViewById(R.id.activity_main_tv);
        btnSelectMedia = (Button) findViewById(R.id.activity_main_btn_select);

        btnSelectMedia.setOnClickListener(this);
    }

    private void showMediaPathSelected() {
        String paths = "";
        for (String path: arrayMediaPath) {
            paths = paths + "\n" + path;
        }
        tvMediaPaths.setText(paths);
    }

    @Override
    public void onClick(View view) {
        MediaSelectorActivity.startActivityForResult(this, REQUEST_CODE_MEDIA_SELECT,
                MediaSelectorActivity.SELECTION_MODE_MULTI, 4, MediaSelectorActivity.MEDIA_TYPE_ALL,
                true, true, arrayMediaPath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_MEDIA_SELECT) {

            arrayMediaPath = data.getStringArrayListExtra(MediaSelectorActivity.RESULTS_SELECTED_MEDIA);
            showMediaPathSelected();
        }
    }
}
