package com.example.android.inditechvalves.Adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.example.android.inditechvalves.R;

import java.util.Base64;

public class FrontAdapter extends FirestoreRecyclerAdapter<Note, FrontAdapter.NoteHolder> {
    public FrontAdapter(@NonNull FirestoreRecyclerOptions<Note> options) {
        super(options);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onBindViewHolder(@NonNull NoteHolder holder, int position, @NonNull Note model) {

        holder.bar_number.setText(model.getBar_number());
        String url = model.getPdf_url();
        holder.btnShare.setOnClickListener(v -> {
            byte[] bytes= Base64.getDecoder().decode(model.getBar_url());
            Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            String bitmapPath = MediaStore.Images.Media.insertImage(v.getContext().getContentResolver(), bitmap,model.getName_of_material(), null);
            Uri bitmapUri = Uri.parse(bitmapPath);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
            sendIntent.setType("image/png");
            v.getContext().startActivity(sendIntent);
        });
        holder.itemView.setOnClickListener(v -> {
            if (TextUtils.isEmpty(url) || url.startsWith("url")){
                Toast.makeText(v.getContext(), "No file attach", Toast.LENGTH_SHORT).show();
            }else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url),"application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                v.getContext().startActivity(intent);
            }
        });

    }
    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.front_recycler,
                parent, false);
        return new NoteHolder(v);
    }
    static class NoteHolder extends RecyclerView.ViewHolder {
        TextView bar_number;
        Button btnShare;
        public NoteHolder(View itemView) {
            super(itemView);
            bar_number = itemView.findViewById(R.id.barId);
            btnShare = itemView.findViewById(R.id.btnShare);

        }
    }
}