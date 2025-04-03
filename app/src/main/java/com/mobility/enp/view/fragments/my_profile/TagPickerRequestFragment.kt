package com.mobility.enp.view.fragments.my_profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.R
import com.mobility.enp.data.model.api_my_profile.refund_request.SendRefundRequest
import com.mobility.enp.databinding.FragmentTagPickerRequestBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.refund_request_adapters.RefundRequestTagPickerAdapter
import com.mobility.enp.view.ui_models.BankUIModel
import com.mobility.enp.view.ui_models.refund_request.TagsRefundRequestUIModel
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.TagPickerRequestViewModel
import kotlinx.coroutines.launch

class TagPickerRequestFragment : Fragment() {

    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: TagPickerRequestViewModel by viewModels { TagPickerRequestViewModel.Factory }
    private var _binding: FragmentTagPickerRequestBinding? = null
    private val binding: FragmentTagPickerRequestBinding get() = _binding!!
    private lateinit var adapter: RefundRequestTagPickerAdapter
    private var tagSerialNumber: String? = null
    private lateinit var bankNames: MutableList<String>
    private var bankId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagPickerRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeTagPickerRequest()
        observerBanks()
        observeSubmitRefundRequest()
        setFranchiser()

        binding.buttonSendRequest.setOnClickListener {
            onSendRefundRequestClicked()
        }
    }

    /**
     * Observes the tag picker request result and updates the UI accordingly.
     */
    private fun observeTagPickerRequest() {
        collectLatestLifecycleFlow(viewModel.tagPickerRequest) { tag ->
            when (tag) {
                is SubmitResult.Loading -> showLoadingState()
                is SubmitResult.Empty -> showEmptyState()
                is SubmitResult.Success -> showSuccessState(tag.data)
                is SubmitResult.FailureNoConnection -> showNoConnectionState()
                is SubmitResult.FailureServerError -> showServerErrorState()
                is SubmitResult.FailureApiError -> showApiErrorState(tag.errorMessage)
                else -> {} // todo add case for invalid server token
            }
        }
    }

    /**
     * Observes banks.
     */
    private fun observerBanks() {
        collectLatestLifecycleFlow(viewModel.banks) { bank ->
            when (bank) {
                SubmitResult.Loading -> {
                    binding.refundLoadingTagPicker.visibility = View.VISIBLE
                }

                is SubmitResult.FailureApiError -> {
                    showError(bank.errorMessage)
                }

                SubmitResult.FailureNoConnection -> showNoConnectionState()

                is SubmitResult.Success -> {
                    setupBankSpinner(bank.data)
                }

                else -> {
                    SubmitResult.Empty
                    SubmitResult.FailureServerError
                }
            }

        }
    }


    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.buttonSendRequest.backgroundTintList = ColorStateList.valueOf(color)

                val parent = binding.constraintContainerRefund

                for (i in 0 until parent.childCount) {
                    val view = parent.getChildAt(i)
                    if (view is TextInputLayout) {
                        view.boxStrokeColor = color
                        val editText = view.editText
                        editText?.setTextColor(color)
                    }
                }
            }
        }
    }


    /**
     * Observes submit refund request
     */

    private fun observeSubmitRefundRequest() {
        collectLatestLifecycleFlow(viewModel.refundRequestState) { refundRequest ->
            when (refundRequest) {
                SubmitResult.Loading -> {
                    binding.refundLoadingTagPicker.visibility = View.VISIBLE
                }

                is SubmitResult.FailureApiError -> {
                    binding.refundLoadingTagPicker.visibility = View.GONE
                    showError(refundRequest.errorMessage)
                }

                SubmitResult.FailureNoConnection -> showNoConnectionState()
                SubmitResult.FailureServerError -> {
                    binding.refundLoadingTagPicker.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                }

                is SubmitResult.Success -> {
                    binding.refundLoadingTagPicker.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.request_recorded),
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().navigate(R.id.action_tagPickerRequestFragment_to_refundRequestFragment2)
                }

                else -> {
                    SubmitResult.Empty
                }
            }
        }
    }

    /**
     * Displays the loading state.
     */
    private fun showLoadingState() {
        binding.refundLoadingTagPicker.visibility = View.VISIBLE
        binding.textNoTags.visibility = View.GONE
    }

    /**
     * Displays the empty state and disables editing fields.
     */
    private fun showEmptyState() {
        binding.refundLoadingTagPicker.visibility = View.GONE
        binding.textNoTags.visibility = View.VISIBLE
        disableEditingFields()

        val constraintLayout = binding.constraintContainerRefund
        val txHintAmountTagPicker = binding.txHintAmountTagPicker
        val textNoTags = binding.textNoTags

        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        constraintSet.connect(
            txHintAmountTagPicker.id,
            ConstraintSet.TOP,
            textNoTags.id,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.dimens_22dp)
        )

        constraintSet.applyTo(constraintLayout)
    }

    /**
     * Updates the UI to display the success state with the list of tags.
     */
    private fun showSuccessState(tag: List<TagsRefundRequestUIModel>) {
        binding.refundLoadingTagPicker.visibility = View.GONE
        binding.textNoTags.visibility = View.GONE
        binding.recyclerViewTagPicker.visibility = View.VISIBLE

        if (!::adapter.isInitialized) {
            adapter = RefundRequestTagPickerAdapter({ serialNumber ->
                tagSerialNumber = serialNumber
            }, franchiseViewModel)
            binding.recyclerViewTagPicker.adapter = adapter
        }
        adapter.submitList(tag)

        val constraintLayout = binding.constraintContainerRefund
        val txHintAmountTagPicker = binding.txHintAmountTagPicker
        val recyclerView = binding.recyclerViewTagPicker

        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        constraintSet.connect(
            txHintAmountTagPicker.id,
            ConstraintSet.TOP,
            recyclerView.id,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.dimens_22dp)
        )

        constraintSet.applyTo(constraintLayout)

    }

    /**
     * Displays a no-connection message using a Snackbar.
     */
    private fun showNoConnectionState() {
        binding.refundLoadingTagPicker.visibility = View.GONE
        viewLifecycleOwner.lifecycleScope.launch {
            val hasData = viewModel.existLocalData()
            if (!hasData) {
                binding.textNoTags.visibility = View.VISIBLE
            }
        }
        noInternetMessage()
    }

    /**
     * Displays a server error message.
     */
    private fun showServerErrorState() {
        binding.refundLoadingTagPicker.visibility = View.GONE
        binding.textNoTags.visibility = View.VISIBLE
        Toast.makeText(
            requireContext(),
            getString(R.string.server_error_msg),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Displays an API error message.
     */
    private fun showApiErrorState(errorMessage: String) {
        binding.refundLoadingTagPicker.visibility = View.GONE
        binding.textNoTags.visibility = View.VISIBLE
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    /**
     * Disables editing for all input fields.
     */
    private fun disableEditingFields() {
        with(binding) {
            editAmount.isFocusable = false
            editAmount.isClickable = false
            editAmount.isFocusableInTouchMode = false

        }
    }

    /**
     * Displays a no-internet message using a SnackBar.
     */
    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

    private fun setupBankSpinner(bankList: List<BankUIModel>) {
        val color = franchiseViewModel.franchiseModel.value?.franchisePrimaryColor

        // Dodavanje hint-a na početak liste
        bankNames = mutableListOf(getString(R.string.hint_select_bank)).apply {
            addAll(bankList.map { it.bankName })
        }

        // Adapter za spinner
        val bankAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_banks_spinner,
            bankNames
        ) {
            // Onemogućiti klik na poziciju 0, tj. hint
            override fun isEnabled(position: Int) = position != 0

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)

                color?.let {
                    (view as? TextView)?.setTextColor(
                        ColorStateList.valueOf(it)
                    )
                } ?: run {
                    (view as? TextView)?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            if (position == 0) R.color.hint_text_color else R.color.figmaSplashScreenColor
                        )
                    )
                }

                return view
            }

        }

        binding.bankSpinner.apply {
            adapter = bankAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val textView = view as? TextView
                    color?.let {
                        if (position == 0) {
                            textView?.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.hint_text_color)
                            )
                            return
                        } else {
                            textView?.setTextColor(
                                color
                            )
                        }
                    } ?: run {
                        if (position == 0) {
                            textView?.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.hint_text_color)
                            )
                            return
                        } else {
                            textView?.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.figmaSplashScreenColor
                                )
                            )
                        }
                    }

                    // Obrada odabrane stavke
                    val selectedBank = bankList[position - 1]
                    bankId = selectedBank.id
                    setupUniqueNumberSpinner(selectedBank.uniqueNumber)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Nema akcije
                }
            }
        }
    }

    private fun setupUniqueNumberSpinner(uniqueNumbers: List<Int>) {
        val color = franchiseViewModel.franchiseModel.value?.franchisePrimaryColor


        if (uniqueNumbers.size == 1) {
            // Ako postoji samo jedan element
            val singleItem = uniqueNumbers.first().toString()
            binding.uniqueNumbersSpinner.apply {
                adapter = object : ArrayAdapter<String>(
                    requireContext(),
                    R.layout.item_unique_numbers_spinner,
                    listOf(singleItem)
                ) {
                    override fun getView(
                        position: Int,
                        convertView: View?,
                        parent: ViewGroup
                    ): View {
                        val view = super.getView(position, convertView, parent)
                        color?.let {
                            (view as? TextView)?.setTextColor(color)

                        } ?: run {
                            (view as? TextView)?.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.figmaSplashScreenColor
                                )
                            )
                        }
                        return view
                    }
                }
                setSelection(0)
                isClickable = false
                isEnabled = false
            }
        } else {
            val uniqueNumberStrings = uniqueNumbers.map { it.toString() }
            val bankCodeAdapter = object : ArrayAdapter<String>(
                requireContext(),
                R.layout.item_unique_numbers_spinner_arrow,
                uniqueNumberStrings
            ) {
                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    view.layoutParams.height =
                        (32 * resources.displayMetrics.density).toInt() // 32dp u px

                    color?.let {
                        (view as? TextView)?.setTextColor(
                            color
                        )
                    } ?: run {
                        (view as? TextView)?.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                if (position == 0) R.color.hint_text_color else R.color.figmaSplashScreenColor
                            )
                        )
                    }


                    return view
                }

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    color?.let {
                        (view as? TextView)?.setTextColor(color)

                    } ?: run {
                        (view as? TextView)?.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.figmaSplashScreenColor)
                        )
                    }
                    return view
                }
            }

            binding.uniqueNumbersSpinner.apply {
                adapter = bankCodeAdapter
                isClickable = true
                isEnabled = true
            }
        }

        enableAccountInputs()

    }

    private fun enableAccountInputs() = with(binding) {
        etCenterAccountNumber.enableEdit()
        etRightAccountNumber.enableEdit()
        etSecondTagPicker.backgroundTintList = null
        txCenterAccountNumber.setBoxBackgroundColorResource(R.color.white)
        uniqueNumbersSpinner.backgroundTintList = null
    }

    private fun View.enableEdit() {
        isClickable = true
        isFocusable = true
        isFocusableInTouchMode = true
        if (this is EditText) isCursorVisible = true
    }


    private fun collectRefundRequestData(): SendRefundRequest? {
        val selectTag = tagSerialNumber
        if (selectTag == null) {
            showError(getString(R.string.please_select_tag))
            return null
        }

        val amount = binding.editAmount.text.toString().trim().toIntOrNull()
        if (amount == null || amount <= 0) {
            showError(getString(R.string.enter_amount))
            return null
        }

        val selectedBankPosition = binding.bankSpinner.selectedItemPosition
        if (selectedBankPosition == 0) {
            showError(getString(R.string.enter_name_bank))
            return null
        }

        val selectedBank = bankId ?: selectedBankPosition

        val uniqueNumber = binding.uniqueNumbersSpinner.selectedItem.toString().trim()
        val centerAccountNumber = binding.etCenterAccountNumber.text.toString().trim()
        val rightAccountNumber = binding.etRightAccountNumber.text.toString().trim()

        // Validacija unosa za račun
        if (uniqueNumber.isEmpty() || centerAccountNumber.isEmpty() || rightAccountNumber.isEmpty()) {
            showError(getString(R.string.enter_bank_account))
            return null
        }

        val sendRefundRequest = SendRefundRequest(
            selectTag,
            selectedBank.toString(),
            uniqueNumber,
            centerAccountNumber,
            rightAccountNumber,
            amount
        )

        return sendRefundRequest
    }

    private fun onSendRefundRequestClicked() {
        val refundRequest = collectRefundRequestData() ?: return

        viewModel.postRefundsRequest(refundRequest)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
