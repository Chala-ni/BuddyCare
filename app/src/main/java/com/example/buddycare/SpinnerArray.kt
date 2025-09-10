package com.example.buddycare

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SpinnerArray(context: Context, items: Array<String>) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {

    private val context: Context = context

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view as TextView
        textView.setTextColor(context.resources.getColor(R.color.input_text_color))
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        }
        val textView = convertView!!.findViewById<TextView>(R.id.text_view)
        textView.text = getItem(position)
        return convertView
    }
}