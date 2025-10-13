package com.mobility.enp.view.fragments.my_profile

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.mobility.enp.util.SharedPreferencesHelper
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        viewModel.fetchInitialData()
    }

    private fun setListener() {
        allStatusText = requireContext().getString(R.string.all_status_tags)
        binding.editSerialNumberMyTags.addTextChangedListener(textWatcher)

        binding.buttonAddTag.setOnClickListener {
            findNavController().navigate(R.id.action_myTagsFragment2_to_addTagFragment)
        }
    }

    private fun observeMyTags() {
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
                        ReportType.DEACTIVATED -> getString(R.string.deactivate_tag_successful)
                        ReportType.ACTIVATED -> getString(R.string.activate_tag_successful)
                        else -> ""
                    }

                    showToastMessage(message)

                    resetFragment()
                }
            }

        }

        collectLatestLifecycleFlow(viewModel.initialData) { result ->
            when (result) {

                is MyTagsViewModel.SubmitResultMyTags.Loading -> {
                    binding.progbar.visibility = View.VISIBLE
                }

                is MyTagsViewModel.SubmitResultMyTags.Success -> {
                    binding.progbar.visibility = View.GONE

                    val myTags = result.data

                    if (myTags.isEmpty()) {
                        binding.myTagsContainer.visibility = View.GONE
                    } else {
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

                        // sets available countries because they return all of them only when there is no country filter

                        allowedCountriesAdapter.submitList(countryList) {
                            allowedCountriesAdapter.setTabPosition(0)
                        }


                        val countryCode = when (countryList[0]) {
                            "MKD" -> "MK"
                            "MNE" -> "ME"
                            "HRV" -> "HR"
                            "SRB" -> "RS"
                            else -> ""
                        }

                        val savedTab = SharedPreferencesHelper.getUserTabCode(requireContext())

                        if (savedTab > 0) {
                            allowedCountriesAdapter.performClick(savedTab)
                        } else {
                            viewModel.setCurrentApiCountry(countryCode)
                            viewModel.fetchMyTags()
                        }
                    }
                }

                is MyTagsViewModel.SubmitResultMyTags.Failure -> {
                    handleError(result.error)
                }

                else -> {}
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
                                .distinct().sorted()

                        statusFilterAdapter.submitList(statusList) {
                            statusFilterAdapter.setTabPosition(0)
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

        collectLatestLifecycleFlow(viewModel.myTagsCountry) { serverResponse ->
            when (serverResponse) {

                is MyTagsViewModel.SubmitResultMyTags.Loading -> {
                    binding.textNoFilteredTags.visibility = View.GONE
                    binding.textNoMyTags.visibility = View.GONE

                    binding.progbar.visibility = View.VISIBLE
                }

                is MyTagsViewModel.SubmitResultMyTags.Success -> {

                    Log.d("ServResponse", "$serverResponse: ")

                    serverResponse.data.isNotEmpty().let {
                        binding.myTagsContainer.visibility = View.VISIBLE
                        binding.buttonAddTag.isEnabled = true
                    }

                    binding.textNoFilteredTags.visibility = View.GONE
                    binding.textNoMyTags.visibility = View.GONE

                    viewModel.allTags = serverResponse.data

                    statusFilterAdapter.setStatus(0)

                    val statusList =
                        listOf(requireContext().getString(R.string.all_status_tags)) + serverResponse.data.flatMap { it.statuses }
                            .mapNotNull { it.statusText }
                            .distinct().sorted()

                    statusFilterAdapter.submitList(statusList) {
                        statusFilterAdapter.setTabPosition(0)
                    }

                    tagsListAdapter.setItems(serverResponse.data)
                }

                is MyTagsViewModel.SubmitResultMyTags.Failure -> {
                    Log.d("ServResponse", "ServError ${serverResponse.error}")
                }

                else -> {
                    MyTagsViewModel.SubmitResultMyTags.Idle
                }
            }
            viewLifecycleOwner.lifecycleScope.launch {
                delay(1000L)
                unhideUI()
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

                    // i have added a delay because if called immediately api returns old data i assume they need a couple of seconds to write this to database for all countries

                    binding.progbar.visibility = View.VISIBLE

                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        delay(2000)
                        withContext(Dispatchers.Main) {
                            resetFragment()
                        }
                    }
                }
            }
        }
    }

    // forces fragment to reset
    private fun resetFragment() {
        findNavController().popBackStack(
            R.id.myTagsFragment2,
            true
        )
        findNavController().navigate(R.id.myTagsFragment2)
    }

    private fun unhideUI() {
        binding.cyclerContent.visibility = View.VISIBLE
        binding.progbar.visibility = View.GONE
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
            }, onActivateTagClicked = { tagData ->
                Log.d("data", "setAdapters: $tagData")
                LostTagDialog.newInstance(
                    title = requireContext().getString(R.string.activate_tag),
                    subtitle = requireContext().getString(R.string.activate_tag_text),
                    onButtonClick = {
                        viewModel.activateTagByCountry(tagData)
                    }
                ).show(parentFragmentManager, "FoundTagDialog")
            }, onDeactivateTagClicked = { tagData ->
                Log.d("data", "setAdapters: $tagData")
                LostTagDialog.newInstance(
                    title = requireContext().getString(R.string.deactivate_tag),
                    subtitle = requireContext().getString(R.string.deactivate_tag_text),
                    onButtonClick = {
                        viewModel.deactivateTagByCountry(tagData)
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
            binding.textNoMyTags.visibility = View.GONE

            tagsListAdapter.selectedCountry = selectedCountry
            tagsListAdapter.clearData()
            viewModel.setCountryFilter(selectedCountry)

            val countryCode = when (selectedCountry) {
                "MKD" -> "MK"
                "MNE" -> "ME"
                "HRV" -> "HR"
                "SRB" -> "RS"
                else -> ""
            }

            SharedPreferencesHelper.setCurrentTab(
                requireContext(),
                allowedCountriesAdapter.getTabPosition()
            )

            binding.cyclerContent.visibility = View.GONE
            binding.progbar.visibility = View.VISIBLE

            viewModel.fetchShowActivateDeactivateButtonsByCountry(countryCode)
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