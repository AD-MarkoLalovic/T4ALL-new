package com.mobility.enp.view.adapters.my_invoices_adapters

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobility.enp.R
import com.mobility.enp.data.model.ErrorBody
import com.mobility.enp.data.model.api_my_invoices.Bill
import com.mobility.enp.data.model.api_my_invoices.BillData
import com.mobility.enp.data.model.api_my_invoices.BillDownload
import com.mobility.enp.data.model.api_my_invoices.BillsDetailsResponse
import com.mobility.enp.databinding.ItemInvoicesBinding
import com.mobility.enp.util.SubmitResult
import com.mobility.enp.util.collectLatestFlow
import com.mobility.enp.view.adapters.tool_history.main_and_filter_screen.ToolHistoryListingAdapter
import com.mobility.enp.viewmodel.MyInvoicesViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class BillsDetailsAdapter(
    private var billData: BillData,
    private val viewModel: MyInvoicesViewModel,
    private val errorBody: MutableLiveData<ErrorBody>,
    private val lifecycleOwner: LifecycleOwner,
    private val spinnerInterface: MonthlyBillsAdapter.TriggerSpinner,
    private val availableCurrencies: String
) :
    RecyclerView.Adapter<BillsDetailsAdapter.BillsViewHolder>() {

    private var billsArray: ArrayList<Bill> = ArrayList(billData.bills)
    private var currentPage = billData.currentPage
    private val lastPage = billData.lastPage
    private var canDownload: Boolean = true

    companion object {
        const val TAG = "BillsAdapter"
    }

    private var countDownTimer: CountDownTimer = object : CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            canDownload = false
            Log.d(TAG, "onTick: $canDownload $millisUntilFinished")
        }

        override fun onFinish() {
            canDownload = true
        }
    }

    inner class BillsViewHolder(val binding: ItemInvoicesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            bill: Bill,
            viewModel: MyInvoicesViewModel
        ) {
            binding.bill = bill

            val billId = bill.id.toString()

            if (bill.status?.value == 1) {

                binding.bttPayNow.apply {
                    visibility = View.INVISIBLE
                    isClickable = false
                }

                binding.invoicesCardView.strokeColor = ContextCompat.getColor(
                    binding.invoicesCardView.context,
                    R.color.figmaToolHistoryPaidBackground
                )

                binding.invoicesStatus.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.invoicesStatus.context,
                        R.drawable.status_icon_green
                    )
                )

                binding.firstContainerInvoice.setBackgroundResource(R.color.figmaToolHistoryPaidBackground)
                binding.secondContainerInvoices.setBackgroundResource(R.color.white)

            } else if (bill.status?.value == 7) {
                binding.bttPayNow.apply {
                    visibility = View.INVISIBLE
                    isClickable = false
                }

                binding.invoicesCardView.strokeColor = ContextCompat.getColor(
                    binding.invoicesCardView.context,
                    R.color.light_orange
                )

                binding.invoicesStatus.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.invoicesStatus.context,
                        R.drawable.status_icon_orange
                    )
                )

                binding.firstContainerInvoice.setBackgroundResource(R.color.light_orange)
                binding.secondContainerInvoices.setBackgroundResource(R.color.white)
            } else {
                binding.invoicesCardView.strokeColor = ContextCompat.getColor(
                    binding.invoicesCardView.context,
                    R.color.figmaToolHistoryUnpaidBackground
                )

                binding.invoicesStatus.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.invoicesStatus.context,
                        R.drawable.status_icon_red
                    )
                )
                binding.firstContainerInvoice.setBackgroundResource(R.color.figmaToolHistoryUnpaidBackground)
                binding.secondContainerInvoices.setBackgroundResource(R.color.white)
            }

            binding.downloadBillDetails.setOnClickListener {
                showPopupMenu(it, billId)
            }

            binding.bttPayNow.setOnClickListener {
                val flashAnimation =
                    AnimationUtils.loadAnimation(binding.root.context, R.anim.icon_flash_animation)
                binding.bttPayNow.startAnimation(flashAnimation)

                spinnerInterface.onStartSpinner()
                viewModel.payBill(billId, errorBody)
            }

            binding.executePendingBindings()
        }

        private fun showPopupMenu(view: View, id: String) {

            binding.downloadBillDetails.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.popup_menu
                )
            )

            val context = ContextThemeWrapper(view.context, R.style.CustomPopupMenuStyle)
            val popupMenu = PopupMenu(context, view, Gravity.END, 0, 0)
            popupMenu.menuInflater.inflate(R.menu.downloads_menu, popupMenu.menu)
            popupMenu.setForceShowIcon(true)

            popupMenu.setOnDismissListener {
                binding.downloadBillDetails.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.transparent
                    )
                )
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.pdfDownload -> {
                        // Skidanje PDF racuna
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            showNotification(binding, id, false)
                        } else {
                            when {
                                ContextCompat.checkSelfPermission(
                                    binding.root.context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    showNotification(binding, id, false)
                                }

                                else -> {
                                    spinnerInterface.requestNotificationFromUser(object :
                                        UserPermission {
                                        override fun onPermissionGranted() {
                                            showNotification(binding, id, false)
                                        }

                                        override fun onPermissionDenied() {
                                        }
                                    })
                                }
                            }
                        }
                        true
                    }

                    R.id.listingOfPassagesDownload -> {
                        // Skidanje listinga prolazaka

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            showNotification(binding, id, true)
                        } else {
                            when {
                                ContextCompat.checkSelfPermission(
                                    binding.root.context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    showNotification(binding, id, true)
                                }

                                else -> {
                                    spinnerInterface.requestNotificationFromUser(object :
                                        UserPermission {
                                        override fun onPermissionGranted() {
                                            showNotification(binding, id, true)
                                        }

                                        override fun onPermissionDenied() {
                                        }
                                    })
                                }
                            }
                        }
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            popupMenu.show()
        }

    }

    private fun showNotification(
        binding: ItemInvoicesBinding,
        billId: String,
        isListingOfPassages: Boolean
    ) {
        if (canDownload) {
            spinnerInterface.onStartSpinner()
            countDownTimer.start()

            val pdf = ".pdf"
            val downloadName = "$billId$pdf"

            if (isListingOfPassages) {
                // API poziv za listing prolazaka
                viewModel.downloadPassageData( object : DownloadBillsDetails {
                    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                    override fun onOK(pdf: BillDownload?) {
                        pdf?.data?.pdfContent?.let {
                            spinnerInterface.onStopSpinner()
                            viewModel.savePdfToDrive(
                                it,
                                downloadName,
                                binding.root.context,
                                true
                            )
                        }
                    }

                    override fun onFailed() {
                        spinnerInterface.onStopSpinner()
                        Toast.makeText(
                            binding.root.context,
                            binding.root.context.getString(R.string.download_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },billId)
            } else {
                // API poziv za PDF račun
                viewModel.downloadPdfBill(object : DownloadBillsDetails {
                    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
                    override fun onOK(pdf: BillDownload?) {
                        pdf?.data?.pdfContent?.let {
                            spinnerInterface.onStopSpinner()
                            viewModel.savePdfToDrive(
                                it,
                                downloadName,
                                binding.root.context,
                                false
                            )
                        }
                    }

                    override fun onFailed() {
                        spinnerInterface.onStopSpinner()
                        Toast.makeText(
                            binding.root.context,
                            binding.root.context.getString(R.string.download_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, billId)
            }
        } else {
            Toast.makeText(
                binding.root.context,
                binding.root.context.getString(R.string.download_timeout),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemInvoicesBinding.inflate(layoutInflater, parent, false)
        return BillsViewHolder(binding)
    }

    override fun getItemCount() = billsArray.size

    override fun onBindViewHolder(holder: BillsViewHolder, position: Int) {
        val current = billsArray[holder.bindingAdapterPosition]
        holder.bind(current, viewModel)
        checkDataFill(holder.bindingAdapterPosition, current, holder.binding.root.context)
    }

    private fun checkDataFill(position: Int, currentBill: Bill, context: Context) {
        Log.d(
            "BillsDetailsAdapter",
            "onBindViewHolder: adapter pos $position arrayTotal ${billsArray.size - 1} totalItems ${billData.total}"
        )
        if (billsArray[billsArray.size - 1] == currentBill && lastPage > currentPage) {


            val billDetailsFlow =
                MutableStateFlow<SubmitResult<BillsDetailsResponse>>(SubmitResult.Loading)

            collectLatestFlow(lifecycleOwner, billDetailsFlow) { serverResponse ->
                when (serverResponse) {
                    is SubmitResult.Success -> {
                        spinnerInterface.onStopSpinner()

                        serverResponse.data.let { data ->
                            spinnerInterface.onStopSpinner()

                            currentPage = data.data.currentPage

                            for (month: Bill in data.data.bills) {
                                billsArray.add(month)
                                notifyItemChanged(billsArray.size - 1)
                            }
                        }
                    }

                    is SubmitResult.FailureServerError -> {
                        spinnerInterface.onStopSpinner()
                        logError(context.resources.getString(R.string.server_error_msg))
                    }

                    is SubmitResult.FailureApiError -> {
                        spinnerInterface.onStopSpinner()
                        logError(context.resources.getString(R.string.api_call_error))
                    }

                    else -> {
                        spinnerInterface.onStopSpinner()
                        SubmitResult.Empty
                    }
                }
            }

            spinnerInterface.onStartSpinner()
            spinnerInterface.pagingUpdateBill(currentPage + 1, billDetailsFlow, availableCurrencies)
        }
    }

    fun submitList(data: BillData) {
        val newList: List<Bill> = data.bills
        val oldList = billsArray
        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            InvoicesItemDiffCallback(
                oldList,
                newList
            )
        )
        billsArray = newList as ArrayList<Bill>
        diffResult.dispatchUpdatesTo(this)
    }

    class InvoicesItemDiffCallback(
        private var oldInvoicesList: List<Bill>,
        private var newInvoicesList: List<Bill>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldInvoicesList.size
        }

        override fun getNewListSize(): Int {
            return newInvoicesList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return (oldInvoicesList[oldItemPosition].billFinal == newInvoicesList[newItemPosition].billFinal)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldInvoicesList[oldItemPosition].equals(newInvoicesList[newItemPosition])
        }

    }

    private fun logError(string: String) {
        Log.d(ToolHistoryListingAdapter.Companion.TAG, "showError: $string")
    }

    interface DownloadBillsDetails {
        fun onOK(pdf: BillDownload?)
        fun onFailed()
    }

    interface UserPermission {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }

}