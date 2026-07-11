package com.mobility.enp.view.fragments.new_toll_history

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.mobility.enp.R
import com.mobility.enp.data.model.new_toll_history.mapper.toFilterDateUi
import com.mobility.enp.databinding.FragmentTollHistoryFilterBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.Util
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.new_toll_history.AllowedCountryFilterAdapter
import com.mobility.enp.view.adapters.new_toll_history.TollHistoryFilterTagAdapter
import com.mobility.enp.view.dialogs.ChangePasswordDialog
import com.mobility.enp.view.dialogs.NotificationsRequestDialog
import com.mobility.enp.view.dialogs.PermissionDeniedDialog
import com.mobility.enp.view.ui_models.toll_history.ExportType
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterEvent
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterExportState
import com.mobility.enp.view.ui_models.toll_history.TollHistoryFilterScreenUiState
import com.mobility.enp.view.ui_models.toll_history.TollHistoryTagsLoadState
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.toll_history.TollHistoryFilterViewModel
import java.util.Locale

class TollHistoryFilterFragment : Fragment() {

    private var _binding: FragmentTollHistoryFilterBinding? = null
    private val binding: FragmentTollHistoryFilterBinding get() = _binding!!

    private val viewModel: TollHistoryFilterViewModel by viewModels { TollHistoryFilterViewModel.factory }
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }

    private lateinit var tagAdapter: TollHistoryFilterTagAdapter
    private lateinit var countryAdapter: AllowedCountryFilterAdapter

    private var pendingExportType: ExportType? = null
    private var isUpdatingShowAllCheckbox = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val exportType = pendingExportType
            pendingExportType = null
            if (isGranted) {
                SharedPreferencesHelper.resetPermissionDenyCount(
                    requireContext(),
                    "notification_deny_count"
                )
                exportType?.let { onExportPermissionGranted(it) }
            } else {
                SharedPreferencesHelper.incrementPermissionDenyCount(
                    requireContext(),
                    "notification_deny_count"
                )
                val denyCount = SharedPreferencesHelper.getPermissionDenyCount(
                    requireContext(),
                    "notification_deny_count"
                )
                if (denyCount > 2) {
                    showNotificationPermissionDeniedDialog()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTollHistoryFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupClickListeners()
        observeScreenState()
        observeEvents()
        observeExportState()
        observeFranchise()
        setupNotificationPermissionResultListener()
    }

    private fun setupAdapters() {
        tagAdapter = TollHistoryFilterTagAdapter(
            onTagToggled = { serialNumber, isChecked ->
                viewModel.onTagToggled(serialNumber, isChecked)
            }
        )
        binding.rvBanksTollHistory.adapter = tagAdapter
        binding.rvBanksTollHistory.layoutManager = LinearLayoutManager(requireContext())

        countryAdapter = AllowedCountryFilterAdapter { countryCode ->
            viewModel.onCountrySelected(countryCode)
        }
        binding.rvCountryFilter.adapter = countryAdapter
        binding.rvCountryFilter.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
    }

    private fun setupClickListeners() {
        binding.chkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdatingShowAllCheckbox) return@setOnCheckedChangeListener
            viewModel.onShowAllTagsChanged(isChecked)
        }

        binding.txtDateLeft.setOnClickListener {
            showDatePicker(isFromDate = true)
        }

        binding.txtDateRight.setOnClickListener {
            showDatePicker(isFromDate = false)
        }

        binding.btnSearch.setOnClickListener {
            if (!Util.isNetworkAvailable(requireContext())) {
                showNoInternetDialog()
                return@setOnClickListener
            }
            viewModel.onSearchClicked()
        }

        binding.exportBlock.setOnClickListener {
            viewModel.onExportMenuClicked()
        }
    }

    private fun observeScreenState() {
        collectLatestLifecycleFlow(viewModel.screenState) { state ->
            renderScreenState(state)
        }
    }

    private fun renderScreenState(state: TollHistoryFilterScreenUiState) {
        when (val tagsState = state.tagsLoadState) {
            TollHistoryTagsLoadState.Loading -> {
                binding.progBar.isVisible = true
                binding.noData.isVisible = false
            }

            TollHistoryTagsLoadState.Empty -> {
                binding.progBar.isVisible = false
                binding.noData.isVisible = true
                binding.btnSearch.isEnabled = false
                tagAdapter.submitList(emptyList())
            }

            is TollHistoryTagsLoadState.Success -> {
                binding.progBar.isVisible = false
                binding.noData.isVisible = false
                binding.btnSearch.isEnabled = state.isSearchEnabled
                updateTagListHeight(tagsState.tags.size)
                tagAdapter.submitList(tagsState.tags)
            }

            is TollHistoryTagsLoadState.Error -> {
                binding.progBar.isVisible = false
                binding.noData.isVisible = false
                if (!tagsState.isNoConnection && tagsState.message.isNotEmpty()) {
                    Toast.makeText(requireContext(), tagsState.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        countryAdapter.submitList(state.countries)

        state.filter.dateFrom?.let { binding.txtDateLeft.setText(it.displayText) }
        state.filter.dateTo?.let { binding.txtDateRight.setText(it.displayText) }

        isUpdatingShowAllCheckbox = true
        binding.chkBox.isChecked = state.showAllTagsChecked
        isUpdatingShowAllCheckbox = false
    }

    private fun observeEvents() {
        collectLatestLifecycleFlow(viewModel.events) { event ->
            when (event) {
                is TollHistoryFilterEvent.NavigateToResults -> {
                    setFragmentResult(
                        FragmentResultKeys.TOLL_HISTORY_FILTER_RESULT,
                        bundleOf(
                            FragmentResultKeys.TOLL_HISTORY_FILTER_COUNTRY to event.filter.countryCode,
                            FragmentResultKeys.TOLL_HISTORY_FILTER_DATE_FROM to event.filter.apiDateFrom,
                            FragmentResultKeys.TOLL_HISTORY_FILTER_DATE_TO to event.filter.apiDateTo
                        )
                    )
                    findNavController().popBackStack()
                }

                is TollHistoryFilterEvent.ShowToast -> {
                    Toast.makeText(requireContext(), getString(event.messageRes), Toast.LENGTH_SHORT)
                        .show()
                }

                is TollHistoryFilterEvent.ShowNoInternetDialog -> showNoInternetDialog()

                TollHistoryFilterEvent.ShowExportMenu -> showExportDropdownMenu()

                is TollHistoryFilterEvent.RequestNotificationPermission -> {
                    requestExportNotificationPermission(event.exportType)
                }

                TollHistoryFilterEvent.LogoutOnInvalidToken -> {
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }
            }
        }
    }

    private fun observeExportState() {
        collectLatestLifecycleFlow(viewModel.exportState) { state ->
            when (state) {
                TollHistoryFilterExportState.Idle -> binding.progBar.isVisible = false
                TollHistoryFilterExportState.Loading -> binding.progBar.isVisible = true
                is TollHistoryFilterExportState.Error -> {
                    binding.progBar.isVisible = false
                    if (state.message.isNotEmpty()) {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    if (state.isInvalidToken) {
                        MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                    }
                    viewModel.resetExportState()
                }

                is TollHistoryFilterExportState.CsvReady,
                is TollHistoryFilterExportState.PdfReady -> {
                    binding.progBar.isVisible = false
                    viewModel.resetExportState()
                }
            }
        }
    }

    private fun observeFranchise() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.btnSearch.backgroundTintList = ColorStateList.valueOf(color)
                binding.exportBlock.setTextColor(color)
                binding.searchMark.setImageResource(franchiseModel.loopIcon)

                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                )
                val colors = intArrayOf(
                    color,
                    ContextCompat.getColor(requireContext(), R.color.primary_light_dark)
                )
                binding.chkBox.buttonTintList = ColorStateList(states, colors)
                tagAdapter.updatePrimaryColor(color)
            } ?: run {
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                )
                val colors = intArrayOf(
                    ContextCompat.getColor(requireContext(), R.color.figmaSplashScreenColor),
                    ContextCompat.getColor(requireContext(), R.color.primary_light_dark)
                )
                binding.chkBox.buttonTintList = ColorStateList(states, colors)
                tagAdapter.updatePrimaryColor(null)
            }

            binding.exportBlock.isVisible = franchiseModel == null
        }
    }

    private fun showDatePicker(isFromDate: Boolean) {
        val franchiseModel = franchiseViewModel.franchiseModel.value
        val currentSelection = if (isFromDate) {
            viewModel.screenState.value.filter.dateFrom?.epochMillis
        } else {
            viewModel.screenState.value.filter.dateTo?.epochMillis
        } ?: MaterialDatePicker.todayInUtcMilliseconds()

        val locale = when (val lang = SharedPreferencesHelper.getUserLanguage(requireContext())) {
            "cyr" -> Locale("sr", "RS")
            "sr", "cnr" -> Locale.forLanguageTag("sr-Latn-RS")
            else -> Locale(lang)
        }
        Locale.setDefault(locale)

        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
            .build()

        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .setSelection(currentSelection)
            .setCalendarConstraints(constraints)
            .setNegativeButtonText(getString(R.string.cancel))
            .setPositiveButtonText(getString(R.string.confirm))

        franchiseModel?.franchiseCalendarStyle?.let { builder.setTheme(it) }

        val datePicker = builder.build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val dateUi = selection.toFilterDateUi()
            if (isFromDate) {
                viewModel.onDateFromSelected(dateUi)
            } else {
                viewModel.onDateToSelected(dateUi)
            }
        }
        datePicker.show(parentFragmentManager, if (isFromDate) "dateFrom" else "dateTo")
    }

    private fun showExportDropdownMenu() {
        binding.exportBlock.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.popup_menu)
        )

        val context = ContextThemeWrapper(requireContext(), R.style.CustomPopupMenuStyle)
        val popupMenu = PopupMenu(context, binding.exportBlock, Gravity.END, 0, 0)
        popupMenu.menuInflater.inflate(R.menu.fitler_menu, popupMenu.menu)
        popupMenu.setForceShowIcon(true)

        popupMenu.setOnDismissListener {
            binding.exportBlock.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.transparent)
            )
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.csvDownload -> {
                    viewModel.onExportTypeSelected(ExportType.CSV)
                    true
                }

                R.id.pdfDownload -> {
                    viewModel.onExportTypeSelected(ExportType.PDF)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun requestExportNotificationPermission(exportType: ExportType) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onExportPermissionGranted(exportType)
            return
        }

        pendingExportType = exportType
        NotificationsRequestDialog.newInstance(
            getString(R.string.notification_title),
            getString(R.string.notification_subtitle)
        ).setOnButtonClickListener(object : NotificationsRequestDialog.OnButtonClick {
            override fun onClickConfirmed() {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }

            override fun onClickRejected() {
                pendingExportType = null
            }
        }).show(parentFragmentManager, "TollHistoryFilterExportPermission")
    }

    private fun onExportPermissionGranted(exportType: ExportType) {
        Toast.makeText(
            requireContext(),
            getString(R.string.permission_granted),
            Toast.LENGTH_SHORT
        ).show()
        viewModel.resetExportState()
    }

    private fun showNoInternetDialog() {
        ChangePasswordDialog.newInstance(
            title = getString(R.string.no_connection_title),
            subtitle = getString(R.string.please_connect_to_the_internet),
            resultKey = FragmentResultKeys.NO_INTERNET_RESULT,
            resultValueKey = FragmentResultKeys.NO_INTERNET_CONFIRMED
        ).show(childFragmentManager, "TollHistoryFilterNoInternet")
    }

    private fun showNotificationPermissionDeniedDialog() {
        PermissionDeniedDialog.newInstance(
            title = getString(R.string.permission_denied_message),
            subtitle = getString(R.string.notification_permission_required_message),
            resultKey = FragmentResultKeys.NOTIFICATION_PERMISSION_RESULT,
            resultValueKey = FragmentResultKeys.NOTIFICATION_PERMISSION_CONFIRMED
        ).show(parentFragmentManager, "TollHistoryFilterNotificationDenied")
    }

    private fun setupNotificationPermissionResultListener() {
        parentFragmentManager.setFragmentResultListener(
            FragmentResultKeys.NOTIFICATION_PERMISSION_RESULT,
            viewLifecycleOwner
        ) { _, bundle ->
            if (bundle.getBoolean(FragmentResultKeys.NOTIFICATION_PERMISSION_CONFIRMED, false)) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                startActivity(intent)
            }
        }
    }

    private fun updateTagListHeight(tagCount: Int) {
        if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) return

        val heightRes = when (tagCount) {
            1 -> R.dimen.recycler_view_one_items_toll
            2 -> R.dimen.recycler_view_two_items_toll
            3 -> R.dimen.recycler_view_three_items_toll
            4 -> R.dimen.recycler_view_four_items_toll
            else -> R.dimen.recycler_view_five_items_toll
        }

        binding.rvBanksTollHistory.layoutParams.height =
            resources.getDimensionPixelSize(heightRes)
        binding.rvBanksTollHistory.requestLayout()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
