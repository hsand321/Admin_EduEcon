package com.example.admin_eduecon.video;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.admin_eduecon.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterVideo extends RecyclerView.Adapter<AdapterVideo.HolderVideo> {


    private Context context;
    private ArrayList<ModelVideo> videoArrayList;

    public AdapterVideo(Context context, ArrayList<ModelVideo> videoArrayList) {
        this.context = context;
        this.videoArrayList = videoArrayList;
    }

    @Override
    public HolderVideo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_video, parent, false);
        return new HolderVideo(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterVideo.HolderVideo holder, int position) {
        ModelVideo modelVideo = videoArrayList.get(position);

        String id = modelVideo.getId();
        String title = modelVideo.getTitle();
        String timestamp = modelVideo.getTimestamp();
        String videoUrl = modelVideo.getVideoUrl();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String formattedDateTime = DateFormat.format("dd/MM/yy K:mm a", calendar).toString();

        holder.tvTitle.setText(title);
        holder.tvTime.setText(formattedDateTime);
        setVideoUrl(modelVideo, holder);

        holder.downloadFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadVideo(modelVideo);
            }
        });

        holder.deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure about this: " + title)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteVideo(modelVideo);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });
    }

    private void setVideoUrl(ModelVideo modelVideo, HolderVideo holder) {
        holder.progressBar.setVisibility(View.VISIBLE);

        String videoUrl = modelVideo.getVideoUrl();
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(holder.videoView);

        Uri videoUri = Uri.parse(videoUrl);
        holder.videoView.setMediaController(mediaController);
        holder.videoView.setVideoURI(videoUri);

        holder.videoView.requestFocus();
        holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
//        holder.videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//            @Override
//            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
//                mediaPlayer.start();
//                return false;
//            }
//        });
        holder.videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
                        holder.progressBar.setVisibility(View.VISIBLE);
                        return true;
                    }
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                        holder.progressBar.setVisibility(View.VISIBLE);
                        return true;
                    }
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                        holder.progressBar.setVisibility(View.GONE);
                        return true;
                    }
                }
                return false;
            }
        });
        holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });


    }

    private void deleteVideo(ModelVideo modelVideo) {
        String videoId = modelVideo.getId();
        String videoUrl = modelVideo.getVideoUrl();

        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Videos");
                        databaseReference.child(videoId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Videos deleted successfully...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void downloadVideo(ModelVideo modelVideo) {

        String videoUrl = modelVideo.getVideoUrl();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl);
        storageReference.getMetadata()
                .addOnSuccessListener(storageMetadata -> {
                    String fileName = storageMetadata.getName();
                    String fileType = storageMetadata.getContentType();
                    String fileDirectory = Environment.DIRECTORY_DOWNLOADS;

                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse(videoUrl);

                    DownloadManager.Request request = new DownloadManager.Request(uri);

                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir("" + fileDirectory, "" + fileName + ".mp4");

                    downloadManager.enqueue(request);
                })
                .addOnFailureListener(e -> Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return videoArrayList.size();
    }

    class HolderVideo extends RecyclerView.ViewHolder {

        VideoView videoView;
        TextView tvTitle, tvTime;
        ProgressBar progressBar;
        FloatingActionButton downloadFab, deleteFab;

        public HolderVideo(@NonNull View itemView) {
            super(itemView);

            videoView = itemView.findViewById(R.id.viewVideo);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            progressBar = itemView.findViewById(R.id.progressBar);
            downloadFab = itemView.findViewById(R.id.downloadFab);
            deleteFab = itemView.findViewById(R.id.deleteFab);

        }
    }
}
