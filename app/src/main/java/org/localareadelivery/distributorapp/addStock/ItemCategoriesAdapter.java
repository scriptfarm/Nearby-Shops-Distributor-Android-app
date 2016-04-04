package org.localareadelivery.distributorapp.addStock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.localareadelivery.distributorapp.Model.ItemCategory;
import org.localareadelivery.distributorapp.R;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by sumeet on 19/12/15.
 */


public class ItemCategoriesAdapter extends RecyclerView.Adapter<ItemCategoriesAdapter.ViewHolder>{


    List<ItemCategory> dataset;

    Context context;
    ItemCategories itemCategories;

    final String IMAGE_ENDPOINT_URL = "/api/Images";

    public ItemCategoriesAdapter(List<ItemCategory> dataset, Context context, ItemCategories itemCategories) {

        this.dataset = dataset;
        this.context = context;
        this.itemCategories = itemCategories;

        if(this.dataset == null)
        {
            this.dataset = new ArrayList<ItemCategory>();
        }

    }

    @Override
    public ItemCategoriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_addstock_item_category,parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemCategoriesAdapter.ViewHolder holder, final int position) {

        holder.categoryName.setText(dataset.get(position).getCategoryName());
        holder.categoryDescription.setText(dataset.get(position).getCategoryDescription());


        String imagePath = getServiceURL() + IMAGE_ENDPOINT_URL + dataset.get(position).getImagePath();

        Picasso.with(context).load(imagePath).placeholder(R.drawable.nature_people).into(holder.categoryImage);

        Log.d("applog",imagePath);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        }

        final int positionInner = position;


        holder.itemCategoryListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (dataset.get(positionInner).getIsLeafNode()) {

                    Intent intent = new Intent(context, Items.class);

                    intent.putExtra(Items.ITEM_CATEGORY_INTENT_KEY,dataset.get(position));

                    context.startActivity(intent);

                }
                else
                {

                    itemCategories.notifyRequestSubCategory(dataset.get(positionInner));
                }

            }
        });

    }


    @Override
    public int getItemCount() {

        return dataset.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        private Button editButton;

        Button detachButton;

        Button deleteButton;

        private TextView categoryName,categoryDescription;
        private LinearLayout itemCategoryListItem;
        @Bind(R.id.categoryImage) ImageView categoryImage;

        @Bind(R.id.deleteIcon) ImageView deleteIcon;
        @Bind(R.id.editIcon) ImageView editIcon;
        @Bind(R.id.textviewEdit) TextView textViewEdit;




        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this,itemView);

            categoryImage = (ImageView) itemView.findViewById(R.id.categoryImage);
            categoryName = (TextView) itemView.findViewById(R.id.categoryName);
            categoryDescription = (TextView) itemView.findViewById(R.id.categoryDescription);
            editButton = (Button) itemView.findViewById(R.id.editButton);
            detachButton = (Button) itemView.findViewById(R.id.detachButton);
            deleteButton = (Button) itemView.findViewById(R.id.deleteButton);

            itemCategoryListItem = (LinearLayout) itemView.findViewById(R.id.itemCategoryListItem);
        }
    }


    public void notifyDelete()
    {
        itemCategories.notifyDelete();

    }


    public String  getServiceURL()
    {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_name), context.MODE_PRIVATE);

        String service_url = sharedPref.getString(context.getString(R.string.preference_service_url_key),"default");

        return service_url;
    }

    public interface requestSubCategory
    {
        // method for notifying the list object to request sub category
        public void notifyRequestSubCategory(ItemCategory itemCategory);
    }



}