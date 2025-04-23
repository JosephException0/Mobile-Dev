package com.android.wordify

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

class HighContrastArrayAdapter(
    context: Context,
    private val resource: Int,
    private val textViewId: Int,
    private val items: List<String>
) : ArrayAdapter<String>(context, resource, textViewId, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val textView = view.findViewById<TextView>(textViewId)
        val imageView = view.findViewById<ImageView>(R.id.list_item_icon)

        textView.text = items[position]

        val sharedPreferences = context.getSharedPreferences("Mode", Context.MODE_PRIVATE)
        val isHighContrast = sharedPreferences.getBoolean("highContrast", false)

        if (isHighContrast) {
            // Apply high contrast color (white) for both text and icon
            val hcColor = ContextCompat.getColor(context, R.color.hc_text_settings)
            textView.setTextColor(hcColor)
            imageView.setColorFilter(hcColor)
        } else {
            // Use default theme-defined text and icon color
            val textColor = ContextCompat.getColor(context, R.color.text_settings)
            val iconColor = ContextCompat.getColor(context, R.color.game_text)
            textView.setTextColor(textColor)
            imageView.setColorFilter(iconColor)
        }

        return view
    }
}
