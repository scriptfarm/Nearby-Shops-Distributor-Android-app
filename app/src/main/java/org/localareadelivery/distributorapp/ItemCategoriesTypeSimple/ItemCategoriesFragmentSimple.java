package org.localareadelivery.distributorapp.ItemCategoriesTypeSimple;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import org.localareadelivery.distributorapp.DaggerComponentBuilder;
import org.localareadelivery.distributorapp.ItemCategoriesTabs.Interfaces.NotifySort;
import org.localareadelivery.distributorapp.ItemCategoriesTabs.Interfaces.ToggleFab;
import org.localareadelivery.distributorapp.ItemCategoriesTypeSimple.Interfaces.NotifyBackPressed;
import org.localareadelivery.distributorapp.ItemCategoriesTypeSimple.Interfaces.NotifyFABClick;
import org.localareadelivery.distributorapp.ItemCategoriesTypeSimple.Interfaces.NotifyHeaderChanged;
import org.localareadelivery.distributorapp.ItemCategoriesTypeSimple.Interfaces.NotifySearch;
import org.localareadelivery.distributorapp.ItemCategoriesTypeSimple.Utility.HeaderItemsList;
import org.localareadelivery.distributorapp.ItemCategoriesTypeSimple.Utility.UtilitySortItemsByCategory;
import org.localareadelivery.distributorapp.Model.Item;
import org.localareadelivery.distributorapp.Model.ItemCategory;
import org.localareadelivery.distributorapp.Model.ShopItem;
import org.localareadelivery.distributorapp.ModelEndpoints.ItemCategoryEndPoint;
import org.localareadelivery.distributorapp.ModelEndpoints.ItemEndPoint;
import org.localareadelivery.distributorapp.ModelEndpoints.ShopItemEndPoint;
import org.localareadelivery.distributorapp.R;
import org.localareadelivery.distributorapp.RetrofitRESTContract.ItemCategoryService;
import org.localareadelivery.distributorapp.RetrofitRESTContract.ItemService;
import org.localareadelivery.distributorapp.RetrofitRESTContract.ShopItemService;
import org.localareadelivery.distributorapp.ShopHome.UtilityShopHome;
import org.localareadelivery.distributorapp.Utility.UtilityGeneral;
import org.localareadelivery.distributorapp.Utility.UtilityLogin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.State;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sumeet on 2/12/16.
 */

public class ItemCategoriesFragmentSimple extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterSimple.NotificationsFromAdapter , NotifyBackPressed, NotifySort , NotifyFABClick, NotifySearch{

    boolean isDestroyed = false;
    @State boolean show = true;

    int item_count_item_category = 0;

    private int limit_item = 10;
    int offset_item = 0;
    int item_count_item;
    int fetched_items_count = 0;

    @Bind(R.id.swipe_container)
    SwipeRefreshLayout swipeContainer;
    @Bind(R.id.recycler_view)
    RecyclerView itemCategoriesList;

    ArrayList<Object> dataset = new ArrayList<>();
    ArrayList<ItemCategory> datasetCategory = new ArrayList<>();
    ArrayList<Item> datasetItems = new ArrayList<>();


    GridLayoutManager layoutManager;

    AdapterSimple listAdapter;


    @Inject
    ItemCategoryService itemCategoryService;


    @Inject
    ShopItemService shopItemService;

    @Inject
    ItemService itemService;


    ItemCategory currentCategory = null;


    public ItemCategoriesFragmentSimple() {

        super();

        DaggerComponentBuilder.getInstance()
                .getNetComponent().Inject(this);

        currentCategory = new ItemCategory();
        currentCategory.setItemCategoryID(1);
        currentCategory.setCategoryName("");
        currentCategory.setParentCategoryID(-1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_item_categories_simple, container, false);

        ButterKnife.bind(this,rootView);


        if(savedInstanceState ==null)
        {
            makeRefreshNetworkCall();
        }


        setupRecyclerView();
        setupSwipeContainer();
        notifyItemHeaderChanged();
        return rootView;
    }



