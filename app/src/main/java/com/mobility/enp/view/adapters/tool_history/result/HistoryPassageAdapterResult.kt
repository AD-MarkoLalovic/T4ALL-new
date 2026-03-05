package com.mobility.enp.view.adapters.tool_history.result

import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.v2base_model.DataValidation
import com.mobility.enp.data.model.api_tool_history.v2base_model.Item
import com.mobility.enp.data.model.api_tool_history.v2base_model.SumTag
import com.mobility.enp.databinding.ItemRelationPassageRealBinding
import com.mobility.enp.util.toSumTagsByCurrency
import com.mobility.enp.view.dialogs.ComplaintFormDialog
import com.mobility.enp.view.dialogs.ComplaintFormDialogOld
import com.mobility.enp.view.dialogs.GeneralMessageDialog
import com.mobility.enp.view.dialogs.ObjectionFormDialog
import com.mobility.enp.viewmodel.UserPassViewModel
import kotlinx.coroutines.launch

class HistoryPassageAdapterResult(
    private val complaintInterface: SendToFragment,
    private val hideComplaintButton: Boolean,
    private val lifecycleOwner: LifecycleOwner,
    private val tagSerialNumber: String,
    private val countryCode: String, private val viewmodel: UserPassViewModel,
    private val onInitDataSize: (Int) -> Unit,
    private val onSumTags: (List<SumTag>) -> Unit
) : ListAdapter<Item, HistoryPassageAdapterResult.RelationViewHolder>(DIFF_CALLBACK) {

    private lateinit var context: Context
    private var totalPages: Int = 0
    private var currentPage: Int = 0
    private var lastPage: Int = 0

    init {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.getV2PassagesBySerialAndCountryCodeResult(tagSerialNumber, countryCode)
                    .collect { data ->
                        if (data.isNotEmpty()) {
                            totalPages = data.size

                            currentPage = data[data.size - 1]?.currentPage ?: 0
                            lastPage = data[data.size - 1]?.lastPage ?: 0

                            if (data.isNotEmpty()) { // sum of tags
                                onInitDataSize(data[0]?.data?.records?.items?.size ?: 0)
                            }
                            val newList = buildList {
                                data.forEach { passage ->
                                    passage?.data?.records?.items?.let {
                                        addAll(it)
                                    }
                                }
                            }

                            submitList(newList)

                            onSumTags(newList.toSumTagsByCurrency())
                        }
                    }
            }
        }

        viewmodel.getToolHistoryTransitResult(tagSerialNumber, 1)
        if (totalPages > 1) {
            viewmodel.getSerialPassageTagDataValidationResult(
                totalPages,
                tagSerialNumber,
                countryCode
            )
        }
    }

    companion object {
        const val TAG = "PassageAdapter"

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Item>() {

            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class RelationViewHolder(
        private val context: Context,
        private val binding: ItemRelationPassageRealBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(relation: Item, complaintInterface: SendToFragment) {

            val formatedCheckInDate = viewmodel.formatPassageDate(relation.checkInDate)
            val formatedCheckOutDate = viewmodel.formatPassageDate(relation.checkOutDate)

            if (!formatedCheckInDate.isNullOrEmpty() && !formatedCheckOutDate.isNullOrEmpty()) {
                relation.checkInDate = formatedCheckInDate
                relation.checkOutDate = formatedCheckOutDate
            }

            val dataValidation = DataValidation(
                totalPages, tagSerialNumber, countryCode
            )

            binding.objectionsNumber.text = ""

            binding.relation = relation
            binding.viewShade.background = null

            when (relation.bill.countryCode) {
                "RS" -> {
                    binding.tagBillCountry.text = "SRB"
                }

                "ME" -> {
                    binding.tagBillCountry.text = "MNE"
                }

                "MK" -> {
                    binding.tagBillCountry.text = "MKD"
                }

                "HR" -> {
                    binding.tagBillCountry.text = "HRV"
                }

                else -> {
                    binding.tagBillCountry.text = ""
                }
            }

            binding.btnComplaint.setOnClickListener {

                if (countryCode.isNotEmpty() && countryCode == "RS") {

                    val fragmentManager = (context as AppCompatActivity).supportFragmentManager

                    val dialog = ComplaintFormDialog.newInstance(
                        relation.id
                    ) { complaintBody ->
                        complaintInterface.sendComplaintData(complaintBody, dataValidation)
                    }

                    dialog.show(fragmentManager, "ComplaintFormDialog")

                } else if (countryCode.isNotEmpty() && countryCode != "RS") {
                    val fragmentManager = (context as AppCompatActivity).supportFragmentManager

                    val dialog = ComplaintFormDialogOld.newInstance(
                        relation.id
                    ) { complaintBody ->
                        complaintInterface.sendComplaintData(complaintBody, dataValidation)
                    }

                    dialog.show(fragmentManager, "ComplaintFormDialogOld")
                } else {
                    Toast.makeText(binding.root.context, "Country Code Issue", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            binding.btnObjection.setOnClickListener {
                val fragmentManager = (context as AppCompatActivity).supportFragmentManager

                if (relation.complaint != null) {
                    if (!relation.complaint.objections.isNullOrEmpty() && relation.complaint.objections.size >= 2) {
                        GeneralMessageDialog.newInstance(
                            title = binding.root.context.getString(R.string.contact_support),
                            subtitle = binding.root.context.getString(R.string.limit_reclamation)
                        ).show(fragmentManager, "denyComplaint")
                    } else {
                        val fragmentManager = (context as AppCompatActivity).supportFragmentManager

                        val dialog = ObjectionFormDialog.newInstance(
                            relation.complaint.id
                        ) { complaintBody ->
                            complaintInterface.sendObjectionData(complaintBody, dataValidation)
                        }

                        dialog.show(fragmentManager, "ObjectionFormDialog")
                    }
                }
            }

            if (relation.complaint != null) {  // ignore recommendation ide is wrong
                binding.complaintId.visibility = View.VISIBLE
                val text = context.getString(R.string.br_registration)
                binding.complaintId.text = buildString {
                    append(text)
                    append(" ")
                    append(relation.complaint.id)
                }

                binding.btnComplaint.visibility = View.GONE
                binding.container.visibility = View.VISIBLE  // shows objection button

                if (!relation.complaint.objections.isNullOrEmpty()) {
                    val buttonText = context.getString(R.string.objection)
                    binding.btnObjection.text = buttonText
                    binding.objectionsNumber.text = relation.complaint.objections.size.toString()
                    if (relation.complaint.objections.isEmpty()) {
                        binding.viewShade.background = null
                    } else {
                        binding.viewShade.background = AppCompatResources.getDrawable(
                            context,
                            R.drawable.figma_objection_background
                        )
                    }
                } else {
                    val buttonText = context.getString(R.string.objection)
                    binding.btnObjection.text = buttonText
                }

                binding.executePendingBindings()

            } else {
                binding.complaintId.visibility = View.GONE
                binding.container.visibility = View.GONE

                binding.btnComplaint.visibility = View.VISIBLE
            }

            when (relation.bill.paid.toInt()) {
                1 -> {
                    binding.toolHistoryStatus.setBackgroundResource(R.drawable.status_icon_green)
                    binding.topContainer.setBackgroundResource(R.drawable.tool_history_top_green)
                    binding.bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_green)
                }

                3,  // unpaid
                0 -> {
                    binding.toolHistoryStatus.setBackgroundResource(R.drawable.status_icon_red)
                    binding.topContainer.setBackgroundResource(R.drawable.tool_history_top_red)
                    binding.bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_red)
                }

                7 -> {
                    binding.toolHistoryStatus.setBackgroundResource(R.drawable.status_icon_orange)
                    if (isTabletXml(binding.root.context)) {
                        binding.topContainer.setBackgroundResource(R.drawable.tool_history_top_orange_tablet)
                        binding.bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_orange_tablet)
                    } else {
                        binding.topContainer.setBackgroundResource(R.drawable.tool_history_top_orange)
                        binding.bottomContainer.setBackgroundResource(R.drawable.tool_history_bottom_orange)
                    }
                }
            }

            if (hideComplaintButton) {
                binding.btnComplaint.visibility = View.GONE
                binding.complaintId.visibility = View.GONE
                binding.container.visibility = View.GONE
            }

            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationViewHolder {
        context = parent.context
        return RelationViewHolder(
            context,
            ItemRelationPassageRealBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }


    override fun onBindViewHolder(holder: RelationViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, complaintInterface)
        runPaginationCheck(currentItem)
    }

    private fun runPaginationCheck(currentItem: Item) {
        if (currentItem == getItem(itemCount - 1)) {
            if (currentPage < lastPage) {
                // trigger background update with flow
                viewmodel.getToolHistoryTransitResult(tagSerialNumber, currentPage + 1)
            }
        }
    }

    interface SendToFragment {
        fun sendComplaintData(complaintBody: ComplaintBody, dataValidation: DataValidation)
        fun sendObjectionData(objectionBody: ObjectionBody, dataValidation: DataValidation)
        fun stopSpinner()
    }

    private fun isTabletXml(context: Context): Boolean {
        val config: Configuration = context.resources.configuration
        val smallestScreenWidthDp: Int = config.smallestScreenWidthDp
        return smallestScreenWidthDp >= 600 // min layout with for tablet
    }
}