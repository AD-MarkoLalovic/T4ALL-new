package com.mobility.enp.view.fragments.tool_history

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.data.model.cardsweb.CardWebModel
import com.mobility.enp.databinding.FragmentToolHistorySearchQueryBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.MyTollCountriesFilterAdapter
import com.mobility.enp.view.adapters.tool_history.select.ToolHistoryTagsAdapter
import com.mobility.enp.view.dialogs.NotificationsRequestDialog
import com.mobility.enp.view.dialogs.PermissionDeniedDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ToolHistoryFilterFragment : Fragment(), ToolHistoryTagsAdapter.TagSend,
    ToolHistoryTagsAdapter.PaginationUpdate, ToolHistoryTagsAdapter.SendToFragment {

    private var _binding: FragmentToolHistorySearchQueryBinding? = null
    private val binding: FragmentToolHistorySearchQueryBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val vModel: UserPassViewModel by activityViewModels { UserPassViewModel.Factory }
    private lateinit var statusFilterAdapter: MyTollCountriesFilterAdapter

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

        setObservers()
        setFranchiser()
        permissionNotificationDeniedDialogResultListener()

        vModel.selectedTags.clear()
        vModel.selectedCountry = ""

        binding.progBar.visibility = View.VISIBLE

        if (vModel.internetAvailable()) {
            vModel.getBaseDataAlternativeApiFilterFragment()
        } else {
            checkInternet()
        }

        binding.btnSearch.setOnClickListener {
            if (vModel.selectedTags.isEmpty() && !vModel.allTagsSelected) {
                Toast.makeText(context, R.string.please_select_tag, Toast.LENGTH_SHORT).show()
            } else if (vModel.selectedCountry.isEmpty()) {
                Toast.makeText(context, R.string.please_select_country, Toast.LENGTH_SHORT).show()
            } else {
                if (vModel.internetAvailable()) {
                    findNavController().navigate(ToolHistoryFilterFragmentDirections.actionToolHistorySearchFragmentToToolHistorySearchResultFragment())
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
            binding.progBar.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                vModel.getCsvData(requireContext())
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

    private fun triggerUpdate() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val bindingMain = (activity as MainActivity).binding
            MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)
            binding.progBar.visibility = View.GONE

            vModel.getBaseDataAlternativeApiFilterFragment()
        }
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vModel.filterList.collect { countryList ->
                    binding.progBar.visibility = View.GONE
                    updateCountriesAdapter(countryList)
                }
            }
        }

        collectLatestLifecycleFlow(vModel.baseTagDataStateFilterFragment) { tagIndex ->
            when (tagIndex) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE
                    setIndexData(tagIndex.data.first)
                    setVisibleCountries(tagIndex.data.second)
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoConnectionState()
                }

                is SubmitResult.FailureServerError -> {
                    binding.progBar.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.progBar.visibility = View.GONE
                    showError(tagIndex.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(tagIndex.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {
                    SubmitResult.Empty
                }
            }
        }

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
                                vModel.setCsvState()
                            } else {
                                showNotificationPermissionRationale(object : UserPermission {
                                    override fun onPermissionGranted() {
                                        vModel.saveBase64ToCSV(
                                            csvContent, nameExtra, requireContext()
                                        )
                                        vModel.setCsvState()
                                    }

                                    override fun onPermissionDenied() {
                                        vModel.setCsvState()
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
                }

                is SubmitResult.FailureApiError -> {
                    binding.progBar.visibility = View.GONE
                    showError(csvTable.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(csvTable.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                is SubmitResult.Empty -> {}
            }
        }


    }


    private fun checkInternet() {
        if (!vModel.internetAvailable()) {
            val bundle = Bundle().apply {
                putString(getString(R.string.title), getString(R.string.no_connection_title))
                putString(
                    getString(R.string.subtitle), getString(R.string.please_connect_to_the_internet)
                )
            }

            findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)

            val binding = (activity as MainActivity).binding
            MainActivity.showSnackMessage(getString(R.string.checking_for_connection), binding)

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                while (true) {
                    if (vModel.internetAvailable()) {
                        triggerUpdate()
                        break
                    } else {
                        delay(1000L)
                    }
                }
            }
        }
    }


    private fun showNotificationPermissionRationale(userPermission: UserPermission) {
        lifecycleScope.launch {
            val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager

            val dialog = NotificationsRequestDialog.newInstance(
                    getString(R.string.notification_title),
                    getString(R.string.notification_subtitle)
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

    private fun setIndexData(indexData: IndexData) {
        indexData.data?.let { index ->
            if (!index.tags.isNullOrEmpty()) {
                binding.noData.visibility = View.GONE

                val adapter =
                    ToolHistoryTagsAdapter(this, franchiseViewModel, this, indexData, this, this)

                binding.cycler.adapter = adapter
                binding.cycler.layoutManager = LinearLayoutManager(context)
            } else {
                binding.btnSearch.isEnabled = false
                binding.noData.visibility = View.VISIBLE
                Toast.makeText(context, R.string.no_passage_data, Toast.LENGTH_SHORT).show()
            }
        }
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
        vModel.setCsvState()
    }


    override fun onSendTag(tag: Tag) {
        vModel.selectedTags.add(tag)
        vModel.tagForExport = tag
        Log.d(TAG, "onSendTag: ${vModel.selectedTags}")
    }

    override fun onTagRemove(tag: Tag) {
        vModel.selectedTags.remove(tag)
        Log.d(TAG, "onSendTag: ${vModel.selectedTags}")
    }

    private fun setVisibleCountries(cardWebModel: CardWebModel?) {
        val countryList = ArrayList<String>()

        if (cardWebModel?.data?.showTabHR == true) {
            countryList.add(getString(R.string.croatia))
        }
        if (cardWebModel?.data?.showTabME == true) {
            countryList.add(getString(R.string.montenegro))
        }
        if (cardWebModel?.data?.showTabMK == true) {
            countryList.add(getString(R.string.macedonia))
        }
        if (cardWebModel?.data?.showTabRS == true) {
            countryList.add(getString(R.string.serbia))
        }

        vModel.setFilterList(countryList)

        statusFilterAdapter = MyTollCountriesFilterAdapter { selectedStatus ->
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

            Log.d(TAG, "selected country: $selectedCountry")

            vModel.selectedCountry = selectedCountry
        }

        binding.cyclerTagTypes.adapter = statusFilterAdapter

        statusFilterAdapter.submitList(countryList.reversed()) {
            statusFilterAdapter.setTabPosition(-1)
        }
    }

    private fun updateCountriesAdapter(countryList: List<String>) {
        statusFilterAdapter = MyTollCountriesFilterAdapter { selectedStatus ->
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

            Log.d(TAG, "selected country: $selectedCountry")

            vModel.selectedCountry = selectedCountry
        }

        binding.cyclerTagTypes.adapter = statusFilterAdapter

        statusFilterAdapter.submitList(countryList.reversed()) {
            statusFilterAdapter.setTabPosition(-1)
        }
    }

    override fun sendDataFillFilterAdapter(
        // updates tags on main adapter
        nextPage: Int,
        perPage: Int,
        flow: MutableStateFlow<SubmitResult<IndexData>>,
    ) {
        binding.progBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            vModel.getBaseTagDataPagination(nextPage, perPage, flow)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun startSpinner() {
        binding.progBar.visibility = View.VISIBLE
    }

    override fun stopSpinner() {
        binding.progBar.visibility = View.GONE
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

    interface UserPermission {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

}