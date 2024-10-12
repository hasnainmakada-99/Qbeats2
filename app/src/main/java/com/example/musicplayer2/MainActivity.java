package com.example.musicplayer2;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listview);

        // Request external storage permission
        Dexter.withContext(MainActivity.this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        ArrayList<File> mysongs = fetchSongs(Environment.getExternalStorageDirectory());

                        if (!mysongs.isEmpty()) {
                            String[] items = new String[mysongs.size()];
                            final ArrayList<String> songPaths = new ArrayList<>();  // Store the paths of the songs

                            for (int i = 0; i < mysongs.size(); i++) {
                                items[i] = mysongs.get(i).getName().replace(".mp3", "").replace(".mpeg", "");
                                songPaths.add(mysongs.get(i).getAbsolutePath());  // Store the path of the song
                            }

                            ArrayAdapter<String> ad = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, items);
                            listView.setAdapter(ad);

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    Intent intent = new Intent(MainActivity.this, activity_playsong.class);
                                    intent.putExtra("songPaths", songPaths);
                                    intent.putExtra("currentSong", songPaths.get(i));  // Send the selected song's path
                                    intent.putExtra("position", i);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "No songs found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getApplicationContext(), "Permission Denied!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    // Method to fetch songs from external storage
    public ArrayList<File> fetchSongs(File file) {
        ArrayList<File> songsList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File currentFile : files) {
                if (!currentFile.isHidden() && currentFile.isDirectory()) {
                    songsList.addAll(fetchSongs(currentFile));
                } else if (currentFile.getName().endsWith(".mp3") || currentFile.getName().endsWith(".mpeg")) {
                    songsList.add(currentFile);
                }
            }
        }
        return songsList;
    }
}
