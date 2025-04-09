package com.mobility.enp.view.fragments.my_profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tags.PostLostTag
import com.mobility.enp.data.model.api_tags.TagFilterData
import com.mobility.enp.data.model.api_tags.TagStatus
import com.mobility.enp.data.model.api_tags.TagsResponse
import com.mobility.enp.databinding.FragmentTagsBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.view.MainActivity
import com.mobility.enp.view.adapters.tags.AdapterTagFilterType
import com.mobility.enp.view.adapters.tags.MyTagsAdapter
import com.mobility.enp.view.dialogs.LostTagDialog
import com.mobility.enp.viewmodel.FranchiseViewModel
import com.mobility.enp.viewmodel.MyTagsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyTagsFragment : Fragment(), AdapterTagFilterType.OnClick, MyTagsAdapter.OnClickContent {

    private var _binding: FragmentTagsBinding? = null
    private val binding get() = _binding!!
    private val franchiseViewModel: FranchiseViewModel by activityViewModels { FranchiseViewModel.Factory }
    private val viewModel: MyTagsViewModel by viewModels()

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

        setObservers()
        setListeners()
        setFranchiser()

        viewLifecycleOwner.lifecycleScope.launch {
            binding.progbar.visibility = View.VISIBLE
            viewModel.getTagsApiData(requireContext())
        }

        binding.buttonAddTag.setOnClickListener {
            findNavController().navigate(R.id.action_myTagsFragment2_to_addTagFragment)
        }
    }

    private fun setFranchiser() {
        franchiseViewModel.franchiseModel.observe(viewLifecycleOwner){franchiseModel ->
            franchiseModel?.franchisePrimaryColor?.let { color ->
                binding.buttonAddTag.backgroundTintList = ColorStateList.valueOf(color)
                binding.inputSerialNumber.boxStrokeColor = color
                binding.editSerialNumberMyTags.setTextColor(ColorStateList.valueOf(color))
                val parent = binding.constraintLayout

                for (i in 0 until parent.childCount) {
                    val view = parent.getChildAt(i)
                    if (view is TextInputLayout) {
                        view.boxStrokeColor = color
                        val editText = view.editText
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

                        view.cursorColor = ColorStateList(states, colors)
                    }
                }
            }
        }
    }

    private fun setListeners() {
        binding.editSerialNumberMyTags.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "before method : ${viewModel.tagApiData.value}")

                val data = viewModel.newFilterLogic(s.toString())

                Log.d(TAG, "after method : ${viewModel.tagApiData.value}")

                data?.let { filteredBySerial ->
                    try {
                        (binding.cyclerTagTypes.adapter as AdapterTagFilterType).triggerClearByPosition(
                            -1
                        )

                        if (filteredBySerial.data.tags.isEmpty()) {
                            context?.let {
                                Toast.makeText(
                                    it,
                                    getString(R.string.tags_with_serial_not_found),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        if (filteredBySerial.data.tags.size == viewModel.tagApiData.value?.data?.tags?.size) {
                            (binding.cyclerTagTypes.adapter as AdapterTagFilterType).triggerClearByPosition(
                                0
                            )
                        }

                        (binding.cyclerContent.adapter as MyTagsAdapter).updateListTags(
                            filteredBySerial
                        )
                    } catch (e: Exception) {
                        Log.d(TAG, "onTextChanged: ${e.message}")
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun triggerUpdate() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val bindingMain = (activity as MainActivity).binding
            MainActivity.showSnackMessage(getString(R.string.connection_restored), bindingMain)
            binding.progbar.visibility = View.GONE
            binding.buttonAddTag.isEnabled = true

            withContext(Dispatchers.IO) {
                context?.let { context ->
                    viewModel.getTagsApiData(context)
                }
            }
        }
    }

    private fun setObservers() {
        viewModel.isInternetAvailable.observe(viewLifecycleOwner) { hasInternet ->
            if (hasInternet != null && !hasInternet) {
                binding.buttonAddTag.isEnabled = false

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

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    while (!Repository.isNetworkAvailable(requireContext())) {
                        delay(1000L)
                    }
                    triggerUpdate()
                }
            }
        }

        viewModel.lostTag.observe(viewLifecycleOwner) {
            Log.d(TAG, "setObservers: $it")
            if (it != null) {
                binding.progbar.visibility = View.VISIBLE

                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    delay(5000)  // server delay
                    viewModel.getTagsApiData(requireContext())
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.reported_lost_tag_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        viewModel.foundTag.observe(viewLifecycleOwner) {
            it?.let {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(5000)
                    viewModel.getTagsApiData(requireContext())

                    withContext(Dispatchers.Main) {
                        binding.progbar.visibility = View.GONE

                        Toast.makeText(
                            requireContext(),
                            getString(R.string.reported_found_tag_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        viewModel.tagApiData.observe(viewLifecycleOwner) { data ->
            Log.d(TAG, "tag: $data")
            if (data != null) {
                val list = data.data.tags.flatMap { tag ->
                    tag.statuses.map { status ->
                        TagStatus(
                            tag.id,
                            tag.serialNumber,
                            tag.registrationPlate,
                            tag.country,
                            tag.category,
                            status,
                            tag.showButtonLostTag,
                            tag.showButtonFoundTag
                        )
                    }
                }

                viewModel.allTagsFiltered = list

                if (list.isEmpty()) {
                    binding.textNoMyTags.visibility = View.VISIBLE
                    binding.myTagsContainer.visibility = View.GONE
                } else {
                    binding.textNoMyTags.visibility = View.GONE
                    binding.myTagsContainer.visibility = View.VISIBLE
                }

                binding.progbar.visibility = View.GONE
                setTagFilters()
                setTagAdapterContent(data)
            }
        }

        viewModel.errorBody.observe(viewLifecycleOwner) { errorBody ->
            context?.let { context ->
                Toast.makeText(
                    context, errorBody.errorBody, Toast.LENGTH_SHORT
                ).show()
                if (errorBody.errorCode == 405 || errorBody.errorCode == 401) {
                    MainActivity.logoutOnInvalidToken(context, findNavController())
                }
            }
        }
    }

    private fun setTagFilters() {
        binding.cyclerTagTypes.adapter = AdapterTagFilterType(
            ArrayList(viewModel.allTagsFiltered), requireContext()
        )
        (binding.cyclerTagTypes.adapter as AdapterTagFilterType).setInterface(this)
        binding.cyclerTagTypes.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setTagAdapterContent(data: TagsResponse) {
        binding.cyclerContent.adapter = MyTagsAdapter(data)
        binding.cyclerContent.layoutManager = LinearLayoutManager(requireContext())
        (binding.cyclerContent.adapter as MyTagsAdapter).setInterface(this)
    }

    override fun send(filterType: TagFilterData) {
        Log.d(TAG, "send: $filterType")
        binding.editSerialNumberMyTags.setText("")

        val data = viewModel.newFilterLogic(filterType.tagValue)

        data?.let {
            (binding.cyclerContent.adapter as MyTagsAdapter).updateListTags(it)
        }
    }

    override fun sendClickedPosition(position: Int) {
        (binding.cyclerTagTypes.adapter as AdapterTagFilterType).triggerClearByPosition(position)
    }

    override fun reportLostTag(tagSerial: String?) {
        val lostTagDialog = LostTagDialog(
            requireContext().getString(R.string.confirm_lost_tag),
            requireContext().getString(R.string.dialog_lost_tag_message),
            object : LostTagDialog.OnButtonClickInLostTag {
                override fun onClickConfirmed() {
                    tagSerial?.let { serial ->
                        val body = PostLostTag(serial)
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.postLostTag(body)
                        }
                    }
                }
            })
        lostTagDialog.show(childFragmentManager, "LostTagDialog")
    }

    override fun reportFoundTag(tagSerial: String?) {
        val lostTagDialog = LostTagDialog(
            requireContext().getString(R.string.confirm_found_tag),
            requireContext().getString(R.string.report_found_tag),
            object : LostTagDialog.OnButtonClickInLostTag {
                override fun onClickConfirmed() {
                    tagSerial?.let { serial ->
                        val body = PostLostTag(serial)

                        binding.progbar.visibility = View.VISIBLE

                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            viewModel.postFoundTag(body)
                        }
                    }
                }
            })
        lostTagDialog.show(childFragmentManager, "FoundTagDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}