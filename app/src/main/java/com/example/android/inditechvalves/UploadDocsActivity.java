package com.example.android.inditechvalves;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UploadDocsActivity extends AppCompatActivity {

    TextInputEditText editText;
    private ProgressBar progressBar;
    private FirebaseFirestore firebaseFirestore;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private StorageReference storageRef;
    private PDFView fileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_docs);


        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        editText = findViewById(R.id.EditInpBarNum);
        TextView chooseFile = findViewById(R.id.chooseFile);
        progressBar = findViewById(R.id.progressUpload);
        fileName = findViewById(R.id.pdfView);
        chooseFile.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(UploadDocsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(UploadDocsActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(UploadDocsActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                openFileChooser();
            }
        });

        findViewById(R.id.scanBar).setOnClickListener(v -> {

            if (mImageUri == null || TextUtils.isEmpty(editText.getText())) {
                Toast.makeText(getApplicationContext(), "Fill the detail first!", Toast.LENGTH_SHORT).show();
            } else {

                progressBar.setVisibility(View.VISIBLE);
                uploadFile();
            }

        });


    }



    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select PDF File"),PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile() {
        if (mImageUri != null) {
            StorageReference ref = storageRef.child(System.currentTimeMillis() + "" + "." + getFileExtension(mImageUri) + ".pdf");
            UploadTask image_path = ref.putFile(mImageUri);
            Task<Uri> urlTask = image_path.continueWithTask(task -> {
                if (task.isSuccessful()) {

                    UploadTask.TaskSnapshot downloadUri = task.getResult();
                    assert downloadUri != null;
                    Log.e("TASK:", "" + downloadUri.toString());
                }


                return ref.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    assert downloadUri != null;
                    Log.e("URL:", "" + downloadUri.toString());

                    Map<String, Object> hos = new HashMap<>();
                    hos.put("pdf_url", downloadUri.toString());

                    firebaseFirestore.collection("Valves").document(Objects.requireNonNull(editText.getText()).toString())
                            .set(hos, SetOptions.merge());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UploadDocsActivity.this, "File uploaded successfully", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(UploadDocsActivity.this, "Failed to send!!!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);

                }
            });
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            fileName.fromUri(mImageUri).load();

        }

    }
}