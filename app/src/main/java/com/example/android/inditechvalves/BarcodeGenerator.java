package com.example.android.inditechvalves;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class BarcodeGenerator {
    private static MultiFormatWriter multiFormatWriter=new MultiFormatWriter();
    private BarcodeGenerator() { }
    public static Bitmap generate(String value) throws WriterException {
        BitMatrix bitMatrix=multiFormatWriter.encode(value, BarcodeFormat.CODE_128,400,170,null);
        BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
        Bitmap bitmap=barcodeEncoder.createBitmap(bitMatrix);
        return bitmap;
    }

}
