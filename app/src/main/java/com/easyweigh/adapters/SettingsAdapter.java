package com.easyweigh.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyweigh.R;
import com.easyweigh.app.AppConst;
import com.easyweigh.data.SettingsItem;
import com.easyweigh.preferences.UserPreference;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Ben Cherif on 25/06/2015.
 */
public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 2;
    private static final int TYPE_ITEM = 1;
    public LayoutInflater inflater;
    private Activity mActivity;
    List<SettingsItem> settingsItems = Collections.emptyList();

    public SettingsAdapter(Activity mActivity, List<SettingsItem> settingsItems) {
        this.mActivity = mActivity;
        inflater = LayoutInflater.from(mActivity);
        this.settingsItems = settingsItems;

    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {

            View view = inflater.inflate(R.layout.ads_header, parent, false);
            HeaderViewHolder holder = new HeaderViewHolder(view);
            return holder;
        } else {
            View view = inflater.inflate(R.layout.settings_item, parent, false);
            ItemHolder holder = new ItemHolder(view);
            return holder;
        }
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ItemHolder itemHolder = (ItemHolder) holder;
            SettingsItem settingsItem = settingsItems.get(position - 1);
            itemHolder.title.setText(settingsItem.title);
            itemHolder.icon.setImageResource(settingsItem.iconId);
        } else if (holder instanceof HeaderViewHolder) {

            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            if (AppConst.SHOW_ADS) {
                if(UserPreference.adsVisibility(mActivity)) {
                    headerHolder.mAdView.setVisibility(View.VISIBLE);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    headerHolder.mAdView.loadAd(adRequest);
                }
            } else {
                headerHolder.mAdView.setVisibility(View.GONE);
            }


        }
    }

    @Override
    public int getItemCount() {

        if (settingsItems != null) {
            return settingsItems.size() + 1;
        } else {
            return 1;
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;

        public ItemHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.listText);
            icon = (ImageView) itemView.findViewById(R.id.listIcon);

        }
    }
    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public AdView mAdView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            mAdView = (AdView) itemView.findViewById(R.id.adView);
        }
    }
}
