package com.example.sdp_i;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

/**
 * @noinspection deprecation
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView capturedImage;
    private String currentPhotoPath;
    private Bitmap currentBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions if not granted
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 100);
        }

        // Initialize UI elements
        capturedImage = findViewById(R.id.capturedImage);
        Button captureButton = findViewById(R.id.captureButton);
        Button processButton = findViewById(R.id.processButton);
        Button uploadButton = findViewById(R.id.uploadButton);

        captureButton.setOnClickListener(v -> {
            try {
                dispatchTakePictureIntent();
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show();
            }
        });

        processButton.setOnClickListener(v -> {
            if (currentBitmap != null) {
                Bitmap processedBitmap = processImage(currentBitmap);
                capturedImage.setImageBitmap(processedBitmap);
            } else {
                Toast.makeText(this, "No image to process", Toast.LENGTH_SHORT).show();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        uploadButton.setOnClickListener(v -> {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account == null) {
                startActivityForResult(googleSignInClient.getSignInIntent(), 2);  // Request sign-in
            } else {
                uploadFileToDrive();
            }
        });


    }

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the file where the photo will be saved
            File photoFile = createImageFile();
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.sdp_i.file provider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            //noinspection deprecation
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name with a timestamp
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file path for use with intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Display the high-quality image in the ImageView
            setPic();
        }

        if (requestCode == 2) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            task.addOnSuccessListener(account -> uploadFileToDrive());
        }

    }

    private void uploadFileToDrive() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                // Initialize GoogleAccountCredential
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        this,
                        Collections.singletonList(DriveScopes.DRIVE_FILE)
                );
                credential.setSelectedAccount(account.getAccount());

                // Initialize the Drive service
                Drive driveService = new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        JacksonFactory.getDefaultInstance(),
                        credential
                ).setApplicationName("SDP-I").build();

                // Retrieve current counter for naming files
                SharedPreferences preferences = getSharedPreferences("uploadPrefs", MODE_PRIVATE);
                int imageCounter = preferences.getInt("imageCounter", 1); // Default to 1 if not set
                String fileName = "image_" + imageCounter + ".jpg";

                // Increment the counter and save it
                preferences.edit().putInt("imageCounter", imageCounter + 1).apply();

                // Metadata for the file
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setName(fileName);
                fileMetadata.setParents(Collections.singletonList("1wZYGu7CyULi3lNLoIUPCj32n6WsfWB4U")); // Specify the folder ID here

                // Process the image if it hasn't been processed yet
                if (currentBitmap != null) {
                    currentBitmap = processImage(currentBitmap);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "No image to upload", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Convert the processed bitmap to a file
                java.io.File filePath = new java.io.File(getFilesDir(), fileName);
                FileOutputStream fos = new FileOutputStream(filePath);
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();

                // Upload the file
                FileContent mediaContent = new FileContent("image/jpeg", filePath);
                com.google.api.services.drive.model.File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();

                runOnUiThread(() -> Toast.makeText(this, "File uploaded! ID: " + uploadedFile.getId(), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    private Bitmap processImage(Bitmap original) {
        // Get the width and height of the original bitmap
        int width = original.getWidth();
        int height = original.getHeight();

        // Calculate the dimensions for the cropped center region (2x zoom)
        int newWidth = width / 2;  // Cropping to 50% width
        int newHeight = height / 2;  // Cropping to 50% height
        int xOffset = (width - newWidth) / 2;  // Center horizontally
        int yOffset = (height - newHeight) / 2;  // Center vertically

        // Crop the bitmap to the center
        Bitmap croppedBitmap = Bitmap.createBitmap(original, xOffset, yOffset, newWidth, newHeight);

        // Scale the cropped bitmap back to original size (2x zoom)
        Matrix matrix = new Matrix();
        matrix.postScale(2.0f, 2.0f);  // Scaling factor is 2x

        return Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight(), matrix, true);
    }


    private void setPic() {
        // Get the dimensions of the screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int targetW = displayMetrics.widthPixels;
        int targetH = displayMetrics.heightPixels;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the ImageView
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        //noinspection deprecation
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        capturedImage.setImageBitmap(bitmap);

        // Display the bitmap in the ImageView
        capturedImage.setImageBitmap(bitmap);

        // Update the currentBitmap variable for processing
        currentBitmap = bitmap;

    }

}
