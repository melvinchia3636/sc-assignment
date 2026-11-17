package com.groupone.assignment.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import com.groupone.assignment.repositories.model.Snapshot;
import com.groupone.assignment.ui.MainViewModel;

/**
 * Base for bottom-bar tabs: owns the ViewBinding lifecycle and exposes the
 * shared {@link MainViewModel} observers.
 */
public abstract class BaseTab<VB extends ViewBinding> extends Fragment {

    protected VB binding;

    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = createBinding(inflater, container);
        return binding.getRoot();
    }

    protected abstract VB createBinding(LayoutInflater inflater,
                                        @Nullable ViewGroup container);

    protected MainViewModel sharedViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        }
        return viewModel;
    }

    protected void observeSnapshot(Observer<Snapshot> observer) {
        sharedViewModel().getSnapshot().observe(getViewLifecycleOwner(), observer);
    }

    protected void observeError(Observer<String> observer) {
        sharedViewModel().getError().observe(getViewLifecycleOwner(), observer);
    }

    protected void observeLoading(SwipeRefreshLayout swipeRefresh) {
        sharedViewModel().getLoading().observe(getViewLifecycleOwner(),
                loading -> swipeRefresh.setRefreshing(Boolean.TRUE.equals(loading)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
