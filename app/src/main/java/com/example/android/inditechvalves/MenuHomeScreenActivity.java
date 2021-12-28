package com.example.android.inditechvalves;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Base64;
import java.util.Objects;

public class MenuHomeScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private CardView addItems, uploadDocs, scanItems, viewInventory , addValves;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Valves");

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_home_screen);

        mAuth = FirebaseAuth.getInstance();

        addItems = (CardView)findViewById(R.id.addItems);
        uploadDocs = (CardView) findViewById(R.id.uploadDocs);
        scanItems = (CardView) findViewById(R.id.scanItems);
        viewInventory = (CardView) findViewById(R.id.viewInventory);
        addValves = (CardView) findViewById(R.id.addValves);


        addItems.setOnClickListener(this);
        uploadDocs.setOnClickListener(this);
        scanItems.setOnClickListener(this);
        viewInventory.setOnClickListener(this);
        addValves.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Intent i;

        switch (view.getId()){
            case R.id.addItems :
                 if (mAuth.getCurrentUser()!=null){
                     i = new Intent(this,AddItemActivity.class);
                     startActivity(i);
                 }else {
                     startActivity(new Intent(MenuHomeScreenActivity.this, LoginActivity.class));
                 }
                 break;
            case R.id.uploadDocs :
                if (mAuth.getCurrentUser()!=null){
                    i = new Intent(this, UploadDocsActivity.class);
                    startActivity(i);
                }else {
                    startActivity(new Intent(MenuHomeScreenActivity.this, LoginActivity.class));
                }
                break;
            case R.id.scanItems :
                IntentIntegrator intentIntegrator = new IntentIntegrator(MenuHomeScreenActivity.this);
                intentIntegrator.setPrompt("Scan QR Easy");
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.setCaptureActivity(BarScanner.class);
                intentIntegrator.initiateScan();
                break;
            case R.id.viewInventory :
                if (mAuth.getCurrentUser()!=null){
                    i = new Intent(this,ViewInventoryActivity.class);
                    startActivity(i);
                }else {
                    startActivity(new Intent(MenuHomeScreenActivity.this, LoginActivity.class));
                }
                break;
            case R.id.addValves :
                if (mAuth.getCurrentUser()!=null){
                    i = new Intent(this,AddValves.class);
                    startActivity(i);
                }else {
                    startActivity(new Intent(MenuHomeScreenActivity.this, LoginActivity.class));
                }
            default: break;
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, data);
        if (intentResult.getContents()!=null){

            String add_of_bar = intentResult.getContents();
            String first = add_of_bar.substring(0,2);

            final Dialog dialog = new Dialog(MenuHomeScreenActivity.this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.activity_more_details);
            LinearLayout linearLayout = dialog.findViewById(R.id.linear2);
            TextView title_name, size_no, cls_display,toc,moc,third, barcode_num;
            title_name = dialog.findViewById(R.id.heading_cast_more);
            size_no = dialog.findViewById(R.id.size_no_display);
            cls_display = dialog.findViewById(R.id.class_display);
            toc = dialog.findViewById(R.id.toc_displaya);
            moc = dialog.findViewById(R.id.moc_display);
            third = dialog.findViewById(R.id.third_textview);
            barcode_num = dialog.findViewById(R.id.barcode_number_display);
            ImageView barImage = dialog.findViewById(R.id.barcode_display);

            dialog.findViewById(R.id.more_cancel_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            //dialog.show();
            if (first.equals("AA")){
                //Auma Actuator
                toc.setVisibility(View.INVISIBLE);

                db.collection("Valves")
                        .whereEqualTo("bar_number", intentResult.getContents())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                        byte[] bytes= Base64.getDecoder().decode(document.getString("bar_url"));
                                        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                        barImage.setImageBitmap(bitmap);
                                        title_name.setText(document.getString("name_of_material"));
                                        moc.setText(getString(R.string.work_number_display).concat(Objects.requireNonNull(document.getString("work_num"))));
                                        barcode_num.setText(document.getString("bar_number"));
                                        dialog.show();
                                    }
                                } else {
                                    Toast.makeText(MenuHomeScreenActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }if (first.equals("CA")){
                //Casting
                linearLayout.setVisibility(View.VISIBLE);


                db.collection("Valves")
                        .whereEqualTo("bar_number", intentResult.getContents())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                        byte[] bytes= Base64.getDecoder().decode(document.getString("bar_url"));
                                        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                        barImage.setImageBitmap(bitmap);
                                        title_name.setText(document.getString("name_of_material"));

                                        size_no.setText(getString(R.string.size_num_display).concat(Objects.requireNonNull(document.getString("size"))));
                                        cls_display.setText(getString(R.string.cls_display).concat(Objects.requireNonNull(document.getString("cls_casting"))));
                                        toc.setText(getString(R.string.toc_display).concat(Objects.requireNonNull(document.getString("type"))));
                                        moc.setText(getString(R.string.moc_display).concat(Objects.requireNonNull(document.getString("material_of_const"))));

                                        barcode_num.setText(document.getString("bar_number"));
                                        dialog.show();
                                    }
                                } else {
                                    Toast.makeText(MenuHomeScreenActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.show();
            }if (first.equals("FB")){
                //Forged Bar
                third.setVisibility(View.VISIBLE);
                db.collection("Valves")
                        .whereEqualTo("bar_number", intentResult.getContents())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                        byte[] bytes= Base64.getDecoder().decode(document.getString("bar_url"));
                                        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                        barImage.setImageBitmap(bitmap);
                                        title_name.setText(document.getString("name_of_material"));

                                        toc.setText(getString(R.string.dim).concat(Objects.requireNonNull(document.getString("outer_dim"))));
                                        moc.setText(getString(R.string.barId).concat(Objects.requireNonNull(document.getString("bar_id"))));
                                        third.setText(getString(R.string.moc_display).concat(Objects.requireNonNull(document.getString("material_of_const"))));

                                        barcode_num.setText(document.getString("bar_number"));
                                        dialog.show();
                                    }
                                } else {
                                    Toast.makeText(MenuHomeScreenActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.show();
            }if (first.equals("PO")){
                third.setVisibility(View.VISIBLE);
                db.collection("Valves")
                        .whereEqualTo("bar_number", intentResult.getContents())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                        byte[] bytes= Base64.getDecoder().decode(document.getString("bar_url"));
                                        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                        barImage.setImageBitmap(bitmap);
                                        title_name.setText(document.getString("name_of_material"));

                                        toc.setText(getString(R.string.model_display).concat(Objects.requireNonNull(document.getString("model"))));
                                        moc.setText(getString(R.string.make_display).concat(Objects.requireNonNull(document.getString("make"))));
                                        third.setText(getString(R.string.invoice_display).concat(Objects.requireNonNull(document.getString("invoice"))));

                                        barcode_num.setText(document.getString("bar_number"));
                                        dialog.show();
                                    }
                                } else {
                                    Toast.makeText(MenuHomeScreenActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                dialog.show();
            }if (first.equals("RB")){
                third.setVisibility(View.VISIBLE);
                db.collection("Valves")
                        .whereEqualTo("bar_number", intentResult.getContents())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                        byte[] bytes= Base64.getDecoder().decode(document.getString("bar_url"));
                                        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                        barImage.setImageBitmap(bitmap);
                                        title_name.setText(document.getString("name_of_material"));

                                        toc.setText(getString(R.string.dim).concat(Objects.requireNonNull(document.getString("outer_dim"))));
                                        moc.setText(getString(R.string.barId).concat(Objects.requireNonNull(document.getString("bar_id"))));
                                        third.setText(getString(R.string.moc_display).concat(Objects.requireNonNull(document.getString("material_of_const"))));

                                        barcode_num.setText(document.getString("bar_number"));
                                        dialog.show();
                                    }
                                } else {
                                    Toast.makeText(MenuHomeScreenActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.show();
            }if (first.equals("SA")){
                toc.setVisibility(View.INVISIBLE);

                db.collection("Valves")
                        .whereEqualTo("bar_number", intentResult.getContents())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                        byte[] bytes= Base64.getDecoder().decode(document.getString("bar_url"));
                                        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                        barImage.setImageBitmap(bitmap);
                                        title_name.setText(document.getString("name_of_material"));

                                        moc.setText(getString(R.string.serial).concat(Objects.requireNonNull(document.getString("serial_num"))));
                                        barcode_num.setText(document.getString("bar_number"));
                                        dialog.show();
                                    }
                                } else {
                                    Toast.makeText(MenuHomeScreenActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                dialog.show();
            }
            if (first.equals("QR")){
                Intent intent = new Intent(this,QrResult.class);
                Bundle bundle = new Bundle();
                bundle.putString("docId", add_of_bar);
                intent.putExtras(bundle);
                startActivity(intent);
            }

        }else {
            Toast.makeText(MenuHomeScreenActivity.this, "OOPS... You did not scan anything", Toast.LENGTH_SHORT).show();
        }
    }
    }
