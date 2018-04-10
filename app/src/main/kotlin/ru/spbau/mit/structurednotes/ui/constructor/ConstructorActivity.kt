package ru.spbau.mit.structurednotes.ui.constructor

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.activity_constructor.*
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.gridLayout
import org.jetbrains.anko.imageButton
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.data.*
import ru.spbau.mit.structurednotes.utils.inflate
import java.util.*

class ConstructorActivity : AppCompatActivity() {

    private val logos = listOf(R.drawable.ic_add_audio, R.drawable.ic_add_text, R.drawable.ic_add_photo)

    private val cardTypeBuilder = CardTypeBuilder().apply {
        color = Color.argb(100, 255, 0, 0)
        logo = R.drawable.ic_add_text
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constructor)
        colorButton.setBackgroundColor(cardTypeBuilder.color!!)

        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
                    makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.UP or ItemTouchHelper.DOWN) or
                            makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT)

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = Math.min(viewHolder.adapterPosition, target.adapterPosition)
                val to = Math.max(viewHolder.adapterPosition, target.adapterPosition)

                for (i in from until to) {
                    Collections.swap(cardTypeBuilder.layout, i, i + 1)
                }

                recyclerView.adapter.notifyItemMoved(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    cardTypeBuilder.remove(viewHolder.layoutPosition)
                    template.adapter.notifyItemRemoved(viewHolder.layoutPosition)
                }
            }
        }).attachToRecyclerView(template)

        template.adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                return ViewHolder(parent.inflate(R.layout.constructor_block))
            }

            override fun getItemCount() = cardTypeBuilder.layout.size

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                (holder.itemView as ViewGroup).removeAllViews()
                holder.itemView.setBackgroundColor(cardTypeBuilder.color!!)
                holder.bindTo(cardTypeBuilder.layout[position])
            }
        }

        template.layoutManager = LinearLayoutManager(this)

        setResult(Activity.RESULT_CANCELED, Intent())
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindTo(attr: CardAttribute) = attr.injectTo(baseContext, itemView as ViewGroup)
    }

    fun onAddPhotoButtonClick(view: View) {
        cardTypeBuilder.photo()
        template.adapter.notifyItemInserted(cardTypeBuilder.layout.lastIndex)
    }

    fun onAddAudioButtonClick(view: View) {
        cardTypeBuilder.audio()
        template.adapter.notifyItemInserted(cardTypeBuilder.layout.lastIndex)
    }

    fun onAddLocationButtonClick(view: View) {
        GPS.constructorDialog(this) { auto ->
            cardTypeBuilder.gps(auto)
            template.adapter.notifyItemInserted(cardTypeBuilder.layout.lastIndex)
        }
    }

    fun onAddNotificationButtonClick(view: View) {
        // pass
    }

    fun onAddTextButtonClick(view: View) {
        Text.constructorDialog(this) { short, label ->
            cardTypeBuilder.text(short, label)
            template.adapter.notifyItemInserted(cardTypeBuilder.layout.lastIndex)
        }
    }

    fun onColorPickerClick(view: View) {
        ColorPickerDialogBuilder.with(this).apply {
            initialColors(intArrayOf(
                    Color.argb(100, 0, 0, 255),
                    Color.argb(100, 0, 255, 0),
                    Color.argb(100, 255, 0, 0)))
            initialColor(cardTypeBuilder.color!!)
            noSliders()
            setPositiveButton("ok", { _, color, _ ->
                cardTypeBuilder.color = color
                colorButton.setBackgroundColor(color)
                template.adapter.notifyDataSetChanged()
            })
        }.build().show()
    }

    fun onLogoButtonClick(view: View) {
        AlertDialog.Builder(this).apply {
            setView(with(AnkoContext.create(context, false)) {
                gridLayout {
                    for (logoId in logos) {
                        imageButton(logoId).apply {
                            setOnClickListener { _ ->
                                cardTypeBuilder.logo = logoId
                                logoButton.setImageResource(logoId)
                            }
                        }
                    }
                }
            })
        }.create().show()
    }

    fun onAddCardTypeClick(view: View) {
        val name = categoryNameEditText.text.toString()

        cardTypeBuilder.name = if (name.isEmpty()) null else name

        val cardType = cardTypeBuilder.build()

        cardType ?: also {
            Toast.makeText(this@ConstructorActivity, "enter category name", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent().also {
            it.putExtra(EXTRA_CARD_TYPE, cardType)
        }

        setResult(Activity.RESULT_OK, intent)

        finish()
    }
}
