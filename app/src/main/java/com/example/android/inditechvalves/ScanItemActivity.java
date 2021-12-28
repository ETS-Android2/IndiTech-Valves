package com.example.android.inditechvalves;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ScanItemActivity extends AppCompatActivity  {
    private EditText barcodeToBeSearched;
    private LinearLayout detailsLayout;
    private TextView details, materialType, barcode;
    private Button scan, search;
    private Object obj;
    private ProgressDialog processDialog;
    private List<Document> uploadedDocs;
    private ArrayList<String> uploadedDocsNames;
    private ListView docsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan_item);
        barcodeToBeSearched = findViewById(R.id.barcodeToBeSearched);
        scan = findViewById(R.id.scan);
        search = findViewById(R.id.search);
        details = findViewById(R.id.details);
        detailsLayout = findViewById(R.id.details_layout);
        materialType = findViewById(R.id.material_type);
        barcode = findViewById(R.id.barcode_value);
        docsList = findViewById(R.id.documents_list);
        processDialog = new ProgressDialog(this);
        uploadedDocs = new ArrayList<>();
        uploadedDocsNames = new ArrayList<>();
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(barcodeToBeSearched.getText())) {
                    barcodeToBeSearched.setError("Please enter barcode to be searched!");
                    return;
                }

                barcodeToBeSearched.clearFocus();
                processDialog.setMessage("Searching Database...");
                processDialog.show();
                detailsLayout.setVisibility(View.GONE);
                String searchQueryText = barcodeToBeSearched.getText().toString().toUpperCase();
                firebasesearch(searchQueryText);
            }
        });
        docsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Document doc = uploadedDocs.get(position);
                Intent intent = new Intent(ScanItemActivity.this, ViewPDFActivity.class);
                intent.putExtra("pdfUrl", doc.getUrl());
                startActivity(intent);

            }
        });

    }

    private void firebasesearch(String searchQuery) {
        String materialIdentifier = searchQuery.substring(0, 2);
        String yearIdentifier = String.valueOf(searchQuery.charAt(2));
        String countIdentifier = searchQuery.substring(3);

        if (obj == null) {
            processDialog.dismiss();
            Toast.makeText(getApplication(), "Material not present!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            processDialog.dismiss();
            detailsLayout.setVisibility(View.VISIBLE);
            barcode.setText(barcodeToBeSearched.getText().toString().toUpperCase());
            switch (materialIdentifier) {
                case "RB":
                    materialType.setText("Rolled Bar");
                    details.setText("Bar ID: " + ((RolledBarMaterial) obj).BarID + "\nMaterial of Construction: " + ((RolledBarMaterial) obj).MaterialofConstruction + "\nOuter Diameter: " + ((RolledBarMaterial) obj).OuterDiameter);
                    break;
                case "CG":
                    materialType.setText("Casting");
                    details.setText("Heat Number: " + ((CastingMaterial) obj).heatNumber + "\nSize: " + ((CastingMaterial) obj).size + "\nClass: " + ((CastingMaterial) obj).castingClass + "\nType: " + ((CastingMaterial) obj).typeOfCasting + "\nMaterial of Construction: " + ((CastingMaterial) obj).materialOfConstruction);
                    break;
                case "FB":
                    materialType.setText("Forged Bar");
                    details.setText("Bar ID: " + ((ForgedBarMaterial) obj).BarID + "\nMaterial of Construction: " + ((ForgedBarMaterial) obj).MaterialofConstruction + "\nOuter Diameter: " + ((ForgedBarMaterial) obj).OuterDiameter);
                    break;
                case "PS":
                    materialType.setText("Positioner");
                    details.setText("Model: " + ((PositionerMaterial) obj).model + "\nMake: " + ((PositionerMaterial) obj).make + "\nInvoice: " + ((PositionerMaterial) obj).invoice);
                    break;
                case "SA":
                    materialType.setText("Electrical Actuator-Auma Make");
                    details.setText("Serial Number: " + ((SDTorkActuatorMaterial) obj).SrNumber);
                    break;
                case "AA":
                    materialType.setText("Electrical Actuator-SD Tork Make");
                    details.setText("Works Number: " + ((AumaActuatorMaterial) obj).worksNumber);
                    break;
            }
        }
    }

}