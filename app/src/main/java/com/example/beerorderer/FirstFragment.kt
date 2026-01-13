package com.example.beerorderer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beerorderer.adapter.BeerAdapter
import com.example.beerorderer.databinding.FragmentFirstBinding
import com.example.beerorderer.viewmodel.BeerViewModel
import com.example.beerorderer.viewmodel.FilterOption
import com.example.beerorderer.viewmodel.SortOption
import com.google.android.material.snackbar.Snackbar

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: BeerViewModel by activityViewModels()
    private lateinit var beerAdapter: BeerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        observeViewModel()

        // Load beers
        viewModel.loadBeers()
    }

    private fun setupRecyclerView() {
        beerAdapter = BeerAdapter(
            onOrderClick = { beer ->
                viewModel.addToOrder(beer)
                Snackbar.make(
                    binding.root,
                    "${beer.name} added to order!",
                    Snackbar.LENGTH_SHORT
                ).show()
            },
            priceConverter = { price -> viewModel.getConvertedPrice(price) }
        )

        binding.beerRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = beerAdapter
        }
    }

    private fun setupFilters() {
        // Setup Sort Spinner
        val sortOptions = arrayOf("Sort by", "Price (Low)", "Price (High)", "Rating (Low)", "Rating (High)", "Reviews (Low)", "Reviews (High)")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOptions)
        binding.sortSpinner.adapter = sortAdapter
        binding.sortSpinner.setSelection(0)

        binding.sortSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val sortOption = when (position) {
                    0 -> SortOption.NONE
                    1 -> SortOption.PRICE_ASC
                    2 -> SortOption.PRICE_DESC
                    3 -> SortOption.RATING_ASC
                    4 -> SortOption.RATING_DESC
                    5 -> SortOption.REVIEWS_ASC
                    6 -> SortOption.REVIEWS_DESC
                    else -> SortOption.NONE
                }
                viewModel.setSortOption(sortOption)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Setup Filter Spinner
        val filterOptions = arrayOf("All Beers", "IPA", "Ale", "Stout", "Lager", "Pilsner", "Porter", "Other")
        val filterAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterOptions)
        binding.filterSpinner.adapter = filterAdapter
        binding.filterSpinner.setSelection(0)

        binding.filterSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filterOption = when (position) {
                    0 -> FilterOption.ALL
                    1 -> FilterOption.IPA
                    2 -> FilterOption.ALE
                    3 -> FilterOption.STOUT
                    4 -> FilterOption.LAGER
                    5 -> FilterOption.PILSNER
                    6 -> FilterOption.PORTER
                    7 -> FilterOption.OTHER
                    else -> FilterOption.ALL
                }
                viewModel.setFilterOption(filterOption)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewModel.beers.observe(viewLifecycleOwner) { beers ->
            beerAdapter.submitList(beers)
            binding.beerCountTextView.text = "${beers.size} beers"
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.beerRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorTextView.text = error
                binding.errorTextView.visibility = View.VISIBLE
                binding.beerRecyclerView.visibility = View.GONE
            } else {
                binding.errorTextView.visibility = View.GONE
            }
        }

        // Observe currency changes to refresh prices
        viewModel.currentCurrency.observe(viewLifecycleOwner) {
            beerAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

