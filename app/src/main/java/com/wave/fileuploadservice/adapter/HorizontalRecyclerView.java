package com.wave.fileuploadservice.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wave.fileuploadservice.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class HorizontalRecyclerView extends RecyclerView.Adapter<HorizontalRecyclerView.ViewHolder> {
    private static final String TAG = "HorizontalRecyclerView";
    private ArrayList<Uri> uri;

    public HorizontalRecyclerView(ArrayList<Uri> uri) {
        this.uri = uri;
    }
    @NonNull
    @NotNull
    @Override
    public HorizontalRecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.single_item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HorizontalRecyclerView.ViewHolder holder, final int position) {
        holder.mImageRecyclerView.setImageURI(uri.get(position));
        holder.iv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "position: "+position);
                Log.d(TAG, "size: "+uri.size());

                uri.remove(position);
                notifyDataSetChanged();
                notifyItemRemoved(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return uri.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageRecyclerView,iv_delete;
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mImageRecyclerView = itemView.findViewById(R.id.iv_images);
            iv_delete = itemView.findViewById(R.id.iv_delete);
        }
    }
}