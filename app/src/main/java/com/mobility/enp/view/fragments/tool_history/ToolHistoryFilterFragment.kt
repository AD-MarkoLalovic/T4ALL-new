package com.mobility.enp.view.fragments.tool_history

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.index.IndexData
import com.mobility.enp.data.model.api_tool_history.index.Tag
import com.mobility.enp.databinding.FragmentToolHistorySearchQueryBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestLifecycleFlow
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tool_history.select.ToolHistoryTagsAdapter
import com.mobility.enp.view.dialogs.NotificationsRequestDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class ToolHistoryFilterFragment : Fragment(), ToolHistoryTagsAdapter.TagSend {

    private var _binding: FragmentToolHistorySearchQueryBinding? = null
    private val binding: FragmentToolHistorySearchQueryBinding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val vModel: UserPassViewModel by activityViewModels { UserPassViewModel.Factory }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission is granted. You can proceed with sending notifications.
                userPerm.onPermissionGranted()
                sendNotification()
            }
        }
    private lateinit var userPerm: UserPermission

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
        setFranchiser()

        vModel.selectedTags.clear()

        binding.progBar.visibility = View.VISIBLE

        if (vModel.internetAvailable()) {
            vModel.getBaseData()
        } else {
            checkInternet()
        }

        binding.btnSearch.setOnClickListener {
            if (vModel.selectedTags.isEmpty() && !vModel.allTagsSelected) {
                Toast.makeText(context, R.string.please_select_tag, Toast.LENGTH_SHORT).show()
            } else {
                if (vModel.internetAvailable()) {
                    findNavController().navigate(ToolHistoryFilterFragmentDirections.actionToolHistorySearchFragmentToToolHistorySearchResultFragment())
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
                }
            }
        }

        binding.chkBox.setOnClickListener {
            val isChecked = binding.chkBox.isChecked
            vModel.allTagsSelected = isChecked
        }

        vModel.startDate.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val textView = binding.txtDateLeft as TextView
                textView.text = data.formattedTime
            }
        }

        vModel.endDate.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val textEndDate = binding.txtDateRight as TextView
                textEndDate.text = data.formattedTime
            }
        }

        binding.txtDateLeft.setOnClickListener {
            vModel.showDatePicker(true, requireContext(), franchiseViewModel.franchiseModel.value)
        }

        binding.txtDateRight.setOnClickListener {
            vModel.showDatePicker(false, requireContext(), franchiseViewModel.franchiseModel.value)
        }

        setSelectedButton(binding.buttonAll)

        binding.buttonAll.setOnClickListener {
            setSelectedButton(binding.buttonAll)
            vModel.selectedCurrency = ""
        }

        binding.buttonMontenegro.setOnClickListener {
            setSelectedButton(binding.buttonMontenegro)
            vModel.selectedCurrency = getString(R.string.rsd)
        }

        binding.buttonSerbia.setOnClickListener {
            setSelectedButton(binding.buttonSerbia)
            vModel.selectedCurrency = getString(R.string.eur)
        }

        binding.buttonCroatia.setOnClickListener {
            setSelectedButton(binding.buttonCroatia)
            vModel.selectedCurrency = getString(R.string.mkd)
        }

        binding.exportBlock.setOnClickListener {
            binding.progBar.visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                vModel.getCsvData(requireContext())
            }
        }
    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner) { franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.btnSearch.backgroundTintList = ColorStateList.valueOf(color)
                binding.exportBlock.setTextColor(color)
                binding.exportBlock.visibility =
                    View.GONE // because frashizers can not export from what daniel told me

                binding.searchMark.setImageResource(franchiseModel.loopIcon)

                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),  // When switch is ON
                    intArrayOf(-android.R.attr.state_checked) // When switch is OFF
                )

                val colors = intArrayOf(
                    color,  // ON color
                    ContextCompat.getColor(
                        requireContext(), R.color.primary_light_dark
                    ) // OFF color
                )

                val colorStateList = ColorStateList(states, colors)
                binding.chkBox.buttonTintList = colorStateList
            } ?: run {
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),  // When switch is ON
                    intArrayOf(-android.R.attr.state_checked) // When switch is OFF
                )

                val colors = intArrayOf(
                    ContextCompat.getColor(
                        requireContext(), R.color.figmaSplashScreenColor
                    ),  // ON color
                    ContextCompat.getColor(
                        requireContext(), R.color.primary_light_dark
                    ) // OFF color
                )

                val colorStateList = ColorStateList(states, colors)
                binding.chkBox.buttonTintList = colorStateList
            }
        }
    }

    private fun setSelectedButton(selectedButton: View) = with(binding) {
        buttonAll.isSelected = false
        buttonSerbia.isSelected = false
        buttonMontenegro.isSelected = false
        buttonCroatia.isSelected = false

        selectedButton.isSelected = true
    }

    private fun triggerUpdate() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val bindingMain = (activity as MainActivity).binding
            MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)
            binding.progBar.visibility = View.GONE

            vModel.getBaseData()
        }
    }

    private fun setObservers() {

        vModel.errorBody.observe(viewLifecycleOwner) { errorBody ->
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

        collectLatestLifecycleFlow(vModel.baseTagDataState) { tagIndex ->
            when (tagIndex) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE
                    setIndexData(tagIndex.data.first)
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
                    showError(tagIndex.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(tagIndex.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {
                    SubmitResult.Empty
                }
            }
        }

        collectLatestLifecycleFlow(vModel.csvTable) { csvTable ->
            when (csvTable) {
                is SubmitResult.Loading -> {
                    binding.progBar.visibility = View.VISIBLE
                }

                is SubmitResult.Success -> {
                    binding.progBar.visibility = View.GONE

                    if (!csvTable.data.data?.csvContent.isNullOrEmpty()) {
                        val nameExtra = UUID.randomUUID().toString().substring(0, 8)
                        vModel.processCsvData(csvTable.data, nameExtra, requireContext())
                        when {
                            ContextCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                csvTable.data.data?.csvContent?.let {
                                    vModel.saveBase64ToCSV(
                                        it, nameExtra, requireContext()
                                    ) // <- converts csv to pdf saves locally and in room byte array
                                }
                                vModel.setCsvState()
                            }

                            else -> {
                                showNotificationPermissionRationale(object : UserPermission {
                                    override fun onPermissionGranted() {
                                        csvTable.data.data?.csvContent?.let {
                                            vModel.saveBase64ToCSV(
                                                it, nameExtra, requireContext()
                                            )
                                        }
                                        vModel.setCsvState()
                                    }

                                    override fun onPermissionDenied() {
                                        vModel.setCsvState()
                                    }
                                })
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.no_passage_data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
                    showError(csvTable.errorMessage)
                }

                is SubmitResult.InvalidApiToken -> {
                    showError(csvTable.errorMessage)
                    MainActivity.logoutOnInvalidToken(requireContext(), findNavController())
                }

                else -> {
                    SubmitResult.Empty
                }
            }
        }
    }


    private fun checkInternet() {
        if (!vModel.internetAvailable()) {
            val bundle = Bundle().apply {
                putString(getString(R.string.title), getString(R.string.no_connection_title))
                putString(
                    getString(R.string.subtitle), getString(R.string.please_connect_to_the_internet)
                )
            }

            findNavController().navigate(R.id.action_global_noInternetConnectionDialog, bundle)

            val binding = (activity as MainActivity).binding
            MainActivity.showSnackMessage(getString(R.string.checking_for_connection), binding)

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                while (true) {
                    if (vModel.internetAvailable()) {
                        triggerUpdate()
                        break
                    } else {
                        delay(1000L)
                    }
                }
            }
        }
    }


    private fun showNotificationPermissionRationale(userPermission: UserPermission) {
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

    private fun sendNotification() {
        Toast.makeText(requireContext(), getString(R.string.permission_granted), Toast.LENGTH_SHORT)
            .show()
    }

    private fun setIndexData(indexData: IndexData) {
        indexData.data?.let { index ->
            if (!index.tags.isNullOrEmpty()) {
                binding.noData.visibility = View.GONE

                vModel.tagSerials = index.tags as ArrayList<Tag>
                Log.d(TAG, "setObservers: ${vModel.tagSerials}")

                val adapter = ToolHistoryTagsAdapter(vModel.tagSerials, this, franchiseViewModel)

                binding.cycler.adapter = adapter
                binding.cycler.layoutManager = LinearLayoutManager(context)
            } else {
                binding.btnSearch.isEnabled = false
                binding.noData.visibility = View.VISIBLE
                Toast.makeText(context, R.string.no_passage_data, Toast.LENGTH_SHORT).show()
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


    override fun onSendTag(tag: Tag) {
        vModel.selectedTags.add(tag)
        Log.d(TAG, "onSendTag: ${vModel.selectedTags}")
    }

    override fun onTagRemove(tag: Tag) {
        vModel.selectedTags.remove(tag)
        Log.d(TAG, "onSendTag: ${vModel.selectedTags}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface UserPermission {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

}