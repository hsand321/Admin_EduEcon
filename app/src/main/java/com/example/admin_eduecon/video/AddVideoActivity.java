package com.example.admin_eduecon.video;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.admin_eduecon.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddVideoActivity extends AppCompatActivity {

    private ActionBar actionBar;

    private EditText etTitle;
    private VideoView videoView;
    private Button uploadVideoBtn;
    private FloatingActionButton pickVideofab;
    MediaController mediaController;

    private static final int VIDEO_PICK_GALLERY_CODE = 100;

    private Uri videoUri = null;
    private String title;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);

        actionBar = getSupportActionBar();

        actionBar.setTitle("Add New Video");

        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        etTitle = findViewById(R.id.etTitle);
        videoView = findViewById(R.id.videoView);
        uploadVideoBtn = findViewById(R.id.uploadVideoBtn);
        pickVideofab = findViewById(R.id.pickVideoBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Uploading video");
        progressDialog.setCanceledOnTouchOutside(false);

        uploadVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = etTitle.getText().toString().trim();
                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(AddVideoActivity.this, "Title is Required...", Toast.LENGTH_SHORT).show();
                } else if (videoUri == null) {
                    Toast.makeText(AddVideoActivity.this, "Pick a video before you can Upload...", Toast.LENGTH_SHORT).show();
                } else {
                    uploadVideoFirebase();
                }
            }
        });

        pickVideofab.setOnClickListener(v -> videoPickGallery());
        mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        videoView.start();

    }

    private void uploadVideoFirebase() {
        progressDialog.show();

        String timestamp = "" + System.currentTimeMillis();

        String filePathAndName = "Videos/" + "video_" + timestamp;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);

        storageReference.putFile(videoUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();
                        if (uriTask.isSuccessful()) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", "" + timestamp);
                            hashMap.put("title", "" + title);
                            hashMap.put("timestamp", "" + timestamp);
                            hashMap.put("videoUrl", "" + downloadUri);

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Videos");
                            reference.child(timestamp)
                                    .setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AddVideoActivity.this, "Video Uploaded...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(AddVideoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddVideoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void videoPickGallery() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), VIDEO_PICK_GALLERY_CODE);
    }

    private void setVideoToVideoView() {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.pause();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == VIDEO_PICK_GALLERY_CODE) {
                if (data != null) {
                    videoUri = data.getData();
                }
                setVideoToVideoView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}