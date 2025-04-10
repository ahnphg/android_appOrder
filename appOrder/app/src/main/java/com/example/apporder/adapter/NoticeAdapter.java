package com.example.apporder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.apporder.R;
import com.example.apporder.modules.Notice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder> {
    private List<Notice> notices;

    public static class NoticeViewHolder extends RecyclerView.ViewHolder {
        public TextView txtTen;
        public TextView txtTieuDe;
        public TextView txtNoiDung;
        public TextView txtTime;

        public NoticeViewHolder(View itemView) {
            super(itemView);
            txtTen = itemView.findViewById(R.id.txtTen);
            txtTieuDe = itemView.findViewById(R.id.txtTieuDe);
            txtNoiDung = itemView.findViewById(R.id.txtNoiDung);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }

    public NoticeAdapter(List<Notice> notices) {
        this.notices = notices;
    }

    @Override
    public NoticeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pa_item_noti, parent, false);
        return new NoticeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoticeViewHolder holder, int position) {
        Notice notice = notices.get(position);
        holder.txtTen.setText("Thông báo");
        holder.txtTieuDe.setText(notice.getTitle());
        holder.txtNoiDung.setText(notice.getContent());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.txtTime.setText(dateFormat.format(new Date(notice.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }
}