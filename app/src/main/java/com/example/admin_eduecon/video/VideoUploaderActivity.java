package com.example.admin_eduecon.video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.admin_eduecon.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class VideoUploaderActivity extends AppCompatActivity {

    //UI views
    FloatingActionButton addVideoBtn;
    private RecyclerView rvVideo;
    private ArrayList<ModelVideo> videoArrayList;
    private AdapterVideo adapterVideo;
    private static int REQUEST_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_CODE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_uploader);

        setTitle("Videos");

        addVideoBtn = findViewById(R.id.addVideoBtn);
        rvVideo = findViewById(R.id.rvVideo);


        loadVideoFromFirebase();

        addVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VideoUploaderActivity.this, AddVideoActivity.class));
            }
        });
    }

    private void loadVideoFromFirebase() {
        videoArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Videos");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                videoArrayList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    ModelVideo modelVideo = dataSnapshot.getValue(ModelVideo.class);
                    videoArrayList.add(modelVideo);
                }
                adapterVideo = new AdapterVideo(VideoUploaderActivity.this, videoArrayList);
                rvVideo.setAdapter(adapterVideo);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}