package com.dsu.dev4fun.doublepotato.ui;

import com.dsu.dev4fun.doublepotato.R;

public enum ButtonIcons {
    DOWNLOAD_BUTTON(R.drawable.download_icon, R.drawable.download_icon_disabled),
    SYNC_BUTTON(R.drawable.sync_action,R.drawable.sync_action_disabled);

    private final int enabledIcon;
    private final int disabledIcon;

    private ButtonIcons(int enabledIcon, int disabledIcon) {
        this.enabledIcon = enabledIcon;
        this.disabledIcon = disabledIcon;
    }

    public int getDisabledIcon() {
        return disabledIcon;
    }

    public int getEnabledIcon() {
        return enabledIcon;
    }
}
