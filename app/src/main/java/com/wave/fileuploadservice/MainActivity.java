package com.wave.fileuploadservice;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.erikagtierrez.multiple_media_picker.Gallery;
import com.karumi.dexter.BuildConfig;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.vlk.multimager.activities.GalleryActivity;
import com.vlk.multimager.utils.Constants;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;
import com.wave.fileuploadservice.adapter.HorizontalRecyclerView;
import com.wave.fileuploadservice.model.ProfileImage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final int REQUEST_GALLERY_PHOTO = 102;
    private static final String TAG = "MainActivity";
    private RecyclerView rv_images;
    Button btnSelectImages;


    ArrayList<Uri> uri = new ArrayList<>();
    ArrayList<ProfileImage> arrayListImages = new ArrayList<>();
    HorizontalRecyclerView adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectImages = findViewById(R.id.btnSelectImages);
        rv_images = findViewById(R.id.rv_images);

        btnSelectImages.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {/*
            case R.id.buttonUpload:
                if (tvSelectedFilePath.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Select file first", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent mIntent = new Intent(this, FileUploadService.class);
                mIntent.putExtra("mFilePath", tvSelectedFilePath.getText().toString());
                FileUploadService.enqueueWork(this, mIntent);
                break;*/
            case R.id.btnSelectImages:
                requestCameraPermission();
                break;
        }
    }


    /**
     * Alert dialog for capture or select from galley
     */
    private void selectImage() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY_PHOTO);
    }

    private void requestCameraPermission() {
        Dexter.withContext(MainActivity.this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            selectImage();
                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            //showPermissionsAlert();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY_PHOTO) {
                Log.d(TAG, String.valueOf(data.getClipData()));
                if (data.getClipData() != null) {
                    //pick up multiple images
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        uri.add(data.getClipData().getItemAt(i).getUri());

                        ProfileImage profileImage = new ProfileImage();
                        profileImage.setImage_path(FileUtils.getPath(MainActivity.this,
                                data.getClipData().getItemAt(i).getUri()));

                        profileImage.setIs_profile_image("0");
                        arrayListImages.add(0, profileImage);
                    }

                } else {
                    //picked single image
                    Uri uriImage = data.getData();
                    uri.add(uriImage);
                    ProfileImage profileImage = new ProfileImage();
                    profileImage.setImage_path(FileUtils.getPath(MainActivity.this,
                            uriImage));
                    arrayListImages.add(0, profileImage);
                }

                Intent mIntent = new Intent(this, FileUploadService.class);
                mIntent.putExtra("mFilePath", arrayListImages);
                FileUploadService.enqueueWork(this, mIntent);


                adapter = new HorizontalRecyclerView(uri);
                rv_images.setLayoutManager(new GridLayoutManager(this, 3));
                rv_images.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
    }


}
