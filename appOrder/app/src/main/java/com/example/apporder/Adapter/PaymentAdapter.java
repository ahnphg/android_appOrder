package com.example.apporder.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.apporder.Activity.ViewDetailActivity; // Thêm import cho ViewDetailActivity
import com.example.apporder.Model.Payment;
import com.example.apporder.Model.paFood;
import com.example.apporder.R;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    private List<Payment> paymentList;
    private Context context;

    public PaymentAdapter(Context context, List<Payment> paymentList) {
        this.context = context;
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pa_item_history, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = paymentList.get(position);

        holder.status.setText("Completed");
        holder.tableName.setText(payment.getTableName());
        holder.orderNumber.setText("#" + payment.getId());
        holder.price.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(payment.getTotalPrice()) + "đ");
        holder.date.setText(payment.getTimestamp());
        holder.countItem.setText(payment.getQuantities().size() + " items");

        // Log để kiểm tra foodList trước khi truyền
        for (paFood food : payment.getFoodList()) {
            Log.d("PaymentAdapter", "Food: " + food.getName() + ", Price: " + food.getPrice());
        }

        // Xử lý sự kiện nhấn vào nút "Xem chi tiết"
        holder.btnViewDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewDetailActivity.class);
            intent.putExtra("billId", payment.getBillId());
            intent.putExtra("tableId", payment.getTableId());
            intent.putExtra("tableName", payment.getTableName());
            intent.putExtra("orderPerson", payment.getOrderPerson());
            intent.putExtra("timestamp", payment.getTimestamp());
            intent.putExtra("foodList", new ArrayList<>(payment.getFoodList()));
            intent.putExtra("quantities", new ArrayList<>(payment.getQuantities()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return paymentList != null ? paymentList.size() : 0;
    }

    public static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView status, tableName, orderNumber, price, date, countItem;
        View btnViewDetail;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            status = itemView.findViewById(R.id.status);
            tableName = itemView.findViewById(R.id.table_name);
            orderNumber = itemView.findViewById(R.id.order_number);
            price = itemView.findViewById(R.id.price);
            date = itemView.findViewById(R.id.date);
            countItem = itemView.findViewById(R.id.count_item);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
        }
    }
}