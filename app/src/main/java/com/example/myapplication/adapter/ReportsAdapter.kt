package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.StoreInfo
import kotlinx.android.synthetic.main.layout_report_item.view.*

class ReportsAdapter(
    private val context: Context,
    var onItemSelectListener: ((pos:Int)->Unit)? = null
) : ListAdapter<StoreInfo, ReportsAdapter.AdapterVH>(ModelDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterVH {
        return AdapterVH(context, LayoutInflater.from(context)
            .inflate(R.layout.layout_report_item, parent, false))
    }

    override fun onBindViewHolder(holder: AdapterVH, position: Int) {
        holder.bind(getItem(position), position, onItemSelectListener)
    }

    class AdapterVH(val context: Context, val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(data: StoreInfo, position: Int, onItemSelectListener: ((pos:Int)->Unit)?) {
            view.apply {
                setOnClickListener {
                    onItemSelectListener?.invoke(position)
                }
                name_tv.text = "${data.name}"
                total_guest_tv.text = "${data.visited}"
            }
        }
    }

    class ModelDiff : DiffUtil.ItemCallback<StoreInfo>() {
        override fun areItemsTheSame(oldItem: StoreInfo, newItem: StoreInfo): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: StoreInfo, newItem: StoreInfo): Boolean =
            oldItem == newItem
    }
}
