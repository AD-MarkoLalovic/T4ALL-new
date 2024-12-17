package com.mobility.enp.view.fragments.tool_history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
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
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.main_screen.ToolHistoryListingAdapter
import com.mobility.enp.view.adapters.tool_history.main_screen.ToolHistoryListingPassageAdapter
import com.mobility.enp.viewmodel.PassageHistoryViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
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
    private val vModel: UserPassViewModel by viewModels { UserPassViewModel.Factory }

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
        binding.progBar.visibility = View.VISIBLE

        setObservers()

        vModel.getIndexData()

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

            vModel.getIndexData()
        }
    }

    private fun setObservers() {

        collectLatestLifecycleFlow(vModel.baseTagDataState) { tagIndex ->
            when (tagIndex) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    setIndexData(tagIndex.data)
                }

                is SubmitResult.FailureNoConnection -> {
                    showNoConnectionState()
                }

                is SubmitResult.FailureServerError -> {
                    binding.progBar.visibility = View.GONE
                    showError(getString(R.string.server_error_msg))
                }

                is SubmitResult.FailureApiError -> {
                    binding.progBar.visibility = View.GONE
                    showError(getString(R.string.api_call_error))
                }

                else -> {
                    SubmitResult.Empty
                }
            }
        }

        isInternetAvailable = MutableLiveData()
        isInternetAvailable.observe(viewLifecycleOwner) { hasInternet ->
            if (hasInternet != null && !hasInternet) {
                binding.loopIcon.isEnabled = false

                var indexData: IndexData? = null

                runBlocking {
                    val diff = CoroutineScope(Dispatchers.IO).async {
                        indexData = vModel.fetchIndexData()   // room
                    }

                    diff.await()
                }

                if (indexData != null) {
                    val bindingMain = (activity as MainActivity).binding
                    MainActivity.showSnackMessage(
                        getString(R.string.offline_using_stored_data), bindingMain
                    )

                    indexData?.let { iData ->
                        vModel.setStateIndex(iData)
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

        viewModel.complaintResponse.observe(viewLifecycleOwner) {
            binding.progBar.visibility = View.GONE
            it?.let {
                Toast.makeText(
                    context, getString(R.string.registered_complaint), Toast.LENGTH_SHORT
                ).show()
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getToolHistoryIndex(
                        requireContext(), isInternetAvailable
                    )
                }
            }
        }
    }

    private fun setIndexData(indexData: IndexData) {
        binding.progBar.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.insertRoomToolHistoryIndexData(indexData)
        }

        viewModel.tagSerials = indexData.data?.tags as ArrayList<Tag>

        val toolHistoryListingAdapter =
            ToolHistoryListingAdapter(indexData, viewModel, this, this, this)

        binding.cycler.adapter = toolHistoryListingAdapter
        binding.cycler.layoutManager = LinearLayoutManager(requireContext())

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

    private fun showNoConnectionState() {
        binding.progBar.visibility = View.GONE
        noInternetMessage()
    }

    private fun noInternetMessage() {
        val mainBinding = (activity as MainActivity).binding
        MainActivity.showSnackMessage(getString(R.string.no_internet), mainBinding)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}