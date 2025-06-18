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
import com.mobility.enp.R
import com.mobility.enp.databinding.FragmentTagsBinding
import com.mobility.enp.util.NetworkError
import com.mobility.enp.util.SubmitResultFold
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.my_tags.MyTagsListAdapter
import com.mobility.enp.view.adapters.my_tags.MyTagsStatusFilterAdapter
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.MyTagsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyTagsFragment : Fragment() {

    private var _binding: FragmentTagsBinding? = null
    private val binding: FragmentTagsBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: MyTagsViewModel by viewModels { MyTagsViewModel.factory }

    private lateinit var statusFilterAdapter: MyTagsStatusFilterAdapter
    private lateinit var tagsListAdapter: MyTagsListAdapter

    private var statusListInitialized = false
    private lateinit var textWatcher: TextWatcher

    companion object {
        const val TAG = "TAGS_FRAGMENT"
    }

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
        binding.editSerialNumberMyTags.addTextChangedListener(textWatcher)

        binding.buttonAddTag.setOnClickListener {
            findNavController().navigate(R.id.action_myTagsFragment2_to_addTagFragment)
        }
    }

    private fun observeMyTags() {
        collectLatestLifecycleFlow(viewModel.myTags) { result ->
            when (result) {
                is SubmitResultFold.Loading -> {
                    binding.progbar.visibility = View.VISIBLE
                }

                is SubmitResultFold.Success -> {
                    binding.progbar.visibility = View.GONE

                    val myTags = result.data

                    if (myTags.isEmpty()) {
                        binding.textNoMyTags.visibility = View.VISIBLE
                        binding.myTagsContainer.visibility = View.GONE

                    } else {
                        binding.textNoMyTags.visibility = View.GONE
                        binding.myTagsContainer.visibility = View.VISIBLE

                        if (!statusListInitialized) {
                            val statusList =
                                listOf(requireContext().getString(R.string.all_status_tags)) + myTags.flatMap { it.statuses }
                                    .mapNotNull { it.statusText }
                                    .distinct()
                            statusFilterAdapter.submitList(statusList)
                            statusListInitialized = true
                        }
                        tagsListAdapter.submitList(myTags)
                    }
                }

                is SubmitResultFold.Failure -> {
                    handleError(result.error)
                }

                is SubmitResultFold.Idle -> {}
            }
        }
    }

    private fun setupTextWatcher() {
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                Log.d("MARKO", "onTextChanged: $query")
                statusFilterAdapter.clearStatus()

                // Filtriramo listu tagova na osnovu unosa u editText
                val result = viewModel.allTags.filter { tag ->
                    tag.serialNumber.lowercase().contains(query)
                }

                tagsListAdapter.submitList(result)
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun setAdapters() {
        statusFilterAdapter = MyTagsStatusFilterAdapter { selectedStatus ->

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

            viewModel.filterTagsByStatus(selectedStatus)
        }
        binding.cyclerTagTypes.adapter = statusFilterAdapter

        tagsListAdapter = MyTagsListAdapter()
        binding.cyclerContent.adapter = tagsListAdapter
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
        binding.progbar.visibility = View.GONE

        val bindingMain = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)

        binding.buttonAddTag.isEnabled = true

        viewModel.fetchMyTags()
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}