    void setupSwipeContainer()
    {

        if(swipeContainer!=null) {

            swipeContainer.setOnRefreshListener(this);
            swipeContainer.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }

    }



    void setupRecyclerView()
    {

        listAdapter = new AdapterSimple(dataset,getActivity(),this);
        itemCategoriesList.setAdapter(listAdapter);

        layoutManager = new GridLayoutManager(getActivity(),6, LinearLayoutManager.VERTICAL,false);
        itemCategoriesList.setLayoutManager(layoutManager);



        // Code for Staggered Grid Layout
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {


            @Override
            public int getSpanSize(int position) {

                if(dataset.get(position) instanceof ItemCategory)
                {
                       final DisplayMetrics metrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

                    int spanCount = (int) (metrics.widthPixels/(180 * metrics.density));

                    if(spanCount==0){
                        spanCount = 1;
                    }

                    return (6/spanCount);

                }
                else if(dataset.get(position) instanceof Item)
                {

                    return 6;
                }
                else if(dataset.get(position) instanceof HeaderItemsList)
                {
                    return 6;
                }

                return 6;
            }
        });


        final DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

//        layoutManager.setSpanCount(metrics.widthPixels/350);


//        int spanCount = (int) (metrics.widthPixels/(150 * metrics.density));
//
//        if(spanCount==0){
//            spanCount = 1;
//        }

//        layoutManager.setSpanCount(spanCount);


        /*final DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int spanCount = (int) (metrics.widthPixels/(180 * metrics.density));

        if(spanCount==0){
            spanCount = 1;
        }

        return (3/spanCount);*/


