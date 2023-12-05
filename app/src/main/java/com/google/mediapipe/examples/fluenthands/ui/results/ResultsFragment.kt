package com.google.mediapipe.examples.fluenthands.ui.results

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import com.google.mediapipe.examples.fluenthands.R
import com.google.mediapipe.examples.fluenthands.db.Result
import com.google.mediapipe.examples.fluenthands.db.ResultDatabase
import com.google.mediapipe.examples.fluenthands.db.ResultDatabaseDao
import com.google.mediapipe.examples.fluenthands.db.ResultRepository
import com.google.mediapipe.examples.fluenthands.db.ResultViewModel
import com.google.mediapipe.examples.fluenthands.db.ResultViewModelFactory

class ResultsFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var adapter: ResultsAdapter
    private lateinit var database: ResultDatabase
    private lateinit var databaseDao: ResultDatabaseDao
    private lateinit var repository: ResultRepository
    private lateinit var viewModelFactory: ResultViewModelFactory
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var deleteButton: Button

    companion object {
        fun newInstance() = ResultsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_results, container, false)
        listView = view.findViewById(R.id.results_list)

        val application = requireNotNull(this.activity).application
        database = ResultDatabase.getInstance(application)
        databaseDao = database.resultDatabaseDao
        repository = ResultRepository(databaseDao)
        viewModelFactory = ResultViewModelFactory(repository)
        resultViewModel = ViewModelProvider(this, viewModelFactory).get(ResultViewModel::class.java)
        adapter = ResultsAdapter(requireContext(), emptyList())

        // Set the adapter to the ListView
        listView.adapter = adapter

        resultViewModel.allResultData.observe(viewLifecycleOwner) { results ->
            // Update the adapter with new data
            adapter.replace(results)
            adapter.notifyDataSetChanged()
        }


        return view
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

}