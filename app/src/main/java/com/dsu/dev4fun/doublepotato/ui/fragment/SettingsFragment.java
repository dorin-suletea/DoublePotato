package com.dsu.dev4fun.doublepotato.ui.fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.dsu.dev4fun.doublepotato.R;
import com.dsu.dev4fun.doublepotato.model.UserPreferences;
import com.dsu.dev4fun.doublepotato.model.meta.DataBuilder;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;

public class SettingsFragment extends Fragment {
    private EditText memoryTf;
    private EditText chanelIdTf;
    private EditText appIdTf;

    private Button purgeBtn;
    private CheckBox volControlsBox;

    private ProgressBar progressBar;

    private Button saveBtn;
    private Button discardBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        memoryTf = (EditText) view.findViewById(R.id.settings_memory_idd);
        chanelIdTf = (EditText) view.findViewById(R.id.settings_channel_id);
        appIdTf = (EditText) view.findViewById(R.id.settings_app_id);
        purgeBtn = (Button) view.findViewById(R.id.delete_all_id);
        volControlsBox = (CheckBox) view.findViewById(R.id.settings_checkbox_vol_controls);
        saveBtn = (Button) view.findViewById(R.id.settings_save_id);
        discardBtn = (Button) view.findViewById(R.id.settings_discard_id);

        progressBar = (ProgressBar) view.findViewById(R.id.purge_progress);
        addListeners();
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initValues();

    }

    private void initValues() {
        memoryTf.setText(Integer.toString(UserPreferences.getInstance().getMemoryAllocation()));
        chanelIdTf.setText(UserPreferences.getInstance().getChannelId());
        appIdTf.setText(UserPreferences.getInstance().getAppId());

        boolean volControls = UserPreferences.getInstance().isVolControlsEnabled();
        volControlsBox.setChecked(volControls);
    }

    private void addListeners() {
        discardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int allocatedMemory = Integer.parseInt(memoryTf.getText().toString());
                String chanelID = chanelIdTf.getText().toString();
                String appId = appIdTf.getText().toString();
                boolean volumeControls = volControlsBox.isChecked();

                UserPreferences.getInstance().setMemoryAllocation(allocatedMemory);
                UserPreferences.getInstance().setChannelId(chanelID);
                UserPreferences.getInstance().setAppId(appId);
                UserPreferences.getInstance().setVolControlsEnabled(volumeControls);
                UserPreferences.getInstance().savePreferences();
                getFragmentManager().popBackStack();


                /** Re init the data builder with the new preferences **/
                DataBuilder.getInstance().init(chanelID, appId, SettingsFragment.this.getActivity());
            }
        });

        purgeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SettingsFragment.this.getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete all")
                        .setMessage("This action will delete all the downloaded songs and metadata about them")
                        .setPositiveButton("Delete all", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressBar.setVisibility(View.VISIBLE);
                                DataBuilder.getInstance().purgeLocalDbAndFiles();
                                MethodWrapper<Void> afterLocalDbInit = new MethodWrapper<Void>() {
                                    @Override
                                    public void execute(Void params) {
                                        progressBar.setVisibility(View.GONE);
                                        getFragmentManager().popBackStack();
                                    }
                                };
                                DataBuilder.getInstance().readLocalModel(afterLocalDbInit);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }
}
