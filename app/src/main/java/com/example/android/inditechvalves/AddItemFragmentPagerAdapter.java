package com.example.android.inditechvalves;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class AddItemFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private Context mContext;
    public AddItemFragmentPagerAdapter(Context context,@NonNull FragmentManager fm) {
        super(fm);
        mContext=context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position==0)
        {
            return new CastingFragment();
        }
        else if(position==1)
        {
            return new RolledBarFragment();
        }
        else if(position==2)
        {
            return new ForgedBarFragment();
        }
        else if(position==3) {
            return new PositionerFragment();
        }
        else if(position==4){
            return new AumaActuatorFragment();
        }
        else{
            return new SdTorkActuatorFragment();
        }
    }

    @Override
    public int getCount() {
        return 6;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        if(position==0)
        {
            return "CASTING";
        }
        else if(position==1)
        {
            return "ROLLED BAR";
        }
        else if(position==2)
        {
            return "FORGED BAR";
        }
        else if(position==3) {
            return "POSITIONER";
        }
        else if(position==4){
            return "AUMA ACTUATOR";
        }
        else{
            return "SD TORK ACTUATOR";
        }
    }
}
