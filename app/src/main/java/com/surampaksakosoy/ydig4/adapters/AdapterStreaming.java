package com.surampaksakosoy.ydig4.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.surampaksakosoy.ydig4.R;
import com.surampaksakosoy.ydig4.models.ModelStreaming;

import java.util.List;

public class AdapterStreaming extends RecyclerView.Adapter<AdapterStreaming.Holder> {

    private List<ModelStreaming> modelStreaming;
    private Context context;
    private String ID_LOGIN;

    public AdapterStreaming(List<ModelStreaming> modelStreamings, Context context, String ID_LOGIN){
        this.modelStreaming = modelStreamings;
        this.context = context;
        this.ID_LOGIN = ID_LOGIN;
    }

    @NonNull
    @Override
    public AdapterStreaming.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_streaming, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterStreaming.Holder holder, int position) {
        final ModelStreaming streaming = modelStreaming.get(position);
        if (ID_LOGIN.equals(streaming.getUniq_id())){
            holder.textViewDari.setText(R.string.anda);

        } else {
            holder.textViewDari.setText(streaming.getId_login());
        }
        holder.textViewJam.setText(streaming.getJam());
        holder.textViewPesan.setText(streaming.getPesan());
        if (streaming.getPhoto() != null){
            Glide.with(context).load(streaming.getPhoto()).placeholder(R.drawable.ic_account).into(holder.photo);
        }
    }

    @Override
    public int getItemCount() {
        return modelStreaming.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        TextView textViewDari, textViewJam, textViewPesan;
        de.hdodenhof.circleimageview.CircleImageView photo;
        LinearLayout layout_recycler;

        Holder(@NonNull View itemView) {
            super(itemView);
            layout_recycler = itemView.findViewById(R.id.layout_recycler);
            textViewDari = itemView.findViewById(R.id.streaming_dari);
            textViewJam = itemView.findViewById(R.id.streaming_jam);
            textViewPesan = itemView.findViewById(R.id.streaming_pesan);
            photo = itemView.findViewById(R.id.streaming_photo);
        }
    }
}
