package com.mobility.enp.view.fragments.tool_history

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.databinding.FragmentToolHistorySearchQueryBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.MyTollCountriesFilterAdapter
import com.mobility.enp.view.adapters.tool_history.filter.ToolHistoryFilterFragmentSerialAdapter
import com.mobility.enp.view.dialogs.NotificationsRequestDialog
import com.mobility.enp.view.dialogs.PermissionDeniedDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class HistoryFilterScreen : Fragment() {

    private var _binding: FragmentToolHistorySearchQueryBinding? = null
    private val binding: FragmentToolHistorySearchQueryBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val vModel: UserPassViewModel by activityViewModels { UserPassViewModel.Factory }
    private lateinit var statusFilterAdapter: MyTollCountriesFilterAdapter
    private lateinit var serialAdapter: ToolHistoryFilterFragmentSerialAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is granted. You can proceed with sending notifications.
                SharedPreferencesHelper.resetPermissionDenyCount(
                    requireContext(), "notification_deny_count"
                )
                userPerm.onPermissionGranted()
                sendNotification()
            } else {
                SharedPreferencesHelper.incrementPermissionDenyCount(
                    requireContext(), "notification_deny_count"
                )

                val denyCount = SharedPreferencesHelper.getPermissionDenyCount(
                    requireContext(), "notification_deny_count"
                )
                if (denyCount > 2) {
                    notificationPermissionDeniedDialog()
                }
            }
        }

    private lateinit var userPerm: UserPermission

    companion object {
        const val TAG = "ToolDetails"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_tool_history_search_query, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initSerialAdapter()
        observeStoredData()
        setObservers()
        setFranchiser()
        permissionNotificationDeniedDialogResultListener()

        vModel.selectedTags.clear()
        vModel.selectedCountry = ""
        vModel.deleteOldResultData()

        binding.progBar.visibility = View.VISIBLE

        binding.btnSearch.setOnClickListener {
            if (vModel.selectedTags.isEmpty() && !vModel.allTagsSelected) {
                Toast.makeText(context, R.string.please_select_tag, Toast.LENGTH_SHORT).show()
            } else if (vModel.selectedCountry.isEmpty()) {
                Toast.makeText(context, R.string.please_select_country, Toast.LENGTH_SHORT).show()
            } else {
                if (vModel.internetAvailable()) {
                    findNavController().navigate(HistoryFilterScreenDirections.actionToolHistorySearchFragmentToToolHistorySearchResultFragment())
                } else {
                    val bundle = Bundle().apply {
                        putString(
                            getString(R.string.title), getString(R.string.no_connection_title)
                        )
                        putString(
                            getString(R.string.subtitle),
                            getString(R.string.please_connect_to_the_internet)
                        )
                    }

                    findNavController().navigate(
                        R.id.action_global_noInternetConnectionDialog, bundle
                    )
                }
            }
        }

        binding.chkBox.setOnClickListener {
            val isChecked = binding.chkBox.isChecked
            vModel.allTagsSelected = isChecked
        }

        vModel.startDate.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val textView = binding.txtDateLeft as TextView
                textView.text = data.formattedTime
            }
        }

        vModel.endDate.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val textEndDate = binding.txtDateRight as TextView
                textEndDate.text = data.formattedTime
            }
        }

        binding.txtDateLeft.setOnClickListener {
            vModel.showDatePicker(true, requireContext(), franchiseViewModel.franchiseModel.value)
        }

        binding.txtDateRight.setOnClickListener {
            vModel.showDatePicker(false, requireContext(), franchiseViewModel.franchiseModel.value)
        }

        binding.exportBlock.setOnClickListener {
            setDropdownMenu(it)
        }
    }

    private fun initSerialAdapter() {
        serialAdapter =
            ToolHistoryFilterFragmentSerialAdapter(
                franchiseViewModel,
                this,
                vModel
            )

        binding.cycler.adapter = serialAdapter
        binding.cycler.layoutManager = LinearLayoutManager(context)
    }

    private fun observeStoredData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vModel.allowedCountriesFlow.collect { allowedCountries ->
                    val listOfCountries: ArrayList<String> = arrayListOf()

                    for (data in allowedCountries) {
                        listOfCountries.add(data.country)
                    }

                    if (listOfCountries.isNotEmpty()) {
                        updateCountriesAdapter(listOfCountries)

                        statusFilterAdapter.performClick(vModel.availableCountryAdapterPositionFilter.value)
                    }

                    binding.progBar.visibility = View.GONE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vModel.tagFlow.collect { indexData ->
                    if (!indexData.isEmpty()) {
                        binding.progBar.visibility = View.GONE

                        checkNoPassage(indexData[0])

                        updateIndexAdapter(indexData)
                    }
                }
            }
        }
    }

    private fun checkNoPassage(indexData: IndexData) {
        indexData.data?.let { index ->
            if (index.tags.isNullOrEmpty()) {
                binding.btnSearch.isEnabled = false
                binding.noData.visibility = View.VISIBLE
                Toast.makeText(context, R.string.no_passage_data, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.btnSearch.backgroundTintList = ColorStateList.valueOf(color)
                binding.exportBlock.setTextColor(color)
                binding.exportBlock.visibility =
                    View.GONE // because fraternizers can not export from what daniel told me

                binding.searchMark.setImageResource(franchiseModel.loopIcon)

                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),  // When switch is ON
                    intArrayOf(-android.R.attr.state_checked) // When switch is OFF
                )

                val colors = intArrayOf(
                    color,  // ON color
                    ContextCompat.getColor(
                        requireContext(), R.color.primary_light_dark
                    ) // OFF color
                )

                val colorStateList = ColorStateList(states, colors)
                binding.chkBox.buttonTintList = colorStateList
            } ?: run {
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),  // When switch is ON
                    intArrayOf(-android.R.attr.state_checked) // When switch is OFF
                )

                val colors = intArrayOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.figmaSplashScreenColor
                    ),  // ON color
                    ContextCompat.getColor(
                        requireContext(), R.color.primary_light_dark
                    ) // OFF color
                )

                val colorStateList = ColorStateList(states, colors)
                binding.chkBox.buttonTintList = colorStateList
            }
        }
    }

    private fun setObservers() {
        collectLatestLifecycleFlow(vModel.csvTable) { csvTable ->
            when (csvTable) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE

                    csvTable.data.data?.csvContent?.takeIf { it.isNotEmpty() }?.let { csvContent ->
                        val nameExtra = UUID.randomUUID().toString().take(8)
                        vModel.processCsvData(csvTable.data, nameExtra, requireContext())

                        if (ContextCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            vModel.saveBase64ToCSV(csvContent, nameExtra, requireContext())
                            vModel.nullFlowState()
                        } else {
                            showNotificationPermissionRationale(object : UserPermission {
                                override fun onPermissionGranted() {
                                    vModel.saveBase64ToCSV(
                                        csvContent, nameExtra, requireContext()
                                    )
                                    vModel.nullFlowState()
                                }

                                override fun onPermissionDenied() {
                                    vModel.nullFlowState()
                                }
                            })
                        }
                    } ?: run {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_passage_data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoConnectionState()
                }

                is SubmitResult.FailureServerError -> {
                    binding.progBar.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                    vModel.nullFlowState()
                }

                is SubmitResult.FailureApiError -> {
                    binding.progBar.visibility = View.GONE
                    showError(csvTable.errorMessage)
                    vModel.nullFlowState()
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(csvTable.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                is SubmitResult.Empty -> {}
            }
        }

        collectLatestLifecycleFlow(vModel.pdfTable) { data ->
            when (data) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE

                    Log.d(TAG, "api response $data ")
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoConnectionState()
                }

                is SubmitResult.FailureServerError -> {
                    binding.progBar.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                    vModel.nullFlowState()
                }

                is SubmitResult.FailureApiError -> {
                    binding.progBar.visibility = View.GONE
                    showError(data.errorMessage)
                    vModel.nullFlowState()
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(data.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                is SubmitResult.Empty -> {}
            }
        }
    }

    private fun showNotificationPermissionRationale(userPermission: UserPermission) {
        lifecycleScope.launch {
            val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager

            val dialog = NotificationsRequestDialog.newInstance(
                getString(R.string.notification_title), getString(R.string.notification_subtitle)
            ).setOnButtonClickListener(object : NotificationsRequestDialog.OnButtonClick {
                override fun onClickConfirmed() {
                    userPerm = userPermission
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }

                override fun onClickRejected() {
                    userPermission.onPermissionDenied()
                }
            })

            dialog.show(fragmentManager, "permDialog")
        }
    }

    private fun sendNotification() {
        Toast.makeText(requireContext(), getString(R.string.permission_granted), Toast.LENGTH_SHORT)
            .show()
    }

    private fun updateIndexAdapter(indexData: List<IndexData>) {
        binding.noData.visibility = View.GONE

        val orientation = resources.configuration.orientation

        when (orientation) {

            Configuration.ORIENTATION_LANDSCAPE -> {

                val heightInDp = when (indexData[0].data?.tags?.size ?: 200) {

                    1 -> binding.root.context.resources.getDimensionPixelSize(
                        R.dimen.recycler_view_one_items_toll
                    )

                    2 -> binding.root.context.resources.getDimensionPixelSize(
                        R.dimen.recycler_view_two_items_toll
                    )

                    3 -> binding.root.context.resources.getDimensionPixelSize(
                        R.dimen.recycler_view_three_items_toll
                    )

                    4 -> binding.root.context.resources.getDimensionPixelSize(
                        R.dimen.recycler_view_four_items_toll
                    )

                    5 -> binding.root.context.resources.getDimensionPixelSize(
                        R.dimen.recycler_view_five_items_toll
                    )

                    else -> binding.root.context.resources.getDimensionPixelSize(
                        R.dimen.recycler_view_five_items_toll
                    )
                }

                binding.cycler.layoutParams.height = heightInDp
                binding.cycler.requestLayout()

            }

            else -> {}
        }

        serialAdapter.setAdapterData(indexData)
    }

    private fun showNoConnectionState() {
        binding.progBar.visibility = View.GONE
        noInternetMessage()
    }

    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        vModel.nullFlowState()
    }

    private fun updateCountriesAdapter(countryList: List<String>) {
        statusFilterAdapter = MyTollCountriesFilterAdapter { selectedStatus ->

            vModel.setCountryAdapterPositionFilter(statusFilterAdapter.getTabPosition())

            val selectedCountry = when (selectedStatus) {
                getString(R.string.croatia) -> {
                    getString(R.string.croatia_hr)
                }

                getString(R.string.montenegro) -> {
                    getString(R.string.montenegro_me)
                }

                getString(R.string.macedonia) -> {
                    getString(R.string.northmacedonia_mk)
                }

                getString(R.string.serbia) -> {
                    getString(R.string.serbia_rs)
                }

                else -> ""
            }

            vModel.selectedCountry = selectedCountry
        }

        binding.cyclerTagTypes.adapter = statusFilterAdapter

        statusFilterAdapter.submitList(countryList.reversed()) {
            statusFilterAdapter.setTabPosition(-1)  // set initially to negative to force country selection
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun notificationPermissionDeniedDialog() {
        PermissionDeniedDialog.newInstance(
            title = requireContext().getString(R.string.permission_denied_message),
            subtitle = requireContext().getString(R.string.notification_permission_required_message),
            resultKey = FragmentResultKeys.NOTIFICATION_PERMISSION_RESULT,
            resultValueKey = FragmentResultKeys.NOTIFICATION_PERMISSION_CONFIRMED
        ).show(parentFragmentManager, "NotificationPermissionDeniedDialog")
    }

    private fun permissionNotificationDeniedDialogResultListener() {
        parentFragmentManager.setFragmentResultListener(
            FragmentResultKeys.NOTIFICATION_PERMISSION_RESULT, viewLifecycleOwner
        ) { _, bundle ->
            val clickSettings =
                bundle.getBoolean(FragmentResultKeys.NOTIFICATION_PERMISSION_CONFIRMED, false)
            if (clickSettings) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireContext().packageName, null)
                startActivity(intent)
            }
        }
    }

    private fun setDropdownMenu(view: View) {
        binding.exportBlock.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                binding.root.context,
                R.color.popup_menu
            )
        )

        val context = ContextThemeWrapper(view.context, R.style.CustomPopupMenuStyle)
        val popupMenu = PopupMenu(context, view, Gravity.END, 0, 0)
        popupMenu.menuInflater.inflate(R.menu.fitler_menu, popupMenu.menu)
        popupMenu.setForceShowIcon(true)

        popupMenu.setOnDismissListener {
            binding.exportBlock.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.transparent
                )
            )
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val isCSV = menuItem.itemId == R.id.csvDownload
            val isPdf = menuItem.itemId == R.id.pdfDownload

            if (isCSV) {
                binding.progBar.visibility = View.VISIBLE
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    vModel.getCsvData(requireContext())
                }
                true
            } else if (isPdf) {
                binding.progBar.visibility = View.VISIBLE
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    vModel.getPDFData(requireContext())
                }
                true
            } else {
                false
            }
        }

        popupMenu.show()
    }

    interface UserPermission {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

}