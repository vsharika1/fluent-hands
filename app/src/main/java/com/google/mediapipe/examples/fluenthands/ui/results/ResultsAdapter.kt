package com.google.mediapipe.examples.fluenthands.ui.results

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.google.mediapipe.examples.fluenthands.R
import com.google.mediapipe.examples.fluenthands.db.Result

class ResultsAdapter(private val context: Context, private var ResultsList: List<Result>): BaseAdapter() {

    override fun getItem(position: Int): Any {
        return ResultsList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return ResultsList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View{
        val view: View = View.inflate(context, R.layout.adapter_result,null)
        return view
    }

    fun replace(newEntryList: List<Result>){
        ResultsList = newEntryList
    }
}