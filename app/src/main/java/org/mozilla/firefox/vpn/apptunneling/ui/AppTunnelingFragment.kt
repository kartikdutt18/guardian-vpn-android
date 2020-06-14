package org.mozilla.firefox.vpn.apptunneling.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.mozilla.firefox.vpn.apptunneling.AppTunnelingComponentImpl
import org.mozilla.firefox.vpn.apptunneling.ui.AppTunnelingViewModel.UIState
import org.mozilla.firefox.vpn.databinding.FragmentAppTunnelingBinding
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.util.viewBinding
import org.mozilla.firefox.vpn.util.viewModel

class AppTunnelingFragment : Fragment() {

    private val component by lazy {
        AppTunnelingComponentImpl(activity!!.guardianComponent)
    }

    private val viewModel by viewModel {
        component.viewModel
    }

    private var binding: FragmentAppTunnelingBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAppTunnelingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiModel.observe(viewLifecycleOwner, Observer {
            if (binding.expandableList.adapter == null) {
                binding.expandableList.adapter = ExpandableAdapter(it, onExpandableItemCallback)
            } else {
                (binding.expandableList.adapter as? ExpandableAdapter)?.setData(it)
            }
        })

        viewModel.vpnState.observe(viewLifecycleOwner, Observer { vpnState ->
            when (vpnState) {
                is VpnState.Disconnected -> {
                    if (binding.switchBtn.isChecked) {
                        updateUIState(UIState.SwitchOnEnabled)
                    } else {
                        updateUIState(UIState.SwitchOffEnabled)
                    }
                }
                else -> {
                    if (binding.switchBtn.isChecked) {
                        updateUIState(UIState.SwitchOnDisabled)
                    } else {
                        updateUIState(UIState.SwitchOffDisabled)
                    }
                }
            }
        })

        viewModel.enableState.observe(viewLifecycleOwner, Observer { enableState ->
            binding.switchBtn.isEnabled = enableState
            binding.switchBtn.alpha = if (enableState) 1f else 0.5f
            (binding.expandableList.adapter as? ExpandableAdapter)?.setEnabled(enableState)
        })

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.switchBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateUIState(UIState.SwitchOnEnabled)
            } else {
                updateUIState(UIState.SwitchOffEnabled)
            }
            viewModel.switchAppTunneling(isChecked)
        }

        binding.switchBtn.isChecked = viewModel.getAppTunnelingSwitchState()
    }

    private fun updateUIState(uiState: UIState) {
        binding.infoView.infoIcon.setImageResource(uiState.infoDrawableId)
        binding.infoView.infoText.text = getString(uiState.infoTextResId)
        binding.infoView.root.isVisible =
            uiState is UIState.Warning || uiState is UIState.SwitchOffEnabled
        binding.expandableList.isVisible =
            uiState is UIState.SwitchOnEnabled || uiState is UIState.SwitchOnDisabled
    }

    private val onExpandableItemCallback = object : ExpandableAdapter.ExpandableItemCallback {

        override fun onProtectedAppChecked(packageName: String) {
            viewModel.addExcludeApp(packageName)
        }

        override fun onProtectAllClicked(packageNameSet: Set<String>) {
            viewModel.removeExcludeApp(packageNameSet)
        }

        override fun onUnprotectedAppChecked(packageName: String) {
            viewModel.removeExcludeApp(packageName)
        }

        override fun onUnprotectAllClicked(packageNameSet: Set<String>) {
            viewModel.addExcludeApp(packageNameSet)
        }
    }
}