package com.google.mediapipe.examples.fluenthands.ui.results

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.mediapipe.examples.fluenthands.R
import com.google.mediapipe.examples.fluenthands.db.Result
import java.text.SimpleDateFormat

class ResultsAdapter(private val context: Context, private var ResultsList: List<Result>): BaseAdapter() {

    //get item from database
    override fun getItem(position: Int): Any {
        return ResultsList.get(position)
    }

    //get item with id from database
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //get item count from database
    override fun getCount(): Int {
        return ResultsList.size
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View{

        //initialize view
        val view: View = View.inflate(context, R.layout.adapter_result,null)

        //initialize the textViews
        val tvQuizNumber = view.findViewById(R.id.quizNum) as TextView
        val tvScore = view.findViewById(R.id.score) as TextView
        val tvDate = view.findViewById(R.id.date) as TextView
        val tvDifficulty = view.findViewById(R.id.difficulty) as TextView
        val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")

        //set the textViews using data from database
        tvDate.text = "Timestamp: ${sdf.format(ResultsList[position].dateTime.time)}"
        tvQuizNumber.text = "Quiz: ${ResultsList[position].id}"
        tvScore.text = "Score: ${ResultsList[position].score.toString()}"
        tvDifficulty.text = "Level: ${ResultsList[position].difficulty}"

        return view
    }

    fun replace(newEntryList: List<Result>){
        ResultsList = newEntryList
    }
}