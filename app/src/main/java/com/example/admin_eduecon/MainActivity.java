package com.example.admin_eduecon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.admin_eduecon.note.DeleteNoteActivity;
import com.example.admin_eduecon.note.UploadNoteActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    CardView uploadNote,addEbook,delNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadNote = findViewById(R.id.addNote);
        addEbook = findViewById(R.id.addEbook);
        delNote = findViewById(R.id.delNote);

        uploadNote.setOnClickListener(this);
        addEbook.setOnClickListener(this);
        delNote.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.addNote:
                intent = new Intent(MainActivity.this, UploadNoteActivity.class);
                startActivity(intent);
                break;
            case R.id.addEbook:
                intent = new Intent(MainActivity.this,UploadPdfActivity.class);
                startActivity(intent);
                break;
            case R.id.delNote:
                intent = new Intent(MainActivity.this, DeleteNoteActivity.class);
                startActivity(intent);
                break;
        }

    }
}