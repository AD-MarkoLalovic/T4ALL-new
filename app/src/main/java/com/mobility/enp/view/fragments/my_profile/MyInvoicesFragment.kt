package com.mobility.enp.view.fragments.my_profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.data.model.api_my_invoices.refactor.Data
import com.mobility.enp.data.model.api_my_invoices.refactor.MyInvoicesResponse
import com.mobility.enp.databinding.FragmentBillsBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.NetworkObserver
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.util.Util
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.toast
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.my_invoices_adapters.BillsDetailsAdapter
import com.mobility.enp.view.adapters.my_invoices_adapters.MonthlyBillsAdapter
import com.mobility.enp.view.adapters.my_invoices_adapters.MyInvoicesCountriesAdapter
import com.mobility.enp.view.dialogs.NotificationsRequestDialog
import com.mobility.enp.view.dialogs.PermissionDeniedDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.MyInvoicesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MyInvoicesFragment : Fragment(), MonthlyBillsAdapter.TriggerSpinner,
    MonthlyBillsAdapter.MontYearListener {

    private var _binding: FragmentBillsBinding? = null
    private val binding: FragmentBillsBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: MyInvoicesViewModel by activityViewModels { MyInvoicesViewModel.Factory }

    private lateinit var adapterCountries: MyInvoicesCountriesAdapter
    private lateinit var adapterMonthly: MonthlyBillsAdapter
    private lateinit var networkObserver: NetworkObserver
    private var dataExists: Data? = null

    private var month = ""
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is granted. You can proceed with sending notifications.
                SharedPreferencesHelper.resetPermissionDenyCount(
                    requireContext(),
                    "notification_deny_count"
                )
                userPerm.onPermissionGranted()
                sendNotification()
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
                    notificationPermissionDeniedDialog()
                }
            }
        }

    private lateinit var userPerm: BillsDetailsAdapter.UserPermission

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.firstLoad) {
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.firstLoad = true
        }

        setObservers()

        setupNetworkObserver()
        permissionNotificationDeniedDialogResultListener()

        if (!Util.isNetworkAvailable(requireContext())) {
            binding.txNoInternet.visibility = View.VISIBLE
            showNoConnectionState()
        }
    }

    private fun setObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allowedCountriesFlow.collect { finalList ->
                    binding.valueTitle.visibility = View.VISIBLE
                    adapterCountries = MyInvoicesCountriesAdapter { selectedStatus ->

                        when (selectedStatus) {

                            getString(R.string.all) -> {
                                binding.textNoBills.visibility = View.GONE
                                if (::adapterMonthly.isInitialized) {
                                    adapterMonthly.resetAdapter()
                                }
                                binding.invoicesLoadingView.visibility = View.VISIBLE
                                viewModel.setSelectedCountry("all")
                                viewModel.setPosition(adapterCountries.getTabPosition())
                                viewModel.recyclerState = null
                                viewModel.fetchMonthlyInvoices()
                            }

                            getString(R.string.croatia) -> {
                                binding.textNoBills.visibility = View.GONE
                                if (::adapterMonthly.isInitialized) {
                                    adapterMonthly.resetAdapter()
                                }
                                binding.invoicesLoadingView.visibility = View.VISIBLE
                                viewModel.setSelectedCountry("HR")
                                viewModel.setPosition(adapterCountries.getTabPosition())
                                viewModel.recyclerState = null
                                viewModel.fetchMonthlyInvoices()
                            }

                            getString(R.string.montenegro) -> {
                                binding.textNoBills.visibility = View.GONE
                                if (::adapterMonthly.isInitialized) {
                                    adapterMonthly.resetAdapter()
                                }
                                binding.invoicesLoadingView.visibility = View.VISIBLE
                                viewModel.setSelectedCountry("ME")
                                viewModel.setPosition(adapterCountries.getTabPosition())
                                viewModel.recyclerState = null
                                viewModel.fetchMonthlyInvoices()
                            }

                            getString(R.string.north_macedonian_passage) -> {
                                binding.textNoBills.visibility = View.GONE
                                if (::adapterMonthly.isInitialized) {
                                    adapterMonthly.resetAdapter()
                                }
                                binding.invoicesLoadingView.visibility = View.VISIBLE
                                viewModel.setSelectedCountry("MK")
                                viewModel.setPosition(adapterCountries.getTabPosition())
                                viewModel.recyclerState = null
                                viewModel.fetchMonthlyInvoices()
                            }

                            getString(R.string.serbia) -> {
                                binding.textNoBills.visibility = View.GONE
                                if (::adapterMonthly.isInitialized) {
                                    adapterMonthly.resetAdapter()
                                }
                                binding.invoicesLoadingView.visibility = View.VISIBLE
                                viewModel.setSelectedCountry("RS")
                                viewModel.setPosition(adapterCountries.getTabPosition())
                                viewModel.recyclerState = null
                                viewModel.fetchMonthlyInvoices()
                            }

                            else -> ""
                        }

                    }

                    binding.cyclerInvoicesCountries.adapter = adapterCountries

                    adapterCountries.submitList(finalList) {
                        adapterCountries.setTabPosition(viewModel.getPosition())
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentData.collect { response ->
                    when (response) {
                        is SubmitResult.Success -> {
                            if (response.data.data!!.months.isEmpty()) {
                                binding.textNoBills.visibility = View.VISIBLE
                            } else {
                                dataExists = response.data.data

                                binding.textNoBills.visibility = View.GONE
                                binding.recyclerViewBills.visibility = View.VISIBLE
                                adapterMonthly = MonthlyBillsAdapter(
                                    response.data.data,
                                    viewModel,
                                    this@MyInvoicesFragment,
                                    viewLifecycleOwner,
                                    this@MyInvoicesFragment,
                                    franchiseViewModel.franchiseModel.value
                                ) {
                                    val state =
                                        binding.recyclerViewBills.layoutManager?.onSaveInstanceState()
                                    viewModel.recyclerState = state
                                }
                                binding.recyclerViewBills.adapter = adapterMonthly
                                binding.recyclerViewBills.layoutManager =
                                    LinearLayoutManager(requireContext())
                                binding.recyclerViewBills.layoutManager
                                    ?.onRestoreInstanceState(viewModel.recyclerState)
                            }
                        }

                        else -> {}
                    }
                }
            }
        }

        collectLatestLifecycleFlow(viewModel.myInvoices) { serverResponse ->
            when (serverResponse) {
                is SubmitResult.Loading -> {
                    binding.invoicesLoadingView.visibility = View.VISIBLE
                    binding.recyclerViewBills.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.invoicesLoadingView.visibility = View.GONE
                    binding.recyclerViewBills.visibility = View.VISIBLE
                    binding.txNoInternet.visibility = View.GONE

                    loadData(serverResponse)
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoConnectionState()
                }

                is SubmitResult.FailureServerError -> {
                    binding.invoicesLoadingView.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.invoicesLoadingView.visibility = View.GONE
                    showError(serverResponse.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(serverResponse.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {}
            }
        }

        collectLatestLifecycleFlow(viewModel.billPad) { result ->
            when (result) {
                is SubmitResultFold.Failure -> {
                    binding.invoicesLoadingView.visibility = View.GONE
                    handleError(result.error)
                    viewModel.resetBillPad()
                }

                SubmitResultFold.Idle -> {}
                SubmitResultFold.Loading -> {
                    binding.invoicesLoadingView.visibility = View.VISIBLE
                }

                is SubmitResultFold.Success<*> -> {
                    binding.invoicesLoadingView.visibility = View.GONE

                    toast(getString(R.string.payment_successfully))
                    viewModel.fetchMonthlyInvoices()
                    viewModel.resetBillPad()
                }
            }
        }

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
            FragmentResultKeys.NOTIFICATION_PERMISSION_RESULT,
            viewLifecycleOwner
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

    private fun loadData(response: SubmitResult.Success<MyInvoicesResponse>) {

        response.data.data?.allowedCountries?.let { allowedCountry ->
            binding.valueTitle.visibility = View.VISIBLE

            if (::adapterCountries.isInitialized) {  // perform adapter setting only once if its not already intialized

                val listOfCountries: ArrayList<String> = arrayListOf()

                listOfCountries.add(getString(R.string.all))  // because we have all "button"

                for (data in allowedCountry) {
                    when (data.value) {
                        getString(R.string.croatia_hr) -> {
                            listOfCountries.add(getString(R.string.croatia))
                        }

                        getString(R.string.montenegro_me) -> {
                            listOfCountries.add(getString(R.string.montenegro))
                        }

                        getString(R.string.northmacedonia_mk) -> {
                            listOfCountries.add(getString(R.string.north_macedonian_passage))
                        }

                        getString(R.string.serbia_rs) -> {
                            listOfCountries.add(getString(R.string.serbia))
                        }
                    }
                }

                val finalList: List<String> = listOfCountries.distinct()
                listOfCountries.clear()

                viewModel.setAllowedCountries(finalList)
                viewModel.setSavedData(response)
            }

        }
    }

    private fun handleError(error: Throwable) {
        when (error) {
            NetworkError.ServerError -> {
                toast(getString(R.string.server_error_msg))
            }

            NetworkError.NoConnection -> {
                noInternetMessage()
            }

            is NetworkError.ApiError -> {
                toast(error.errorResponse.message ?: getString(R.string.server_error_msg))
            }
        }
    }

    private fun sendNotification() {
        Toast.makeText(requireContext(), getString(R.string.permission_granted), Toast.LENGTH_SHORT)
            .show()
    }

    override fun onStartSpinner() {
        _binding?.let {
            it.invoicesLoadingView.visibility = View.VISIBLE
        }
    }

    override fun onStopSpinner() {
        _binding?.let {
            it.invoicesLoadingView.visibility = View.GONE
        }
    }


    override fun pagingUpdate(
        nextPage: Int,
        flow: MutableStateFlow<SubmitResult<MyInvoicesResponse>>
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewModel.fetchMonthlyInvoicesPaging(
                nextPage, flow
            )
        }
    }

    override fun pagingUpdateBill(
        nextPage: Int,
        flow: MutableStateFlow<SubmitResult<BillsDetailsResponse>>,
        availableCurrencies: String
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewModel.fetchBillDetailsNewPaging(
                flow,
                month,
                availableCurrencies,
                nextPage
            )
        }
    }

    override fun requestNotificationFromUser(userPermission: BillsDetailsAdapter.UserPermission) {
        lifecycleScope.launch {
            val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager

            val dialog = NotificationsRequestDialog
                .newInstance(
                    getString(R.string.notification_title),
                    getString(R.string.notification_subtitle)
                )
                .setOnButtonClickListener(object : NotificationsRequestDialog.OnButtonClick {
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


    private fun showNoConnectionState() {
        noInternetMessage()
    }

    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onMontYearSelected(montYear: String) {
        month = montYear
    }

    private fun setupNetworkObserver() {
        networkObserver = NetworkObserver(
            requireContext(),
            onAvailable = {
                viewModel.fetchMonthlyInvoices()
            },
            onLost = {
                viewLifecycleOwner.lifecycleScope.launch {
                    noInternetMessage()
                    if (dataExists != null) {
                        binding.recyclerViewBills.visibility = View.VISIBLE
                        binding.txNoInternet.visibility = View.GONE
                        binding.valueTitle.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewBills.visibility = View.GONE
                        binding.txNoInternet.visibility = View.VISIBLE
                        binding.valueTitle.visibility = View.GONE
                    }
                }

            }
        )

        networkObserver.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        networkObserver.stop()
        _binding = null
    }

}
