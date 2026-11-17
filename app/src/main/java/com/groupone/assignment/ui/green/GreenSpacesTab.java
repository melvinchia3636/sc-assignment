package com.groupone.assignment.ui.green;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.groupone.assignment.R;
import com.groupone.assignment.data.GreenSpace;
import com.groupone.assignment.data.Snapshot;
import com.groupone.assignment.databinding.FragmentGreenSpaceBinding;
import com.groupone.assignment.ui.MainViewModel;
import com.groupone.assignment.ui.common.BaseTab;

import java.util.List;

/**
 * Green-space screen: nearby parks and gardens.
 */
public class GreenSpacesTab extends BaseTab<FragmentGreenSpaceBinding> {

    private MainViewModel viewModel;
    private GreenSpaceAdapter adapter;

    @Override
    protected FragmentGreenSpaceBinding createBinding(LayoutInflater inflater,
                                                      ViewGroup container) {
        return FragmentGreenSpaceBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = sharedViewModel();

        adapter = new GreenSpaceAdapter();
        binding.greenList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.greenList.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
        observeSnapshot(this::render);
        observeError(message -> {
            if (message != null) {
                binding.greenCount.setText("");
                binding.greenEmpty.setVisibility(View.VISIBLE);
                binding.greenEmpty.setText(message);
            }
        });
        observeLoading(binding.swipeRefresh);
    }

    private void render(Snapshot snapshot) {
        List<GreenSpace> greenSpaces = snapshot.getGreenSpaces();
        adapter.submitList(greenSpaces);
        binding.greenCount.setText(greenSpaces.isEmpty()
                ? "" : getString(R.string.green_found, greenSpaces.size()));
        binding.greenEmpty.setVisibility(greenSpaces.isEmpty() ? View.VISIBLE : View.GONE);
        binding.greenEmpty.setText(snapshot.isGreenSpacesUnavailable()
                ? getString(R.string.green_unavailable)
                : getString(R.string.empty_green_spaces));
    }
}
