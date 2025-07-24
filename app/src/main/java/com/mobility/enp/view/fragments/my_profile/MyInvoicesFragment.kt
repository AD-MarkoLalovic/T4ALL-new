package com.mobility.enp.view.fragments.my_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.data.model.api_my_invoices.refactor.MyInvoicesResponse
import com.mobility.enp.databinding.FragmentBillsBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.my_invoices_adapters.BillsDetailsAdapter
import com.mobility.enp.view.adapters.my_invoices_adapters.MonthlyBillsAdapter
import com.mobility.enp.view.adapters.refund_request_adapters.RefundRequestsCreatedAdapter
import com.mobility.enp.view.dialogs.NotificationsRequestDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.MyInvoicesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyInvoicesFragment : Fragment(), MonthlyBillsAdapter.TriggerSpinner,
    MonthlyBillsAdapter.MontYearListener {

    private var _binding: FragmentBillsBinding? = null
    private val binding: FragmentBillsBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: MyInvoicesViewModel by viewModels()

    private lateinit var adapterMonthly: MonthlyBillsAdapter

    private var errorBody: MutableLiveData<ErrorBody> = MutableLiveData()
    private var month = ""
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is granted. You can proceed with sending notifications.
                userPerm.onPermissionGranted()
                sendNotification()
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

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        setObserversError()

        setSelectedButton(binding.buttonAll)
        setButtonsEnabled(true)

        viewModel.fetchMonthlyInvoices(errorBody)

        setListener()

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.billPad.observe(viewLifecycleOwner) { bool ->
            if (bool != null) {
                if (bool) {
                    Toast.makeText(
                        requireContext(), getText(R.string.payment_successfully), Toast.LENGTH_SHORT
                    ).show()
                    viewModel.fetchMonthlyInvoices(errorBody)
                } else {
                    Toast.makeText(
                        requireContext(), getText(R.string.payment_unsuccessful), Toast.LENGTH_SHORT
                    ).show()
                    viewModel.fetchMonthlyInvoices(errorBody)
                }
            }
        }

        viewModel.monthlyInvoicesList.observe(viewLifecycleOwner) { response ->
            response?.let {

                response.data?.allowedCountries?.let { allowedCountry ->
                    binding.buttonAll.visibility = View.VISIBLE

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

                if (it.data!!.months.isEmpty()) {
                    binding.textNoBills.visibility = View.VISIBLE
                    setButtonsEnabled(true)
                } else {
                    binding.textNoBills.visibility = View.GONE
                    binding.recyclerViewBills.visibility = View.VISIBLE
                    adapterMonthly = MonthlyBillsAdapter(
                        it.data!!,
                        viewModel,
                        errorBody,
                        this,
                        this,
                        this,
                        franchiseViewModel.franchiseModel.value
                    )
                    binding.recyclerViewBills.adapter = adapterMonthly
                    binding.recyclerViewBills.layoutManager = LinearLayoutManager(requireContext())

                    setButtonsEnabled(true)

                    viewModel.setLocalData(it)
                }
            }
        }

        viewModel.checkNetDownload.observe(viewLifecycleOwner) {
            if (!it) {
                showNoInternetDialog()
            }
        }

        viewModel.checkNetMyInvoices.observe(viewLifecycleOwner) { hasInternet ->
            if (hasInternet != null && !hasInternet) {

                viewLifecycleOwner.lifecycleScope.launch {
                    val localData = viewModel.checkBills()

                    if (localData != null) {
                        val binding = (activity as MainActivity).binding
                        MainActivity.showSnackMessage(
                            getString(R.string.offline_using_stored_data), binding
                        )
                        viewModel.fetchLocalData()
                    } else {

                        showNoInternetDialog()

                        val binding = (activity as MainActivity).binding
                        MainActivity.showSnackMessage(
                            getString(R.string.checking_for_connection), binding
                        )

                        withContext(Dispatchers.IO) {
                            context?.let {
                                while (true) {
                                    if (Repository.isNetworkAvailable(it)) {
                                        triggerUpdate()
                                        break
                                    } else {
                                        delay(1000L)
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private fun showNoInternetDialog() {
        val bundle = Bundle().apply {
            putString(getString(R.string.title), getString(R.string.no_connection_title))
            putString(
                getString(R.string.subtitle), getString(R.string.please_connect_to_the_internet)
            )
        }

        findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)
    }

    private fun sendNotification() {
        Toast.makeText(requireContext(), getString(R.string.permission_granted), Toast.LENGTH_SHORT)
            .show()
    }

    private suspend fun triggerUpdate() {
        val mainActivity = activity as? MainActivity
        val bindingMain = mainActivity?.binding

        bindingMain?.let {
            withContext(Dispatchers.Main) {
                MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)
                binding.invoicesLoadingView.visibility = View.GONE

                viewModel.fetchMonthlyInvoices(errorBody)
            }
        }
    }

    private fun setListener() {
        viewModel.monthlyInvoicesList.observe(viewLifecycleOwner) { months ->
            months?.let {
                binding.recyclerViewBills.visibility = View.VISIBLE
                binding.invoicesLoadingView.visibility = View.GONE
            }
        }

        binding.buttonAll.setOnClickListener {
            setSelectedButton(it)
            if (::adapterMonthly.isInitialized) {
                adapterMonthly.resetAdapter()
            }
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.setSelectedCountry("")
            setButtonsEnabled(false)
            viewModel.fetchMonthlyInvoices(errorBody)
        }

        binding.buttonCroatia.setOnClickListener {
            setSelectedButton(it)
            if (::adapterMonthly.isInitialized) {
                adapterMonthly.resetAdapter()
            }
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.setSelectedCountry("HR")
            setButtonsEnabled(false)
            viewModel.fetchMonthlyInvoices(errorBody)
        }

        binding.northMacedonia.setOnClickListener {
            setSelectedButton(it)
            if (::adapterMonthly.isInitialized) {
                adapterMonthly.resetAdapter()
            }
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.setSelectedCountry("MK")
            setButtonsEnabled(false)
            viewModel.fetchMonthlyInvoices(errorBody)
        }

        binding.buttonMontenegro.setOnClickListener {
            setSelectedButton(it)
            if (::adapterMonthly.isInitialized) {
                adapterMonthly.resetAdapter()
            }
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.setSelectedCountry("ME")
            setButtonsEnabled(false)
            viewModel.fetchMonthlyInvoices(errorBody)
        }

        binding.buttonSerbia.setOnClickListener {
            setSelectedButton(it)
            if (::adapterMonthly.isInitialized) {
                adapterMonthly.resetAdapter()
            }
            binding.invoicesLoadingView.visibility = View.VISIBLE
            viewModel.setSelectedCountry("RS")
            setButtonsEnabled(false)
            viewModel.fetchMonthlyInvoices(errorBody)
        }

    }

    private fun setObserversError() {
        errorBody = MutableLiveData()
        errorBody.observe(viewLifecycleOwner) { errorBody ->

            setButtonsEnabled(true)

            context?.let { context ->
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
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


    override fun pagingUpdate(nextPage: Int, data: MutableLiveData<MyInvoicesResponse>) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewModel.fetchMonthlyInvoicesPaging(errorBody, data, nextPage)
        }
    }

    override fun pagingUpdateBill(
        nextPage: Int, data: MutableLiveData<BillsDetailsResponse>, availableCurrencies: String
    ) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            viewModel.fetchBillDetailsPaging(data, month, availableCurrencies, nextPage, errorBody)
        }
    }

    override fun requestNotificationFromUser(userPermission: BillsDetailsAdapter.UserPermission) {
        lifecycleScope.launch {
            val fragmentManager = (requireContext() as AppCompatActivity).supportFragmentManager
            val generalMessageDialog = NotificationsRequestDialog(
                getString(R.string.notification_title),
                getString(R.string.notification_subtitle),
                object : NotificationsRequestDialog.OnButtonClick {
                    override fun onClickConfirmed() {
                        userPerm = userPermission
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }

                    override fun onClickRejected() {
                        userPermission.onPermissionDenied()
                    }
                })
            generalMessageDialog.show(fragmentManager, "permDialog")
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

    override fun onMontYearSelected(montYear: String) {
        month = montYear
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
