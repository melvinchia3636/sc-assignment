package com.groupone.assignment.ui.green;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.groupone.assignment.R;
import com.groupone.assignment.databinding.TabGreenSpaceBinding;
import com.groupone.assignment.ui.MainViewModel;
import com.groupone.assignment.ui.common.BaseTab;

/**
 * Green-space screen placeholder. Live green space data is not part of the
 * scraper API yet.
 */
public class GreenSpacesTab extends BaseTab<TabGreenSpaceBinding> {

    private MainViewModel viewModel;

    @Override
    protected TabGreenSpaceBinding createBinding(LayoutInflater inflater,
                                                      ViewGroup container) {
        return TabGreenSpaceBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = sharedViewModel();

        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
        binding.greenPlaceholder.setText(R.string.empty_green_spaces);

        observeStation(snapshot -> binding.swipeRefresh.setRefreshing(false));
        observeError(message -> {
            if (message != null) {
                binding.greenPlaceholder.setText(message);
            }
        });
        observeLoading(binding.swipeRefresh);
    }
}
