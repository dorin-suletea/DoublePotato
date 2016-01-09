package com.dsu.dev4fun.doublepotato.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.ui.BusinessLogicHelper;

public class AbstractToolbarFragment extends Fragment {

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_settings, menu);
        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                openSettingsFragment();
                return true;
            }
        });

        menu.findItem(R.id.action_exit).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //we can do that since only 1 activity
                getActivity().finish();
                return true;
            }
        });

    }

    private void openSettingsFragment() {
        BusinessLogicHelper.replaceCurrentFragment((getActivity()), new Bundle(), new SettingsFragment());
    }
}
