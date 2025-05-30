package com.mobility.enp.view.adapters.tool_history.main_screen

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.api_tool_history.complaint.ComplaintBody
import com.mobility.enp.data.model.api_tool_history.complaint.ObjectionBody
import com.mobility.enp.data.model.api_tool_history.listing.InvoiceRelation
import com.mobility.enp.data.model.api_tool_history.listing.ToolHistoryListing
import com.mobility.enp.data.model.api_tool_history.v2base_model.Item
import com.mobility.enp.data.model.api_tool_history.v2base_model.V2HistoryTagResponse
import com.mobility.enp.databinding.ItemRelationPassageRealBinding
import com.mobility.enp.network.Repository
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestFlow
import com.mobility.enp.view.dialogs.ComplaintFormDialog
import com.mobility.enp.view.dialogs.ComplaintFormDialogOld
import com.mobility.enp.view.dialogs.ObjectionFormDialog
import kotlinx.coroutines.flow.MutableStateFlow

class ToolHistoryListingPassageAdapter(
    private val data: V2HistoryTagResponse,
    private val complaintInterface: SendToFragment,
    private val hideComplaintButton: Boolean,
    private val lifecycleOwner: LifecycleOwner,
    private val tagSerialNumber: String,
    private val countryCode: String
) :
    RecyclerView.Adapter<ToolHistoryListingPassageAdapter.RelationViewHolder>() {

    private lateinit var context: Context

    private var currentPage = data.data?.records?.pagination?.currentPage ?: 1
    private val lastPage = data.data?.records?.pagination?.lastPage ?: 1

    private var relation: ArrayList<Item> =
        data.data?.records?.items as ArrayList<Item>

    companion object {
        const val TAG = "PassageAdapter"
    }

    inner class RelationViewHolder(
        private val context: Context,
        private val binding: ItemRelationPassageRealBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(relation: Item, complaintInterface: SendToFragment) {
            binding.relation = relation
            binding.viewShade.background = null
            binding.toolHistoryStatus.setOnClickListener {
                Log.d(TAG, "relation status: $relation")
            }

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

                    val complaintFormDialog = ComplaintFormDialog({ complaintBody ->
                        complaintInterface.sendComplaintData(complaintBody)
                    }, relation.id)

                    complaintFormDialog.show(fragmentManager, "ComplaintFormDialog")
                } else if (countryCode.isNotEmpty() && countryCode != "RS") {
                    val fragmentManager = (context as AppCompatActivity).supportFragmentManager

                    val complaintFormDialog = ComplaintFormDialogOld({ complaintBody ->
                        complaintInterface.sendComplaintData(complaintBody)
                    }, relation.id)

                    complaintFormDialog.show(fragmentManager, "ComplaintFormDialog")
                } else {
                    Toast.makeText(binding.root.context, "Country Code Issue", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            binding.btnObjection.setOnClickListener {
                val fragmentManager = (context as AppCompatActivity).supportFragmentManager

                if (relation.complaint != null) {
                    val objectionDialog =
                        ObjectionFormDialog({ objection ->
                            complaintInterface.sendObjectionData(objection)
                        }, relation.complaint.id)
                    objectionDialog.show(fragmentManager, "ObjectionFormDialog")
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
                binding.container.visibility = View.VISIBLE

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

    override fun getItemCount() = relation.size

    override fun onBindViewHolder(holder: RelationViewHolder, position: Int) {
        val currentItem = relation[holder.bindingAdapterPosition]
        holder.bind(currentItem, complaintInterface)
        if (Repository.isNetworkAvailable(context)) {
            performDataFill(currentItem, holder.bindingAdapterPosition) // paggination
        }
    }

    private fun performDataFill(currentItem: Item, bindingAdapterPosition: Int) {
        if (relation[relation.size - 1] == currentItem && lastPage > currentPage) {
            val indexListing =
                MutableStateFlow<SubmitResult<V2HistoryTagResponse>>(SubmitResult.Loading)

            collectLatestFlow(lifecycleOwner, indexListing) { serverResponse ->
                complaintInterface.stopSpinner()
                when (serverResponse) {
                    is SubmitResult.Success -> {
                        serverResponse.data.let {
                            Log.d(
                                TAG,
                                "performDataFill: ${it.data?.records?.pagination?.currentPage} ${it.data?.records?.pagination?.lastPage}"
                            )
                            currentPage = it.data?.records?.pagination?.currentPage ?: 1

                            for (item: Item in it.data?.records?.items ?: emptyList()) {
                                relation.add(item)
                                notifyItemChanged(relation.size - 1)
                                Log.d(TAG, "dataInserted: $item")
                            }
                        }
                    }

                    else -> {
                        SubmitResult.Empty
                    }
                }
            }

            complaintInterface.sendDataFill(currentPage + 1, indexListing, tagSerialNumber)
        } else if (lastPage == currentPage && relation[relation.size - 1] == currentItem) {
            Log.d(TAG, "performDataFill: no more passage data for tag ${data.serial}")
        }
    }

    interface SendToFragment {
        fun sendComplaintData(complaintBody: ComplaintBody)
        fun sendObjectionData(objectionBody: ObjectionBody)
        fun sendDataFill(
            nextPage: Int,
            flow: MutableStateFlow<SubmitResult<V2HistoryTagResponse>>,
            tagSerialNumber: String
        )

        fun stopSpinner()
    }

    private fun isTabletXml(context: Context): Boolean {
        val config: Configuration = context.resources.configuration
        val smallestScreenWidthDp: Int = config.smallestScreenWidthDp
        return smallestScreenWidthDp >= 600 // min layout with for tablet
    }
}