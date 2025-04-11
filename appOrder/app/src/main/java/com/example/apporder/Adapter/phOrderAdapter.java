package com.example.apporder.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apporder.Model.paFood;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.example.apporder.R;


public class phOrderAdapter extends RecyclerView.Adapter<phOrderAdapter.FoodViewHolder> {

    private List<paFood> foodList;
    private HashMap<String, Integer> quantityMap = new HashMap<>();
    private boolean isReadOnly = false;

    public phOrderAdapter(List<paFood> foodList) {
        this.foodList = foodList;
        for (paFood food : foodList) {
            quantityMap.put(food.getId(), 0);
        }
    }

    public phOrderAdapter(List<paFood> foodList, List<Integer> quantities) {
        this.foodList = foodList;
        for (int i = 0; i < foodList.size(); i++) {
            quantityMap.put(foodList.get(i).getId(), quantities.get(i));
        }
        isReadOnly = true;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pa_item_pay, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        paFood food = foodList.get(position);
        int quantity = quantityMap.get(food.getId());

        // Log để kiểm tra giá trị price
        Log.d("phOrderAdapter", "Food: " + food.getName() + ", Price: " + food.getPrice());

        // Hiển thị số lượng món
        holder.txtSoLuong.setText(String.valueOf(quantity));

        // Hiển thị tên món
        holder.itemName.setText(food.getName());

        // Hiển thị giá món với đơn vị VNĐ
        String priceFormatted = NumberFormat.getNumberInstance(Locale.getDefault()).format(food.getPrice()) + " VNĐ";
        holder.price.setText(priceFormatted);
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView txtSoLuong, itemName, price;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSoLuong = itemView.findViewById(R.id.txtSoLuong);
            itemName = itemView.findViewById(R.id.item_name);
            price = itemView.findViewById(R.id.price);
        }
    }

    public HashMap<String, Integer> getQuantityMap() {
        return quantityMap;
    }

    public List<paFood> getFoodList() {
        return foodList;
    }
}