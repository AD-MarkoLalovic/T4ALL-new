package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.FragmentToolHistorySearchQueryBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.select.ToolHistoryTagsAdapter
import com.mobility.enp.viewmodel.PassageHistoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ToolHistoryFilterFragment : Fragment(), ToolHistoryTagsAdapter.TagSend {

    private lateinit var binding: FragmentToolHistorySearchQueryBinding
    private val viewModel: PassageHistoryViewModel by activityViewModels()
    private var data: MutableLiveData<IndexData> = MutableLiveData<IndexData>()
    private lateinit var isInternetAvailable: MutableLiveData<Boolean>

    companion object {
        const val TAG = "ToolDetails"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
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
                    data, it, isInternetAvailable
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
            viewModel.allTagsSelected = binding.chkBox.isChecked
        }

        viewModel.startDate.observe(viewLifecycleOwner) { data ->
            binding.txtDateLeft.text = data.formattedTime
        }
        viewModel.endDate.observe(viewLifecycleOwner) { data ->
            binding.txtDateRight.text = data.formattedTime
        }

        binding.rlLeft.setOnClickListener {
            context?.let { context ->
                viewModel.showDatePicker(true, context)
            }
        }
        binding.rlRight.setOnClickListener {
            context?.let { context ->
                viewModel.showDatePicker(false, context)
            }
        }

        val listButtons =
            listOf(binding.buttonAll, binding.buttonEUR, binding.buttonRSD, binding.buttonMKD)

        binding.buttonAll.setOnClickListener {
            removeColors(listButtons)
            setColors(it)
            viewModel.selectedCurrency = ""
        }
        binding.buttonRSD.setOnClickListener {
            removeColors(listButtons)
            setColors(it)
            viewModel.selectedCurrency = getString(R.string.rsd)
        }
        binding.buttonEUR.setOnClickListener {
            removeColors(listButtons)
            setColors(it)
            viewModel.selectedCurrency = getString(R.string.eur)
        }
        binding.buttonMKD.setOnClickListener {
            removeColors(listButtons)
            setColors(it)
            viewModel.selectedCurrency = getString(R.string.mkd)
        }
        binding.exportBlock.setOnClickListener {
            binding.progBar.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                viewModel.getCsvData(requireContext())
            }
        }
        setColors(binding.buttonAll)
    }


    private fun setColors(view: View) {
        val button = (view as Button)
        button.setBackgroundResource(R.drawable.rounded_status_marked_border)
        button.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.primary_light_dark)
        button.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.white
            )
        )
    }

    private fun removeColors(view: List<View>) {
        view.forEachIndexed { _, buttonView ->
            val button = (buttonView as Button)
            button.setBackgroundResource(R.drawable.button_outline)
            button.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(), R.color.figmaIntroEclipseColorInactive
            )
            button.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.primary_light_darkest
                )
            )
        }
    }

    private fun triggerUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            val bindingMain = (activity as MainActivity).binding
            MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)
            binding.progBar.visibility = View.GONE

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.getToolHistoryIndex(
                    data, requireContext(), isInternetAvailable
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

        data = MutableLiveData<IndexData>()
        data.observe(viewLifecycleOwner) {
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
                viewModel.processCsvData(it)
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

}