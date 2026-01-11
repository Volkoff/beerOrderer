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

class OrderAdapter(
    private val onRemoveClick: (Beer) -> Unit
) : ListAdapter<Beer, OrderAdapter.OrderViewHolder>(BeerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val beerImageView: ImageView = itemView.findViewById(R.id.beerImageView)
        private val beerNameTextView: TextView = itemView.findViewById(R.id.beerNameTextView)
        private val beerPriceTextView: TextView = itemView.findViewById(R.id.beerPriceTextView)
        private val beerRatingTextView: TextView = itemView.findViewById(R.id.beerRatingTextView)
        private val removeButton: Button = itemView.findViewById(R.id.removeButton)

        fun bind(beer: Beer) {
            beerNameTextView.text = beer.name
            beerPriceTextView.text = beer.price
            beerRatingTextView.text = itemView.context.getString(
                R.string.beer_rating_format,
                "%.2f".format(beer.rating.average),
                beer.rating.reviews
            )

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

            removeButton.setOnClickListener {
                onRemoveClick(beer)
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

