package com.dsu.dev4fun.doublepotato.ui.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.View;

public abstract class AbstractServiceFragment extends AbstractToolbarFragment {
    protected BroadcastReceiver mIntentReceiver;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        addServiceReceiver();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        if (mIntentReceiver!=null) {
            getActivity().unregisterReceiver(mIntentReceiver);
        }
        super.onDestroyView();
    }

    public abstract void addServiceReceiver();
}
