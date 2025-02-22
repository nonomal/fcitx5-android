package org.fcitx.fcitx5.android.ui.main.settings.im

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.InputMethodEntry
import org.fcitx.fcitx5.android.daemon.launchOnFcitxReady
import org.fcitx.fcitx5.android.ui.common.BaseDynamicListUi
import org.fcitx.fcitx5.android.ui.common.DynamicListUi
import org.fcitx.fcitx5.android.ui.common.OnItemChangedListener
import org.fcitx.fcitx5.android.ui.main.settings.ProgressFragment

class InputMethodListFragment : ProgressFragment(), OnItemChangedListener<InputMethodEntry> {

    val entries: List<InputMethodEntry>
        get() = ui.entries

    private fun updateIMState() {
        if (isInitialized)
            lifecycleScope.launchOnFcitxReady(fcitx) { f ->
                f.setEnabledIme(entries.map { it.uniqueName }.toTypedArray())
            }
    }

    private lateinit var ui: BaseDynamicListUi<InputMethodEntry>

    override suspend fun initialize(): View {
        val available = fcitx.runOnReady { availableIme().toSet() }
        val initialEnabled = fcitx.runOnReady { enabledIme().toList() }
        ui = requireContext().DynamicListUi(
            mode = BaseDynamicListUi.Mode.ChooseOne {
                (available - entries.toSet()).toTypedArray()
            },
            initialEntries = initialEnabled,
            enableOrder = true,
            initSettingsButton = { entry ->
                setOnClickListener {
                    it.findNavController().navigate(
                        R.id.action_imListFragment_to_imConfigFragment,
                        bundleOf(
                            InputMethodConfigFragment.ARG_UNIQUE_NAME to entry.uniqueName,
                            InputMethodConfigFragment.ARG_NAME to entry.displayName
                        )
                    )
                }
            },
            show = { it.displayName }
        )
        ui.addOnItemChangedListener(this@InputMethodListFragment)
        // English keyboard shouldn't be removed
        ui.removable = { it.uniqueName != "keyboard-us" }
        return ui.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.disableToolbarSaveButton()
        viewModel.setToolbarTitle(requireContext().getString(R.string.input_methods_conf))
        viewModel.enableToolbarEditButton {
            ui.enterMultiSelect(
                requireActivity().onBackPressedDispatcher,
                viewModel
            )
        }
    }

    override fun onPause() {
        ui.exitMultiSelect(viewModel)
        viewModel.disableToolbarEditButton()
        super.onPause()
    }

    override fun onItemSwapped(fromIdx: Int, toIdx: Int, item: InputMethodEntry) {
        updateIMState()
    }

    override fun onItemAdded(idx: Int, item: InputMethodEntry) {
        updateIMState()
    }

    override fun onItemRemoved(idx: Int, item: InputMethodEntry) {
        updateIMState()
    }

    override fun onItemRemovedBatch(indexed: List<Pair<Int, InputMethodEntry>>) {
        batchRemove(indexed)
    }

    override fun onItemUpdated(idx: Int, old: InputMethodEntry, new: InputMethodEntry) {
        updateIMState()
    }
}