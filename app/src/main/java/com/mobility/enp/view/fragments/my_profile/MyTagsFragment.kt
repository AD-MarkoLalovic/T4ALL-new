package com.mobility.enp.view.fragments.my_profile

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentTagsBinding
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.util.Util.isTablet
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.my_tags.MyTagsListAdapter
import com.mobility.enp.view.adapters.my_tags.MyTagsStatusFilterAdapter
import com.mobility.enp.view.dialogs.LostTagDialog
import com.mobility.enp.view.ui_models.my_tags.TagUiModel
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.MyTagsViewModel
import com.mobility.enp.viewmodel.ReportType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyTagsFragment : Fragment() {

    private var _binding: FragmentTagsBinding? = null
    private val binding: FragmentTagsBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: MyTagsViewModel by viewModels { MyTagsViewModel.factory }

    private lateinit var statusFilterAdapter: MyTagsStatusFilterAdapter
    private lateinit var allowedCountriesAdapter: MyTagsStatusFilterAdapter
    private lateinit var tagsListAdapter: MyTagsListAdapter

    private lateinit var textWatcher: TextWatcher
    private lateinit var allStatusText: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFranchiser()
        setupTextWatcher()
        setListener()
        setAdapters()
        observeMyTags()

        viewModel.fetchMyTags()
    }

    private fun setListener() {
        allStatusText = requireContext().getString(R.string.all_status_tags)
        binding.editSerialNumberMyTags.addTextChangedListener(textWatcher)

        binding.buttonAddTag.setOnClickListener {
            findNavController().navigate(R.id.action_myTagsFragment2_to_addTagFragment)
        }
    }

    private fun observeMyTags() {
        collectLatestLifecycleFlow(viewModel.myTagsCountry) { result ->
            when (result) {
                is MyTagsViewModel.SubmitResultMyTags.Loading -> {
                    binding.progbar.visibility = View.VISIBLE
                }

                is MyTagsViewModel.SubmitResultMyTags.Success -> {
                    binding.progbar.visibility = View.GONE
                    val activateTag = result.data[0].showButtonActivateTag ?: false
                    val deactivateTag = result.data[0].showButtonDeactivateTag ?: false
//                    tagsListAdapter.updateButtons(activateTag,deactivateTag)
                }

                is MyTagsViewModel.SubmitResultMyTags.Failure -> {
                    handleError(result.error)
                }

                is MyTagsViewModel.SubmitResultMyTags.Idle -> {}

                else -> {}
            }
        }
        collectLatestLifecycleFlow(viewModel.deactivateActivateTag) { result ->
            when (result) {
                is SubmitResultFold.Failure -> {
                    handleError(result.error)
                }

                SubmitResultFold.Idle -> {}
                SubmitResultFold.Loading -> binding.progbar.visibility = View.VISIBLE
                is SubmitResultFold.Success<*> -> {
                    val type = result.reportType
                    val message = when (type) {
                        ReportType.DEACTIVATED -> getString(R.string.reported_lost_tag_successfully)
                        ReportType.ACTIVATED -> getString(R.string.reported_found_tag_successfully)
                        else -> ""
                    }

                    showToastMessage(message)
                    viewModel.fetchMyTags()
                }
            }

        }
        collectLatestLifecycleFlow(viewModel.myTags) { result ->
            when (result) {
                is MyTagsViewModel.SubmitResultMyTags.Loading -> {
                    binding.progbar.visibility = View.VISIBLE
                }

                is MyTagsViewModel.SubmitResultMyTags.Success -> {
                    binding.progbar.visibility = View.GONE
                    binding.buttonAddTag.isEnabled = true
                    val myTags = result.data

                    if (myTags.isEmpty()) {
                        binding.textNoMyTags.visibility = View.VISIBLE
                        binding.myTagsContainer.visibility = View.GONE

                    } else {
                        binding.textNoMyTags.visibility = View.GONE
                        binding.myTagsContainer.visibility = View.VISIBLE
                        viewModel.setAllStatusLabel(allStatusText)

                        val statusList =
                            listOf(requireContext().getString(R.string.all_status_tags)) + myTags.flatMap { it.statuses }
                                .mapNotNull { it.statusText }
                                .distinct()
                        statusFilterAdapter.submitList(statusList) {
                            statusFilterAdapter.selectedStatus = 0
                        }

                        val countryList = myTags.flatMap { it.statuses }
                            .mapNotNull { it.statusesCountry }
                            .distinct()
                            .reversed()
                            .map { code ->
                                when (code) {
                                    "RS" -> "SRB"
                                    "MK" -> "MKD"
                                    "ME" -> "MNE"
                                    "HR" -> "HRV"
                                    else -> code
                                }
                            }

                        allowedCountriesAdapter.submitList(countryList) {
                            allowedCountriesAdapter.selectedStatus = 0
                        }

                        tagsListAdapter.setItems(myTags)
                    }
                }

                is MyTagsViewModel.SubmitResultMyTags.Failure -> {
                    handleError(result.error)
                }

                is MyTagsViewModel.SubmitResultMyTags.Idle -> {}

                is MyTagsViewModel.SubmitResultMyTags.Filtered -> {
                    updateTagsList(result.data)
                }
            }
        }

        collectLatestLifecycleFlow(viewModel.reportTag) { result ->
            when (result) {
                is SubmitResultFold.Failure -> {
                    handleError(result.error)
                }

                SubmitResultFold.Idle -> {}
                SubmitResultFold.Loading -> binding.progbar.visibility = View.VISIBLE
                is SubmitResultFold.Success<*> -> {
                    val type = result.reportType
                    val message = when (type) {
                        ReportType.LOST -> getString(R.string.reported_lost_tag_successfully)
                        ReportType.FOUND -> getString(R.string.reported_found_tag_successfully)
                        else -> ""
                    }
                    showToastMessage(message)
                    viewModel.fetchMyTags()
                }
            }
        }
    }

    private fun updateTagsList(tags: List<TagUiModel>) {
        if (tags.isEmpty()) {
            binding.textNoFilteredTags.visibility = View.VISIBLE
            binding.cyclerContent.visibility = View.GONE
        } else {
            binding.textNoFilteredTags.visibility = View.GONE
            binding.cyclerContent.visibility = View.VISIBLE
        }

        tagsListAdapter.setItems(tags)
    }

    private fun setupTextWatcher() {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!binding.editSerialNumberMyTags.isFocused) return
                val query = s.toString().lowercase()
                statusFilterAdapter.clearStatus()
                allowedCountriesAdapter.clearStatus()

                // Filtriramo listu tagova na osnovu unosa u editText
                val result = viewModel.allTags.filter { tag ->
                    tag.serialNumber.lowercase().contains(query)
                }

                updateTagsList(result)
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun setAdapters() {
        tagsListAdapter = MyTagsListAdapter(
            onLostClicked = { serialNumber ->
                LostTagDialog.newInstance(
                    title = requireContext().getString(R.string.confirm_lost_tag),
                    subtitle = requireContext().getString(R.string.dialog_lost_tag_message),
                    onButtonClick = {
                        viewModel.reportLostTag(serialNumber)
                    }
                ).show(parentFragmentManager, "LostTagDialog")
            },
            onFoundClicked = { serialNumber ->
                LostTagDialog.newInstance(
                    title = requireContext().getString(R.string.confirm_found_tag),
                    subtitle = requireContext().getString(R.string.report_found_tag),
                    onButtonClick = {
                        viewModel.reportFoundTag(serialNumber)
                    }
                ).show(parentFragmentManager, "FoundTagDialog")
            }
        )
        binding.cyclerContent.adapter = tagsListAdapter

        tagsListAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                updateRecyclerViewHeight(binding.cyclerContent)
            }
        })

        statusFilterAdapter = MyTagsStatusFilterAdapter { selectedStatus ->
            clearSearchFieldFocusAndKeyboard()
            viewModel.setStatusFilter(selectedStatus)
        }
        binding.cyclerTagTypes.adapter = statusFilterAdapter

        allowedCountriesAdapter = MyTagsStatusFilterAdapter { selectedCountry ->
            clearSearchFieldFocusAndKeyboard()
            tagsListAdapter.selectedCountry = selectedCountry
            viewModel.setCountryFilter(selectedCountry)
        }
        binding.rvAllowedCountries.adapter = allowedCountriesAdapter

    }

    private fun clearSearchFieldFocusAndKeyboard() {
        //uklanjam tekst sa editText polja, sklanjam mu fokus i uklanjam tastaturu
        binding.editSerialNumberMyTags.apply {
            removeTextChangedListener(textWatcher)
            setText("")
            clearFocus()
            addTextChangedListener(textWatcher)
        }
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.editSerialNumberMyTags.windowToken, 0)
    }

    private fun handleError(error: Throwable) {
        when (error) {
            is NetworkError.ServerError -> {
                binding.progbar.visibility = View.GONE
                showToastMessage(getString(R.string.server_error_msg))
            }

            is NetworkError.ApiError -> {
                binding.progbar.visibility = View.GONE
                showToastMessage(
                    error.errorResponse.message ?: getString(R.string.server_error_msg)
                )
            }

            is NetworkError.NoConnection -> {
                noInternetMessage()
            }
        }
    }

    private fun noInternetMessage() {
        binding.progbar.visibility = View.VISIBLE
        binding.buttonAddTag.isEnabled = false

        val bundle = Bundle().apply {
            putString(getString(R.string.title), getString(R.string.no_connection_title))
            putString(
                getString(R.string.subtitle),
                getString(R.string.please_connect_to_the_internet)
            )
        }

        findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)

        viewLifecycleOwner.lifecycleScope.launch {
            while (!viewModel.internetChecked()) {
                delay(500L)
            }
            triggerUpdate()
        }
    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.buttonAddTag.backgroundTintList = ColorStateList.valueOf(color)
                binding.inputSerialNumber.boxStrokeColor = color

                with(binding.inputSerialNumber) {
                    val editText = this.editText
                    editText?.textSelectHandle?.setTint(color)
                    editText?.setTextColor(color)

                    val states = arrayOf(
                        intArrayOf(android.R.attr.state_pressed),  // pressed
                        intArrayOf(android.R.attr.state_focused),  // focused
                        intArrayOf()                               // default
                    )

                    val colors = intArrayOf(
                        color,        // pressed
                        color,        // focused
                        color         // default
                    )

                    this.cursorColor = ColorStateList(states, colors)
                }
            }
        }
    }

    private fun triggerUpdate() {
        if (!isAdded || _binding == null) return

        binding.progbar.visibility = View.GONE

        val bindingMain = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)

        binding.buttonAddTag.isEnabled = true

        viewModel.fetchMyTags()
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun updateRecyclerViewHeight(recyclerView: RecyclerView) {
        if (requireContext().isTablet()) return

        val adapter = recyclerView.adapter ?: return

        val params = recyclerView.layoutParams
        params.height = if (adapter.itemCount == 1) {
            ViewGroup.LayoutParams.WRAP_CONTENT
        } else {
            resources.getDimension(R.dimen.dimens_500dp).toInt()
        }

        recyclerView.layoutParams = params
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}