package com.example.apporder.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.example.apporder.Activity.PaymentActivity;
import com.example.apporder.Model.Bill;
import com.example.apporder.R;

public class phBillAdapter extends RecyclerView.Adapter<phBillAdapter.BillViewHolder> {
    private Context context;
    private List<Bill> billList;

    public phBillAdapter(Context context, List<Bill> billList) {
        this.context = context;
        this.billList = billList;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pa_item_ongoing, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = billList.get(position);

        holder.tableName.setText(bill.getTableName());
        holder.orderNumber.setText("#" + bill.getId());
        holder.price.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(bill.getTotalPrice()) + "đ");
        holder.date.setText(bill.getTimestamp());
        holder.countItem.setText(bill.getQuantities().size() + " items");

        holder.btnThanhToan.setOnClickListener(v -> {
            Intent intent = new Intent(context, PaymentActivity.class);
            intent.putExtra("billId", bill.getId());
            intent.putExtra("tableId", bill.getTableId());
            intent.putExtra("tableName", bill.getTableName());
            context.startActivity(intent);
        });

        holder.btnXoa.setOnClickListener(v -> {
            // Xử lý xóa hóa đơn nếu cần
        });
    }

    @Override
    public int getItemCount() {
        return billList != null ? billList.size() : 0;
    }

    public static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView tableName, orderNumber, price, date, countItem;
        View btnThanhToan, btnXoa;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            tableName = itemView.findViewById(R.id.table_name);
            orderNumber = itemView.findViewById(R.id.order_number);
            price = itemView.findViewById(R.id.price);
            date = itemView.findViewById(R.id.date);
            countItem = itemView.findViewById(R.id.count_item);
            btnThanhToan = itemView.findViewById(R.id.btnThanhtoan);
            btnXoa = itemView.findViewById(R.id.btnXoa);
        }
    }
}