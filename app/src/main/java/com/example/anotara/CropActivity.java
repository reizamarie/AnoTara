package com.example.anotara;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImageView;

public class CropActivity extends AppCompatActivity {

    CropImageView cropImageView;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        cropImageView = findViewById(R.id.cropImageView);

        imageUri = getIntent().getData();

        if (imageUri != null) {
            cropImageView.setImageUriAsync(imageUri);
        }

        findViewById(R.id.cropBtn).setOnClickListener(v -> {

            android.graphics.Bitmap croppedBitmap = cropImageView.getCroppedImage();

            if (croppedBitmap != null) {
                String savedPath = saveBitmapToInternalStorage(croppedBitmap);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("croppedImagePath", savedPath);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        findViewById(R.id.cancelBtn).setOnClickListener(v -> finish());
    }

    private String saveBitmapToInternalStorage(android.graphics.Bitmap bitmap) {
        try {
            java.io.File file = new java.io.File(
                    getFilesDir(),
                    "profile_image_" + System.currentTimeMillis() + ".jpg"
            );

            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);

            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream);

            outputStream.flush();
            outputStream.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}