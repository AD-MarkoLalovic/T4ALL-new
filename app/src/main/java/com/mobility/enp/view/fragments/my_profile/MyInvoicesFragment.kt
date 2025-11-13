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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.data.model.api_my_invoices.refactor.MyInvoicesResponse
import com.mobility.enp.databinding.FragmentBillsBinding
import com.mobility.enp.util.FragmentResultKeys
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.NetworkObserver
import com.mobility.enp.util.SharedPreferencesHelper
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.util.toast
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.my_invoices_adapters.BillsDetailsAdapter
import com.mobility.enp.view.adapters.my_invoices_adapters.MonthlyBillsAdapter
import com.mobility.enp.view.dialogs.NotificationsRequestDialog
import com.mobility.enp.view.dialogs.PermissionDeniedDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.MyInvoicesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.mobility.enp.data.model.api_my_invoices.refactor.Data
import com.mobility.enp.util.Util

class MyInvoicesFragment : Fragment(), MonthlyBillsAdapter.TriggerSpinner,
    MonthlyBillsAdapter.MontYearListener {

    private var _binding: FragmentBillsBinding? = null
    private val binding: FragmentBillsBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: MyInvoicesViewModel by activityViewModels { MyInvoicesViewModel.Factory }

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

        setObservers()

        setSelectedButton(binding.buttonAll)
        setButtonsEnabled(true)

        setListener()
        setupNetworkObserver()
        permissionNotificationDeniedDialogResultListener()

        if (!Util.isNetworkAvailable(requireContext())) {
            binding.txNoInternet.visibility = View.VISIBLE
            showNoConnectionState()

        }
    }

    private fun setObservers() {
        collectLatestLifecycleFlow(viewModel.myInvoices) { serverResponse ->
            when (serverResponse) {
                is SubmitResult.Loading -> {
                    binding.invoicesLoadingView.visibility = View.VISIBLE
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
            when(result) {
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
            binding.buttonAll.visibility = View.VISIBLE
            binding.valueTitle.visibility = View.VISIBLE

            for (country in allowedCountry) {
                when (country.value) {
                    "RS" -> {
                        binding.buttonSerbia.visibility = View.VISIBLE
                    }

                    "ME" -> {
                        binding.buttonMontenegro.visibility = View.VISIBLE
                    }

                    "MK" -> {
                        binding.northMacedonia.visibility = View.VISIBLE
                    }

                    "HR" -> {
                        binding.buttonCroatia.visibility = View.VISIBLE
                    }
                }
            }
        }

        if (response.data.data!!.months.isEmpty()) {
            binding.textNoBills.visibility = View.VISIBLE
            setButtonsEnabled(true)
        } else {
            dataExists = response.data.data

            binding.textNoBills.visibility = View.GONE
            binding.recyclerViewBills.visibility = View.VISIBLE
            adapterMonthly = MonthlyBillsAdapter(
                response.data.data,
                viewModel,
                this,
                this,
                this,
                franchiseViewModel.franchiseModel.value
            )
            binding.recyclerViewBills.adapter = adapterMonthly
            binding.recyclerViewBills.layoutManager = LinearLayoutManager(requireContext())

            setButtonsEnabled(true)

            viewModel.setLocalData(response.data)
        }

    }

    private fun handleError(error: Throwable) {
        when(error) {
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

    private fun setListener() {
        binding.buttonAll.setOnClickListener {
            binding.textNoBills.visibility = View.GONE
            setSelectedButton(it)
            if (::adapterMonthly.isInitialized) {
                adapterMonthly.resetAdapter()
            }
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.setSelectedCountry("")
            setButtonsEnabled(false)
            viewModel.fetchMonthlyInvoices()
        }

        binding.buttonCroatia.setOnClickListener {
            binding.textNoBills.visibility = View.GONE
            setSelectedButton(it)
            if (::adapterMonthly.isInitialized) {
                adapterMonthly.resetAdapter()
            }
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.setSelectedCountry("HR")
            setButtonsEnabled(false)
            viewModel.fetchMonthlyInvoices()
        }

        binding.northMacedonia.setOnClickListener {
            binding.textNoBills.visibility = View.GONE
            setSelectedButton(it)
            if (::adapterMonthly.isInitialized) {
                adapterMonthly.resetAdapter()
            }
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.setSelectedCountry("MK")
            setButtonsEnabled(false)
            viewModel.fetchMonthlyInvoices()
        }

        binding.buttonMontenegro.setOnClickListener {
            binding.textNoBills.visibility = View.GONE
            setSelectedButton(it)
            if (::adapterMonthly.isInitialized) {
                adapterMonthly.resetAdapter()
            }
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.setSelectedCountry("ME")
            setButtonsEnabled(false)
            viewModel.fetchMonthlyInvoices()
        }

        binding.buttonSerbia.setOnClickListener {
            binding.textNoBills.visibility = View.GONE
            setSelectedButton(it)
            if (::adapterMonthly.isInitialized) {
                adapterMonthly.resetAdapter()
            }
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.setSelectedCountry("RS")
            setButtonsEnabled(false)
            viewModel.fetchMonthlyInvoices()
        }

    }

    private fun setButtonsEnabled(enabled: Boolean) { // to prevent button spam until data is fetched from api
        binding.buttonAll.isEnabled = enabled
        binding.buttonCroatia.isEnabled = enabled
        binding.buttonMontenegro.isEnabled = enabled
        binding.northMacedonia.isEnabled = enabled
        binding.buttonSerbia.isEnabled = enabled
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


    private fun setSelectedButton(selectedButton: View) = with(binding) {
        northMacedonia.isSelected = false
        buttonSerbia.isSelected = false
        buttonMontenegro.isSelected = false
        buttonCroatia.isSelected = false
        buttonAll.isSelected = false

        selectedButton.isSelected = true
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
        viewModel.resetState()
        _binding = null
    }

}
