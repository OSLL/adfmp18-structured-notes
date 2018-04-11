package ru.spbau.mit.structurednotes.ui.list

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_list.*
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.data.CardData
import ru.spbau.mit.structurednotes.data.CardType
import ru.spbau.mit.structurednotes.data.EXTRA_CARDS_DATA
import ru.spbau.mit.structurednotes.data.EXTRA_CARD_TYPE
import ru.spbau.mit.structurednotes.utils.inflate

class ListActivity : AppCompatActivity() {

    private lateinit var cardType: CardType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cardType = intent.getParcelableExtra(EXTRA_CARD_TYPE)
        val notesData: List<CardData> = intent.getParcelableArrayListExtra(EXTRA_CARDS_DATA)

        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)

        title = cardType.name

        notes_list_view.layoutManager = LinearLayoutManager(this)
        notes_list_view.adapter = RecyclerAdapter(notesData)
    }

    private inner class RecyclerAdapter(val data: List<CardData>): RecyclerView.Adapter<RecyclerHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                RecyclerHolder(parent.inflate(R.layout.list_note).also { it.setBackgroundColor(cardType.color); })

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
            holder.bindTo(data[position].data)
        }
    }

    private inner class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindTo(data: List<List<String>>) {
            cardType.layout.forEachIndexed { index, attr ->
                attr.injectToList(baseContext, itemView as ViewGroup, data[index])
            }
        }
    }

}
