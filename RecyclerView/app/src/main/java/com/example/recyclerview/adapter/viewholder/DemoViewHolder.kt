package com.example.recyclerview.adapter.viewholder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.recyclerview.App
import com.example.recyclerview.R

class DemoViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_movie, parent, false
    )
) {
    fun bind() {
        Toast.makeText(App.instance.context(), "bind ok", Toast.LENGTH_SHORT).show()
    }
}