        itemCategoriesList.addOnScrollListener(new RecyclerView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                /*if(layoutManager.findLastVisibleItemPosition()==dataset.size()-1)
                {
                    // trigger fetch next page

                    if((offset_item + limit_item)<=item_count_item)
                    {
                        offset_item = offset_item + limit_item;

                        makeRequestItem(false,false);
                    }

                }
*/


                if(layoutManager.findLastVisibleItemPosition()==dataset.size()-1)
                {

                    // trigger fetch next page

                    if(layoutManager.findLastVisibleItemPosition()== previous_position)
                    {
                        return;
                    }


                    // trigger fetch next page

                    if((offset_item+limit_item)<=item_count_item)
                    {
                        offset_item = offset_item + limit_item;

                        makeRequestItem(false,false);
                    }

                    previous_position = layoutManager.findLastVisibleItemPosition();

                }
            }


            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                if(dy > 20)
                {

                    boolean previous = show;

                    show = false ;

                    if(show!=previous)
                    {
                        // changed
                        Log.d("scrolllog","show");

                        if(getActivity() instanceof ToggleFab)
                        {
                            ((ToggleFab)getActivity()).hideFab();
                        }
                    }

                }else if(dy < -20)
                {

                    boolean previous = show;

                    show = true;

                    if(show!=previous)
                    {
                        Log.d("scrolllog","hide");

                        if(getActivity() instanceof ToggleFab)
                        {
                            ((ToggleFab)getActivity()).showFab();
                        }
                    }
                }


            }


        });

    }


    @State int previous_position = -1;



    @Override
    public void onRefresh() {

        makeRequestItemCategory();
        makeRequestItem(true,true);

        makeNetworkCallShopItem();
    }



    void makeRefreshNetworkCall()
    {
        swipeContainer.post(new Runnable() {
            @Override
            public void run() {

                swipeContainer.setRefreshing(true);
                onRefresh();
            }
        });

    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isDestroyed=false;
    }

    private void showToastMessage(String message)
    {
        if(getActivity()!=null)
        {
            Toast.makeText(getActivity(),message, Toast.LENGTH_SHORT).show();
        }
    }



    boolean isFirst = true;


    void makeRequestItemCategory()
    {




        /*Call<ItemCategoryEndPoint> endPointCall = itemCategoryService.getItemCategoriesEndPoint(
                null,
                currentCategory.getItemCategoryID(),
                null,
                null,
                null,
                null,null,null,
                "ITEM_CATEGORY_NAME",null,null,false);*/

        Call<ItemCategoryEndPoint> endPointCall = null;

        if(searchQuery == null)
        {
            endPointCall = itemCategoryService.getItemCategoriesQuerySimple(
                    currentCategory.getItemCategoryID(),
                    null,
                    null,
                    "id",null,null
            );
        }
        else
        {
            endPointCall = itemCategoryService.getItemCategoriesQuerySimple(
                    null,
                    null,
                    searchQuery,
                    "id",null,null
            );
        }



        endPointCall.enqueue(new Callback<ItemCategoryEndPoint>() {
            @Override
            public void onResponse(Call<ItemCategoryEndPoint> call, Response<ItemCategoryEndPoint> response) {

                if(isDestroyed)
                {
                    return;
                }

                if(response.body()!=null)
                {

                    ItemCategoryEndPoint endPoint = response.body();
                    item_count_item_category = endPoint.getItemCount();

                    datasetCategory.clear();
                    datasetCategory.addAll(endPoint.getResults());
                }


                if(isFirst)
                {
                    isFirst = false;
                }
                else
                {
                    // is last
                    refreshAdapter();
                    isFirst = true;// reset the flag
                }


//                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ItemCategoryEndPoint> call, Throwable t) {


                if(isDestroyed)
                {
                    return;
                }

                showToastMessage("Network request failed. Please check your connection !");


                if(isFirst)
                {
                    isFirst = false;
                }
                else
                {
                    // is last
                    refreshAdapter();
                    isFirst = true;// reset the flag
                }



//                if(swipeContainer!=null)
//                {
//                    swipeContainer.setRefreshing(false);
//                }

            }
        });
    }



    void refreshAdapter()
    {
        dataset.clear();

        HeaderItemsList headerItemCategory = new HeaderItemsList();

        if(searchQuery==null)
        {
            headerItemCategory.setHeading(currentCategory.getCategoryName() + " Subcategories");
        }
        else
        {
            headerItemCategory.setHeading( "Search Results (Subcategories)");
        }

        dataset.add(headerItemCategory);

        dataset.addAll(datasetCategory);

        HeaderItemsList headerItem = new HeaderItemsList();

        if(searchQuery==null)
        {
            headerItem.setHeading(currentCategory.getCategoryName() + " Items");
        }
        else
        {
            headerItem.setHeading("Search Results (Items)");
        }


        dataset.add(headerItem);

        dataset.addAll(datasetItems);

        listAdapter.notifyDataSetChanged();

        swipeContainer.setRefreshing(false);
    }



    String searchQuery = null;

    @Override
    public void search(final String searchString) {
        searchQuery = searchString;
        makeRefreshNetworkCall();
    }

    @Override
    public void endSearchMode() {
        searchQuery = null;
        makeRefreshNetworkCall();
    }



    void makeRequestItem(final boolean clearDataset, boolean resetOffset)
    {

        if(resetOffset)
        {
            offset_item = 0;
        }


        String current_sort = "";

        current_sort = UtilitySortItemsByCategory.getSort(getContext()) + " " + UtilitySortItemsByCategory.getAscending(getContext());
/*
        Call<ItemEndPoint> endPointCall = itemService.getItemsEndpoint(currentCategory.getItemCategoryID(),
                null,
                null,
                null,
                null,null, null, null,
                current_sort, limit_item,offset_item,null);*/

        Call<ItemEndPoint> endPointCall = null;



        if(searchQuery==null)
        {
            endPointCall = itemService.getItemsOuterJoin(
                    currentCategory.getItemCategoryID(),
                    null,
                    current_sort,
                    limit_item,offset_item, null);
        }
        else
        {
            endPointCall = itemService.getItemsOuterJoin(
                    null,
                    searchQuery,
                    current_sort,
                    limit_item,offset_item, null);
        }


        endPointCall.enqueue(new Callback<ItemEndPoint>() {
            @Override
            public void onResponse(Call<ItemEndPoint> call, Response<ItemEndPoint> response) {


                if(isDestroyed)
                {
                    return;
                }


                if(clearDataset)
                {

                    if(response.body()!=null)
                    {

                        datasetItems.clear();
                        datasetItems.addAll(response.body().getResults());
                        item_count_item = response.body().getItemCount();
                        fetched_items_count = datasetItems.size();

//                        if(response.body().getItemCount()!=null)
//                        {
//
//                        }
                    }


                    if(isFirst)
                    {
                        isFirst = false;
                    }
                    else
                    {
                        // is last
                        refreshAdapter();
                        isFirst = true;// reset the flag
                    }

                }
                else
                {
                    if(response.body()!=null)
                    {

                        dataset.addAll(response.body().getResults());
                        fetched_items_count = fetched_items_count + response.body().getResults().size();
                        item_count_item = response.body().getItemCount();
                        listAdapter.notifyDataSetChanged();
                    }

                    swipeContainer.setRefreshing(false);
                }


                notifyItemHeaderChanged();


            }

            @Override
            public void onFailure(Call<ItemEndPoint> call, Throwable t) {

                if(isDestroyed)
                {
                    return;
                }


                if(clearDataset)
                {

                    if(isFirst)
                    {
                        isFirst = false;
                    }
                    else
                    {
                        // is last
                        refreshAdapter();
                        isFirst = true;// reset the flag
                    }
                }
                else
                {
                    swipeContainer.setRefreshing(false);
                }


                showToastMessage("Items: Network request failed. Please check your connection !");

            }
        });

    }





    @Override
    public void notifyRequestSubCategory(ItemCategory itemCategory) {

        ItemCategory temp = currentCategory;
        currentCategory = itemCategory;
        currentCategory.setParentCategory(temp);

        makeRefreshNetworkCall();

        // End Search Mode
        searchQuery = null;
    }

    @Override
    public void notifyItemSelected() {
        if(getActivity() instanceof ToggleFab)
        {
            ((ToggleFab)getActivity()).showFab();
            show=true;
        }
    }


    @Override
    public boolean backPressed() {

        int currentCategoryID = 1; // the ID of root category is always supposed to be 1

        if(currentCategory!=null) {


            if (currentCategory.getParentCategory() != null) {

                currentCategory = currentCategory.getParentCategory();
                currentCategoryID = currentCategory.getItemCategoryID();

            } else {
                currentCategoryID = currentCategory.getParentCategoryID();
            }


            if (currentCategoryID != -1) {
                makeRefreshNetworkCall();
            }
        }

        return currentCategoryID == -1;
    }


    void notifyItemHeaderChanged()
    {
        if(getActivity() instanceof NotifyHeaderChanged)
        {
            ((NotifyHeaderChanged) getActivity()).notifyItemHeaderChanged(String.valueOf(fetched_items_count) + " out of " + String.valueOf(item_count_item) + " " + currentCategory.getCategoryName() + " Items");
        }
    }


    @Override
    public void notifySortChanged() {

        System.out.println("Notify Sort Clicked !");
        makeRefreshNetworkCall();
    }






    // display shop Item Status


    void makeNetworkCallShopItem()
    {

        int currentShopID = UtilityShopHome.getShop(getContext()).getShopID();

        if(currentCategory==null)
        {

            swipeContainer.setRefreshing(false);

            return;
        }

        Call<ShopItemEndPoint> call;


        if(searchQuery!=null)
        {
            call = shopItemService.getShopItemEndpoint(
                    null,
                    currentShopID,
                    null,null,null,null,null,null,null,null,null,null,null,null,
                    searchQuery,
                    null,null,null,
                    false,false
            );

        }
        else
        {

            call = shopItemService.getShopItemEndpoint(
                    currentCategory.getItemCategoryID(),
                    currentShopID,
                    null,null,null,null,null,null,null,null,null,null,null,null,
                    null,
                    null,null,null,
                    false,false
            );
        }



        call.enqueue(new Callback<ShopItemEndPoint>() {
            @Override
            public void onResponse(Call<ShopItemEndPoint> call, Response<ShopItemEndPoint> response) {


                listAdapter.shopItemMap.clear();

                for(ShopItem shopItem: response.body().getResults())
                {
                    listAdapter.shopItemMap.put(shopItem.getItemID(),shopItem);
                }

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<ShopItemEndPoint> call, Throwable t) {

                showToastMessage("Network request failed. Please check your network !");
            }
        });
    }




    void addSelectedToShopClick()
    {

        if(listAdapter.selectedItems.size()==0)
        {
            showToastMessage("No item selected. Please make a selection !");

            return;
        }

        List<ShopItem> tempShopItemList = new ArrayList<>();

        for(Map.Entry<Integer,Item> entry : listAdapter.selectedItems.entrySet())
        {
//            entry.getValue().setItemCategoryID(parentCategory.getItemCategoryID());


            ShopItem shopItem = new ShopItem();
            shopItem.setShopID(UtilityShopHome.getShop(getContext()).getShopID());
            shopItem.setItemID(entry.getValue().getItemID());

            tempShopItemList.add(shopItem);
        }

        makeShopItemCreateBulkRequest(tempShopItemList);

    }


    private void makeShopItemCreateBulkRequest(List<ShopItem> tempShopItemList) {


        Call<ResponseBody> call = shopItemService.createShopItemBulk(
                UtilityLogin.getAuthorizationHeaders(getActivity()),
                tempShopItemList
        );


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200)
                {
                    showToastMessage("Update Successful !");

                    clearSelectedItems();

                }else if (response.code() == 206)
                {
                    showToastMessage("Partially Updated. Check data changes !");

                    clearSelectedItems();

                }else if(response.code() == 304)
                {

                    showToastMessage("No item updated !");

                }else
                {
                    showToastMessage("Unknown server error or response !");
                }

                makeNetworkCallShopItem();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {


                showToastMessage("Network Request failed. Check your internet / network connection !");

            }
        });

    }




    void removeSeletedShopItemClick(){



        if(listAdapter.selectedItems.size()==0)
        {
            showToastMessage("No item selected. Please make a selection !");

            return;
        }

        List<ShopItem> tempShopItemList = new ArrayList<>();

        for(Map.Entry<Integer,Item> entry : listAdapter.selectedItems.entrySet())
        {
//            entry.getValue().setItemCategoryID(parentCategory.getItemCategoryID());


            ShopItem shopItem = new ShopItem();
            shopItem.setShopID(UtilityShopHome.getShop(getContext()).getShopID());
            shopItem.setItemID(entry.getValue().getItemID());

            tempShopItemList.add(shopItem);
        }

        makeShopItemDeleteBulkRequest(tempShopItemList);
    }




    private void makeShopItemDeleteBulkRequest(List<ShopItem> tempShopItemList) {

        Call<ResponseBody> call = shopItemService.deleteShopItemBulk(
                UtilityLogin.getAuthorizationHeaders(getActivity()),
                tempShopItemList
        );


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.code() == 200)
                {
                    showToastMessage("Update Successful !");

                    clearSelectedItems();

                }else if (response.code() == 206)
                {
                    showToastMessage("Partially Updated. Check data changes !");

                    clearSelectedItems();

                }else if(response.code() == 304)
                {

                    showToastMessage("No item updated !");

                }else
                {
                    showToastMessage("Unknown server error or response !");
                }

                makeNetworkCallShopItem();

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {


                showToastMessage("Network Request failed. Check your internet / network connection !");

            }
        });

    }



    void clearSelectedItems()
    {
        // clear the selected items
        listAdapter.selectedItems.clear();
    }


    @Override
    public void removeSelectedFromShop() {
//        showToastMessage("Remove Selected");

        removeSeletedShopItemClick();
    }

    @Override
    public void addSelectedToShop() {

//        showToastMessage("Add Selected !");
        addSelectedToShopClick();
    }


}