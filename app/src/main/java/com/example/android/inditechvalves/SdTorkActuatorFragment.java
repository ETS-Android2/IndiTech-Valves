package com.example.android.inditechvalves;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class SdTorkActuatorFragment extends Fragment {
    private EditText srNumber;
    Bitmap bitmap;
    private ImageView barcodeImage;
    private Button printBarcode;
    private Button share;
    private TextView barcodeHumanReadableText;
    String pdfFile;
    File myPDFFile;
    private String timeStamp;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sd_tork_actuator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        srNumber = view.findViewById(R.id.sr_number);
        Button generateBarcode = view.findViewById(R.id.generate_barcode);
        barcodeImage = view.findViewById(R.id.barcode_image);
        barcodeHumanReadableText = view.findViewById(R.id.barcode_hrt);
        share = view.findViewById(R.id.shareBar);
        printBarcode = view.findViewById(R.id.printBar);
        share.setEnabled(false);
        printBarcode.setEnabled(false);


        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        timeStamp = "SA"+ TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        generateBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},PackageManager.PERMISSION_GRANTED);
                }
                if (TextUtils.isEmpty(srNumber.getText())){
                    Toast.makeText(v.getContext(), "Please enter details!!!", Toast.LENGTH_SHORT).show();
                }else {
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(timeStamp, BarcodeFormat.CODE_128,380,150);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        bitmap = barcodeEncoder.createBitmap(bitMatrix);

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        byte[] byteArray = byteArrayOutputStream .toByteArray();
                        String encodeImage = Base64.getEncoder().encodeToString(byteArray);
                        //Toast.makeText(view.getContext(), ""+ encodeImage, Toast.LENGTH_SHORT).show();
                        Log.e("Base64",encodeImage);

                        Map<String, Object> data = new HashMap<>();
                        data.put("bar_number", timeStamp);
                        data.put("bar_url", encodeImage);
                        data.put("name_of_material","SD Tork Actuator");
                        data.put("serial_num", Objects.requireNonNull(srNumber.getText()).toString());
                        db.collection("Valves").document(timeStamp).set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                          @Override
                                                          public void onSuccess(Void unused) {
                                                              Toast.makeText(view.getContext(), "Success", Toast.LENGTH_SHORT).show();
                                                              //String file = directory + "3.jpg";
                                                              //Bitmap bitmap = BitmapFactory.decodeFile(encodeImage);

                                                              PdfDocument pdfDocument = new PdfDocument();
                                                              PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(384, 384, 1).create();
                                                              PdfDocument.Page page = pdfDocument.startPage(myPageInfo);
                                                              page.getCanvas().drawBitmap(bitmap, 0, 50, null);
                                                              pdfDocument.finishPage(page);

                                                              pdfFile = directory + "/" + timeStamp + ".pdf";
                                                              myPDFFile = new File(pdfFile);

                                                              try {
                                                                  pdfDocument.writeTo(new FileOutputStream(myPDFFile));
                                                                  Toast.makeText(requireActivity(), "Wrote to file", Toast.LENGTH_SHORT).show();
                                                              } catch (IOException e) {
                                                                  e.printStackTrace();
                                                                  Toast.makeText(getContext(), "WRITE ERROR: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                              }
                                                              pdfDocument.close();

                                        barcodeImage.setVisibility(View.VISIBLE);
                                        barcodeImage.setImageBitmap(bitmap);
                                        barcodeHumanReadableText.setVisibility(View.VISIBLE);
                                        barcodeHumanReadableText.setText(timeStamp);
                                        share.setEnabled(true);
                                        printBarcode.setEnabled(true);

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
                                                              printBarcode.setOnClickListener(new View.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(View v) {

                                                                      try {
                                                                          Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(v.getContext().getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", myPDFFile);
                                                                          Intent intent = new Intent(Intent.ACTION_VIEW);
                                                                          intent.setDataAndType(uri, "application/pdf");
                                                                          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                          intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                                          v.getContext().startActivity(intent);
                                                                      }catch (Exception e){
                                                                          e.printStackTrace();
                                                                          Toast.makeText(getContext(), "PrintBtn: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                      }

                                                                  }
                                                              });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(view.getContext(), "error :"+e, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } catch (WriterException e) {
                        Toast.makeText(view.getContext(), ""+e, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

}