package com.example.myapplication.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.ReportsAdapter
import com.example.myapplication.databinding.ActivityReportsBinding
import com.example.myapplication.model.StoreInfo
import com.example.myapplication.ui.viewmodel.ReportsViewModel
import kotlinx.android.synthetic.main.activity_reports.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding:ActivityReportsBinding
    private lateinit var viewModel: ReportsViewModel
    private lateinit var adapter:ReportsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupUI()

        viewModel.showAllStores()

    }

    private fun setupUI() {
        binding.backBt.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(ReportsViewModel::class.java).apply {
            getAllStores().observe(this@ReportsActivity, {
                renderStores(it)
            })
        }
    }

    private fun renderStores(list:List<StoreInfo>) {
        adapter = ReportsAdapter(this)
        adapter.submitList(list)

        list_rv.apply {
            layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = this@ReportsActivity.adapter
        }

    }

    companion object {
        private const val TAG = "MINE"
    }

}