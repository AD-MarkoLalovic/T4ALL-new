package com.mobility.enp.view.fragments.tool_history

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.FragmentToolHistorySearchQueryBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.select.ToolHistoryTagsAdapter
import com.mobility.enp.viewmodel.PassageHistoryViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ToolHistoryFilterFragment : Fragment(), ToolHistoryTagsAdapter.TagSend {

    private var _binding: FragmentToolHistorySearchQueryBinding? = null
    private val binding: FragmentToolHistorySearchQueryBinding get() = _binding!!
    private val viewModel: PassageHistoryViewModel by activityViewModels()
    private lateinit var isInternetAvailable: MutableLiveData<Boolean>
    private val vModel: UserPassViewModel by viewModels { UserPassViewModel.Factory }

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
        viewModel.selectedTags.clear()

        context?.let {
            binding.progBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.getToolHistoryIndex(
                    it, isInternetAvailable
                )
            }
        }

        binding.btnSearch.setOnClickListener {
            if (viewModel.selectedTags.isEmpty() && !viewModel.allTagsSelected) {
                Toast.makeText(context, R.string.please_select_tag, Toast.LENGTH_SHORT).show()
            } else {
                if (viewModel.startDate.value?.inDateForm != null && viewModel.endDate.value?.inDateForm != null) {
                    if (viewModel.startDate.value!!.inDateForm!!.before(viewModel.endDate.value!!.inDateForm)) {
                        if (Repository.isNetworkAvailable(requireContext())) {
                            findNavController().navigate(ToolHistoryFilterFragmentDirections.actionToolHistorySearchFragmentToToolHistorySearchResultFragment())
                        } else {
                            val bundle = Bundle().apply {
                                putString(
                                    getString(R.string.title),
                                    getString(R.string.no_connection_title)
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
                    } else {
                        Toast.makeText(
                            context, getString(R.string.end_date_check), Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(context, R.string.select_date, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.chkBox.setOnClickListener {
            val isChecked = binding.chkBox.isChecked
            viewModel.allTagsSelected = isChecked
            setCheckboxColors(isChecked)
        }

        viewModel.startDate.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val textView = binding.txtDateLeft as TextView
                textView.text = data.formattedTime
            }
        }
        viewModel.endDate.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val textEndDate = binding.txtDateRight as TextView
                textEndDate.text = data.formattedTime
            }
        }

        binding.txtDateLeft.setOnClickListener {
            viewModel.showDatePicker(true, requireContext())
        }
        binding.txtDateRight.setOnClickListener {
            viewModel.showDatePicker(false, requireContext())
        }

        setSelectedButton(binding.buttonAll)

        binding.buttonAll.setOnClickListener {
            setSelectedButton(binding.buttonAll)
            viewModel.selectedCurrency = ""
        }
        binding.buttonRSD.setOnClickListener {
            setSelectedButton(binding.buttonRSD)
            viewModel.selectedCurrency = getString(R.string.rsd)
        }
        binding.buttonEUR.setOnClickListener {
            setSelectedButton(binding.buttonEUR)
            viewModel.selectedCurrency = getString(R.string.eur)
        }
        binding.buttonMKD.setOnClickListener {
            setSelectedButton(binding.buttonMKD)
            viewModel.selectedCurrency = getString(R.string.mkd)
        }
        binding.exportBlock.setOnClickListener {
            binding.progBar.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                viewModel.getCsvData(requireContext())
            }
        }
    }

    private fun setSelectedButton(selectedButton: View) = with(binding) {
        buttonAll.isSelected = false
        buttonEUR.isSelected = false
        buttonRSD.isSelected = false
        buttonMKD.isSelected = false

        selectedButton.isSelected = true
    }

    private fun triggerUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            val bindingMain = (activity as MainActivity).binding
            MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)
            binding.progBar.visibility = View.GONE

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.getToolHistoryIndex(
                    requireContext(), isInternetAvailable
                )
            }
        }
    }

    private fun setObservers() {
        isInternetAvailable = MutableLiveData()
        isInternetAvailable.observe(viewLifecycleOwner) { hasInternet ->
            if (hasInternet != null && !hasInternet) {

                val bundle = Bundle().apply {
                    putString(getString(R.string.title), getString(R.string.no_connection_title))
                    putString(
                        getString(R.string.subtitle),
                        getString(R.string.please_connect_to_the_internet)
                    )
                }

                findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)

                val binding = (activity as MainActivity).binding
                MainActivity.showSnackMessage(getString(R.string.checking_for_connection), binding)

                lifecycleScope.let {
                    CoroutineScope(Dispatchers.IO).launch {
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

        viewModel.errorBody.observe(viewLifecycleOwner) { errorBody ->
            binding.progBar.visibility = View.GONE
            context?.let { context ->
                Toast.makeText(
                    context, errorBody.errorBody, Toast.LENGTH_SHORT
                ).show()
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
        }

        viewModel.data.observe(viewLifecycleOwner) {
            binding.progBar.visibility = View.GONE
            if (it.data != null) {
                if (!it.data?.tags.isNullOrEmpty()) {
                    binding.noData.visibility = View.GONE

                    viewModel.tagSerials = it.data?.tags as ArrayList<Tag>
                    Log.d(TAG, "setObservers: ${viewModel.tagSerials}")

                    val adapter = ToolHistoryTagsAdapter(viewModel.tagSerials, this)

                    binding.cycler.adapter = adapter
                    binding.cycler.layoutManager = LinearLayoutManager(context)
                } else {
                    binding.btnSearch.isEnabled = false
                    binding.noData.visibility = View.VISIBLE
                    Toast.makeText(context, R.string.no_passage_data, Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(context, "Api Error", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.csvData.observe(viewLifecycleOwner) { csvData ->
            binding.progBar.visibility = View.GONE
            csvData?.let {
                vModel.processCsvData(it)
            }
        }
    }

    override fun onSendTag(tag: Tag) {
        viewModel.selectedTags.add(tag)
        Log.d(TAG, "onSendTag: ${viewModel.selectedTags}")
    }

    override fun onTagRemove(tag: Tag) {
        viewModel.selectedTags.remove(tag)
        Log.d(TAG, "onSendTag: ${viewModel.selectedTags}")
    }

    private fun setCheckboxColors(isChecked: Boolean) {
        if (isChecked) {
            binding.chkBox.buttonTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.figmaSplashScreenColor
                )
            )
        } else {
            binding.chkBox.buttonTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primary_light_dark
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}