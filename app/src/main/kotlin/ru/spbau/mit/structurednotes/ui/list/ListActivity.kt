package ru.spbau.mit.structurednotes.ui.list

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.MapView
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.serialization.json.JSON
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.data.*
import ru.spbau.mit.structurednotes.ui.attributes.CardAttributeAction
import ru.spbau.mit.structurednotes.utils.inflate

class ListActivity : AppCompatActivity() {

    private lateinit var cardType: CardType
    var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cardType = JSON.parse(intent.getStringExtra(EXTRA_CARD_TYPE))
        val notesData: CardData = JSON.parse(intent.getStringExtra(EXTRA_CARDS_DATA))

        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)

        title = cardType.name

        notes_list_view.layoutManager = LinearLayoutManager(this)
        notes_list_view.adapter = RecyclerAdapter(notesData.data)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    private inner class RecyclerAdapter(val data: List<NoteData>): RecyclerView.Adapter<RecyclerHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                RecyclerHolder(parent.inflate(R.layout.list_note).also { it.setBackgroundColor(cardType.color); })

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
            holder.bindTo(data[position].data)
        }
    }

    private inner class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindTo(data: List<List<String>>) {
            cardType.layout
                    .map { CardAttributeAction.from(it) }
                    .forEachIndexed { index, attr ->
                        attr.injectToList(this@ListActivity, itemView as ViewGroup, data[index])
                    }
        }
    }

}
