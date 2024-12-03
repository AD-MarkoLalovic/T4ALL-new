package com.mobility.enp.view.dialogs

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobility.enp.R
import com.mobility.enp.data.model.notification.NotificationModel
import com.mobility.enp.databinding.NotificationDialogLayoutBinding
import com.mobility.enp.view.adapters.NotificationAdapter
import com.mobility.enp.viewmodel.NotificationsViewModel

class NotificationDialog : DialogFragment() {

    private var _binding: NotificationDialogLayoutBinding? = null
    private val binding: NotificationDialogLayoutBinding get() = _binding!!
    private val viewModel: NotificationsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.notification_dialog_layout, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.notificationList.observe(viewLifecycleOwner) { list ->
            val list2Send: ArrayList<NotificationModel> =
                arrayListOf()

            if (list.isNullOrEmpty()) {
                list2Send.add(
                    NotificationModel(
                        getString(R.string.no_notifications),  // title former
                        getString(R.string.no_notifications), // this was changed // what is shown
                        System.currentTimeMillis()
                    )
                )
            } else {
                list2Send.addAll(list)
            }


            context?.let {
                binding.cycler.adapter = NotificationAdapter(list2Send, it)
                binding.cycler.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
        }

        binding.notificationClose.setOnClickListener {
            dismiss()
        }

    }

    override fun onStart() {
        super.onStart()
        setWidthPercent(95)
        isCancelable = false
    }

    private fun DialogFragment.setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        dialog?.window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}