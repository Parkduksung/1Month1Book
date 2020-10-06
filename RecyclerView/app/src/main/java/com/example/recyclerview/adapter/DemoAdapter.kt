package com.example.recyclerview.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recyclerview.adapter.viewholder.DemoViewHolder

class DemoAdapter : RecyclerView.Adapter<DemoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoViewHolder =
        DemoViewHolder(parent)

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: DemoViewHolder, position: Int) {
        holder.bind()
    }

}