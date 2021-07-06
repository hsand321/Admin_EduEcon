package com.example.admin_eduecon.note;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.admin_eduecon.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeleteNoteActivity extends AppCompatActivity {

    private RecyclerView delNoteRecycler;
    private ProgressBar progressBar;
    private ArrayList<NoteData> list;
    private NoteAdapter adapter;

    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_note);

        delNoteRecycler = findViewById(R.id.delNoteRecycler);
        progressBar = findViewById(R.id.progressBar);

        reference = FirebaseDatabase.getInstance().getReference().child("Note");

        delNoteRecycler.setLayoutManager(new LinearLayoutManager(this));
        delNoteRecycler.setHasFixedSize(true);
        
        getNote();
    }

    private void getNote() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    NoteData data = snapshot.getValue(NoteData.class);
                    list.add(data);
                }

                adapter = new NoteAdapter(DeleteNoteActivity.this, list);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                delNoteRecycler.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);

                Toast.makeText(DeleteNoteActivity.this,databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


}