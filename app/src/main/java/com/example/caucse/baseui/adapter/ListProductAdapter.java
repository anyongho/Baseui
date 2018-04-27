package com.example.caucse.baseui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.caucse.baseui.R;
import com.example.caucse.baseui.model.Product;

import java.util.List;

public class ListProductAdapter extends BaseAdapter{
    private Context mContext;
    private List<Product> mProductList;

    public ListProductAdapter(Context mContext, List<Product> mProductList) {
        this.mContext = mContext;
        this.mProductList = mProductList;
    }

    @Override
    public int getCount() {
        return mProductList.size();
    }

    @Override
    public Object getItem(int position) {
        return mProductList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mProductList.get(position).getId();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View v = View.inflate(mContext, R.layout.item_listview, null);
        TextView tvName = (TextView)v.findViewById(R.id.tv_product_name);
        TextView tvCountry = (TextView)v.findViewById(R.id.tv_product_country);
        TextView tvFlavor = (TextView)v.findViewById(R.id.tv_product_Flavor);
        TextView tvKind = (TextView)v.findViewById(R.id.tv_product_kind);
        TextView tvIBU = (TextView)v.findViewById(R.id.tv_product_IBU);
        TextView tvAlcohol = (TextView)v.findViewById(R.id.tv_product_Alcohol);
        TextView tvKcal = (TextView)v.findViewById(R.id.tv_product_Kcal);
        tvName.setText(mProductList.get(position).getName());
        tvCountry.setText(mProductList.get(position).getCountry());
        tvFlavor.setText(mProductList.get(position).getFlavor());
        tvKind.setText(mProductList.get(position).getKind());
        tvIBU.setText(String.valueOf(mProductList.get(position).getIBU()));
        tvAlcohol.setText(String.valueOf(mProductList.get(position).getAlcohol()));
        tvKcal.setText(String.valueOf(mProductList.get(position).getKcal()));
        return v;
    }
}
