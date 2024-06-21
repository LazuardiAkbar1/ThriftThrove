package com.dicoding.capstonui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.capstonui.R
import com.dicoding.capstonui.model.CartItem
import com.dicoding.capstonui.product.ProductDetailActivity

class CartAdapter(private val cartItems: MutableList<CartItem>) :
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]

        holder.itemName.text = cartItem.item_name
        holder.itemPrice.text = "Rp.${cartItem.price}"
        holder.itemQuantity.text = cartItem.quantity.toString()

        // Load image using Glide library
        Glide.with(holder.itemView.context)
            .load(cartItem.image)
            .centerCrop()
            .into(holder.itemImage)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ProductDetailActivity::class.java)
            intent.putExtra("item_id", cartItem.item_id)
            holder.itemView.context.startActivity(intent)
        }

        holder.deleteIcon.setOnClickListener {
            removeItem(position)
        }
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    private fun removeItem(position: Int) {
        cartItems.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, cartItems.size)
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.productImage)
        val itemName: TextView = itemView.findViewById(R.id.productName)
        val itemPrice: TextView = itemView.findViewById(R.id.productPrice)
        val itemQuantity: TextView = itemView.findViewById(R.id.productQuantity)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val cartItem = cartItems[position]
                    // Navigate to product detail activity or fragment with item_id
                    val intent = Intent(itemView.context, ProductDetailActivity::class.java)
                    intent.putExtra("item_id", cartItem.item_id)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }
}
