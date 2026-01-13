package com.example.beerorderer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.beerorderer.adapter.OrderAdapter
import com.example.beerorderer.databinding.FragmentSecondBinding
import com.example.beerorderer.viewmodel.BeerViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel: BeerViewModel by activityViewModels()
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(
            onRemoveClick = { beer ->
                viewModel.removeFromOrder(beer)
                Snackbar.make(
                    binding.root,
                    "${beer.name} removed from order!",
                    Snackbar.LENGTH_SHORT
                ).show()
            },
            priceConverter = { price -> viewModel.getConvertedPrice(price) }
        )

        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            orderAdapter.submitList(orders)

            // Show/hide empty state
            if (orders.isEmpty()) {
                binding.emptyTextView.visibility = View.VISIBLE
                binding.ordersRecyclerView.visibility = View.GONE
                binding.totalCard.visibility = View.GONE
            } else {
                binding.emptyTextView.visibility = View.GONE
                binding.ordersRecyclerView.visibility = View.VISIBLE
                binding.totalCard.visibility = View.VISIBLE
            }
        }

        // Observe total price
        viewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
            binding.totalPriceTextView.text = totalPrice
        }

        // Observe currency changes to refresh prices
        viewModel.currentCurrency.observe(viewLifecycleOwner) {
            orderAdapter.notifyDataSetChanged()
        }

        // Setup send order button
        binding.sendOrderButton.setOnClickListener {
            if (viewModel.orderCount.value ?: 0 > 0) {
                viewModel.sendOrder()
                Snackbar.make(
                    binding.root,
                    "Order sent successfully!",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        // Setup clear order button
        binding.clearOrderButton.setOnClickListener {
            if (viewModel.orderCount.value ?: 0 > 0) {
                viewModel.clearAllOrders()
                Snackbar.make(
                    binding.root,
                    "All orders cleared!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}