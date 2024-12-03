package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.ToolHistoryListing
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.FragmentPassageHistoryBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.main_screen.ToolHistoryListingAdapter
import com.mobility.enp.view.adapters.tool_history.main_screen.ToolHistoryListingPassageAdapter
import com.mobility.enp.viewmodel.PassageHistoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ToolHistoryMainFragment : Fragment(), ToolHistoryListingPassageAdapter.SendToFragment,
    ToolHistoryListingAdapter.SavePassageData {

    private var _binding: FragmentPassageHistoryBinding? = null
    private val binding: FragmentPassageHistoryBinding get() = _binding!!
    private val viewModel: PassageHistoryViewModel by activityViewModels()

    private var data: MutableLiveData<IndexData> = MutableLiveData<IndexData>()
    private lateinit var isInternetAvailable: MutableLiveData<Boolean>

    companion object {
        const val TAG = "ToolHist"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPassageHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.nullDates()

        context?.let {
            binding.progBar.visibility = View.VISIBLE
            setObservers()

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.getToolHistoryIndex(
                    data, it, isInternetAvailable
                )
            }
        }

        binding.loopIcon.setOnClickListener {
            if (Repository.isNetworkAvailable(requireContext())) {
                findNavController().navigate(ToolHistoryMainFragmentDirections.actionToolHistoryFragmentToToolHistorySearchFragment())
            } else {
                Toast.makeText(
                    context, context?.getString(R.string.no_internet), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun triggerUpdate() {
        CoroutineScope(Dispatchers.Main).launch {
            val bindingMain = (activity as MainActivity).binding
            MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)
            binding.progBar.visibility = View.GONE
            binding.loopIcon.isEnabled = true

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
                binding.loopIcon.isEnabled = false

                var indexData: IndexData? = null

                runBlocking {
                    val diff = CoroutineScope(Dispatchers.IO).async {
                        indexData = viewModel.fetchIndexData()
                    }

                    diff.await()
                }

                if (indexData != null) {
                    val bindingMain = (activity as MainActivity).binding
                    MainActivity.showSnackMessage(
                        getString(R.string.offline_using_stored_data), bindingMain
                    )

                    indexData?.let { iData ->
                        data.postValue(iData)
                    }
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

                    val binding = (activity as MainActivity).binding
                    MainActivity.showSnackMessage(
                        getString(R.string.checking_for_connection), binding
                    )

                }

                viewLifecycleOwner.lifecycleScope.launch {
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
            if (it != null) {

                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.insertRoomToolHistoryIndexData(it)
                }

                viewModel.tagSerials = it.data?.tags as ArrayList<Tag>

                val toolHistoryListingAdapter =
                    ToolHistoryListingAdapter(it, viewModel, this, this, this)

                binding.cycler.adapter = toolHistoryListingAdapter
                binding.cycler.layoutManager = LinearLayoutManager(requireContext())

                // set first adapter here // reason we can have multiple tags which further split to sub adapters with their own combination of tag serial and tool history transit calls and pagination on top
            } else {
                Toast.makeText(context, "Api Error", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.complaintResponse.observe(viewLifecycleOwner) {
            binding.progBar.visibility = View.GONE
            it?.let {
                Toast.makeText(
                    context, getString(R.string.registered_complaint), Toast.LENGTH_SHORT
                ).show()
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getToolHistoryIndex(
                        data, requireContext(), isInternetAvailable
                    )
                }
            }
        }
    }

    override fun sendComplaintData(complaintBody: ComplaintBody) {
        binding.progBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.postComplaint(complaintBody)
        }
    }

    override fun sendObjectionData(objectionBody: ObjectionBody) {
        binding.progBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.postObjection(objectionBody)
        }
    }

    override fun sendDataFill(
        nextPage: Int, dataFill: MutableLiveData<ToolHistoryListing>, tagSerialNumber: String
    ) {
        binding.progBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.getToolHistoryListingMutable(dataFill, tagSerialNumber, nextPage)
        }
    }

    override fun stopSpinner() {
        binding.progBar.visibility = View.GONE
    }

    override fun psgData(toolHistoryListing: ToolHistoryListing) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                viewModel.insertPassageData(toolHistoryListing)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}