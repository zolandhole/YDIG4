package com.surampaksakosoy.ydig4.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.surampaksakosoy.ydig4.fragments.ProfileFragment;
import com.surampaksakosoy.ydig4.fragments.StreamingFragment;
import com.surampaksakosoy.ydig4.fragments.TentangKami;

public class pagerAdapter extends FragmentStatePagerAdapter {

    private int nomortab;

    public pagerAdapter(FragmentManager fm, int nomortab){
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.nomortab = nomortab;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new StreamingFragment();
            case 1:
                return new TentangKami();
            case 2:
                return new ProfileFragment();
                default:
                    return null;
        }
    }

    @Override
    public int getCount() {
        return nomortab;
    }
}
