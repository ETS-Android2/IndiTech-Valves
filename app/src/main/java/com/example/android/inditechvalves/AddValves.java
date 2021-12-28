package com.example.android.inditechvalves;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.android.inditechvalves.Adapter.Note;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class AddValves extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private TextInputEditText valveName, barIds;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    String timeStamp,pdfFile;;
    ProgressBar progressBar;
    ImageView showBar;
    Button share,print;
    File myPDFFile;
    @RequiresApi(api = Build.VERSION_CODES.O)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valves_item);
        PDFBoxResourceLoader.init(getApplicationContext());
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        valveName = findViewById(R.id.editValveName);
        barIds = findViewById(R.id.edit_bar_id_bottom_sheet);
        progressBar = findViewById(R.id.progress);
        timeStamp = "QR"+TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());

        showBar=findViewById(R.id.showBar);
        print = findViewById(R.id.printQR);
        share = findViewById(R.id.shareQR);
        showBar.setVisibility(View.GONE);
        print.setEnabled(false);
        share.setEnabled(false);
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/";
        findViewById(R.id.btnGenerate).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(AddValves.this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},PackageManager.PERMISSION_GRANTED);
            }
            List<String> items = Arrays.asList(Objects.requireNonNull(Objects.requireNonNull(barIds.getText()).toString().split("\\s*,\\s*")));
            progressBar.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(valveName.getText()) || TextUtils.isEmpty(barIds.getText())){
                Toast.makeText(AddValves.this, "Please enter details", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }else {
                db.collection("Valves")
                        .whereIn("bar_number", items)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots){
                                Note note = queryDocumentSnapshot.toObject(Note.class);
                                note.setDocId(queryDocumentSnapshot.getId());
                                String documentId = note.getPdf_url();
                                String bar_number = note.getBar_number();
                                Map<String, Object> data2 = new HashMap<>();
                                data2.put("pdf_url", documentId);
                                data2.put("bar_number",bar_number);
                                db.collection("QRs").document(timeStamp).collection("files").add(data2)
                                        .addOnSuccessListener(documentReference -> {

                                            Toast.makeText(v.getContext(), "Success", Toast.LENGTH_SHORT).show();
                                            Log.e("files", "Write success");
                                            progressBar.setVisibility(View.GONE);
                                        });
                            }
                            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                            try {
                                progressBar.setVisibility(View.VISIBLE);
                                BitMatrix bitMatrix = multiFormatWriter.encode(timeStamp, BarcodeFormat.QR_CODE,212,512);
                                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                byte[] byteArray = byteArrayOutputStream .toByteArray();
                                String encodeImage = Base64.getEncoder().encodeToString(byteArray);
                                Map<String, Object> data3 = new HashMap<>();
                                data3.put("bar_number", timeStamp);
                                data3.put("name_of_material", Objects.requireNonNull(valveName.getText()).toString());
                                data3.put("bar_url", encodeImage);
                                db.collection("QRs").document(timeStamp).set(data3);
                                progressBar.setVisibility(View.GONE);

                                showBar.setImageBitmap(bitmap);

                                showBar.setVisibility(View.VISIBLE);
                                PdfDocument pdfDocument = new PdfDocument();
                                PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(384, 384, 1).create();
                                PdfDocument.Page page = pdfDocument.startPage(myPageInfo);
                                page.getCanvas().drawBitmap(bitmap, 10, 10, null);
                                pdfDocument.finishPage(page);

                                pdfFile = directory + "/" + timeStamp + ".pdf";
                                myPDFFile = new File(pdfFile);
                                try {
                                    pdfDocument.writeTo(new FileOutputStream(myPDFFile));
                                    Toast.makeText(AddValves.this, "Wrote to file", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(AddValves.this, "WRITE ERROR: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                pdfDocument.close();
                                share.setEnabled(true);
                                print.setEnabled(true);
                                share.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        byte[] bytes= Base64.getDecoder().decode(encodeImage);
                                        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                        String bitmapPath = MediaStore.Images.Media.insertImage(v.getContext().getContentResolver(), bitmap,timeStamp, null);
                                        Uri bitmapUri = Uri.parse(String.valueOf(bitmapPath));
                                        Intent sendIntent = new Intent();
                                        sendIntent.setAction(Intent.ACTION_SEND);
                                        sendIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                                        sendIntent.setType("image/png");
                                        v.getContext().startActivity(sendIntent);
                                    }
                                });
                                print.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(v.getContext().getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", myPDFFile);
                                            //Uri uri = FileProvider.getUriForFile(AddValves.this, BuildConfig.APPLICATION_ID + ".provider",myPDFFile);
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setDataAndType(uri,"application/pdf");
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            v.getContext().startActivity(intent);
                                        }catch (Exception e){
                                        e.printStackTrace();
                                        Toast.makeText(AddValves.this, "PrintBtn: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } catch (WriterException e) {
                                Toast.makeText(v.getContext(), ""+e, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });


    }
}