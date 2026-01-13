package com.example.beerorderer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.beerorderer.R
import com.example.beerorderer.data.Beer

class BeerAdapter(
    private val onOrderClick: (Beer) -> Unit,
    private val priceConverter: ((String) -> String)? = null
) : ListAdapter<Beer, BeerAdapter.BeerViewHolder>(BeerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_beer, parent, false)
        return BeerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BeerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BeerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val beerImageView: ImageView = itemView.findViewById(R.id.beerImageView)
        private val beerNameTextView: TextView = itemView.findViewById(R.id.beerNameTextView)
        private val beerPriceTextView: TextView = itemView.findViewById(R.id.beerPriceTextView)
        private val beerRatingTextView: TextView = itemView.findViewById(R.id.beerRatingTextView)
        private val orderButton: Button = itemView.findViewById(R.id.orderButton)

        fun bind(beer: Beer) {
            beerNameTextView.text = beer.name
            beerPriceTextView.text = priceConverter?.invoke(beer.price) ?: beer.price
            beerRatingTextView.text = "★ ${"%.2f".format(beer.rating.average)} (${beer.rating.reviews} reviews)"

            // Load image with Glide
            if (!beer.image.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(beer.image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(beerImageView)
            } else {
                beerImageView.setImageResource(R.mipmap.ic_launcher)
            }

            orderButton.setOnClickListener {
                onOrderClick(beer)
            }
        }
    }

    class BeerDiffCallback : DiffUtil.ItemCallback<Beer>() {
        override fun areItemsTheSame(oldItem: Beer, newItem: Beer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Beer, newItem: Beer): Boolean {
            return oldItem == newItem
        }
    }
}

