package com.easyweigh.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easyweigh.R;
import com.easyweigh.data.DBHelper;

/**
 * Created by Abderrahim on 10/14/2015.
 */
public class TabsFragment extends Fragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;
    public  int nb_items =4;
    DBHelper dbhelper;
    SharedPreferences prefs;
    String fromProd;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbhelper = new DBHelper(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        View view = inflater.inflate(R.layout.tabs_layout, null);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new TabsAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(2); // if you use 3 tabs
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
                tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                tabLayout.setTabMode(TabLayout.MODE_FIXED);
                setupTabIcons();
            }
        });
        return view;
    }

    /**
     * method to setup tabs icons
     */
    private void setupTabIcons() {

        tabLayout.getTabAt(0).setText("Home");
        tabLayout.getTabAt(1).setText("Buy");
        tabLayout.getTabAt(2).setText("Delivery");
        tabLayout.getTabAt(3).setText("Reports");

    }



    class TabsAdapter extends FragmentPagerAdapter {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomePageFragment();

                case 1:

              fromProd = prefs.getString("FromProd", "");
                if(fromProd.equals("Agents")){
                    return new AgentProduceFragment();
                }else
                {
                    return new ProduceVGPFragment();
                }

                case 2:
                    return new DelivaryFragment();
                case 3:

                    if(fromProd.equals("Agents")){
                        return new AgentsReportsFragment();
                    }else
                    {
                        return new ReportsFragment();
                    }


            }
            return null;
        }

        @Override
        public int getCount() {
            return nb_items;

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

}