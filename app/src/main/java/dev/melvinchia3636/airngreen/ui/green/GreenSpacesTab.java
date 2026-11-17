package dev.melvinchia3636.airngreen.ui.green;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.melvinchia3636.airngreen.R;
import dev.melvinchia3636.airngreen.databinding.TabGreenSpaceBinding;
import dev.melvinchia3636.airngreen.ui.MainViewModel;
import dev.melvinchia3636.airngreen.ui.common.BaseTab;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;

/**
 * Green-space screen placeholder. Live green space data is not part of the
 * scraper API yet.
 */
public class GreenSpacesTab extends BaseTab<TabGreenSpaceBinding> {

    public static final GoogleMaterial.Icon NAV_ICON = GoogleMaterial.Icon.gmd_park;

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

        observeError(message -> {
            if (message != null) {
                binding.greenPlaceholder.setText(message);
            }
        });
        observeLoading(binding.swipeRefresh);
    }
}
