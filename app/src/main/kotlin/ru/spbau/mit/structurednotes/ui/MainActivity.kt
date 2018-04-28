package ru.spbau.mit.structurednotes.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_card.view.*
import kotlinx.serialization.json.JSON
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.data.*
import ru.spbau.mit.structurednotes.ui.constructor.ConstructorActivity
import ru.spbau.mit.structurednotes.ui.list.ListActivity
import ru.spbau.mit.structurednotes.ui.note.NoteActivity
import ru.spbau.mit.structurednotes.utils.inflate
import java.util.*

class MainActivity : AppCompatActivity() {

    private val CONSTRUCTOR_CARD_TYPE = 1
    private val NOTE_TYPE = 2

    private lateinit var db: PersistentDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        db = PersistentDB(filesDir.resolve("data"), filesDir.resolve("cards"))
        db.load()

        ItemTouchHelper(object : ItemTouchHelper.Callback() {

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
                    makeFlag(
                            ItemTouchHelper.ACTION_STATE_DRAG,
                            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    )

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = viewHolder.adapterPosition
                val to  = target.adapterPosition
                if (from < to) {
                    for (i in from until to) {
                        Collections.swap(db.cards.data, i, i + 1)
                    }
                } else {
                    for (i in from downTo to + 1) {
                        Collections.swap(db.cards.data, i, i - 1)
                    }
                }
                recyclerView.adapter.notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {}
        }).attachToRecyclerView(card_view)

        card_view.layoutManager = GridLayoutManager(this, 2)
        card_view.adapter = RecyclerAdapter()

        fab.setOnClickListener {
            val intent = Intent(this, ConstructorActivity::class.java).also {
                it.putExtra(EXTRA_CARD_TYPE_ID, (db.cards.data.map { it.id }.max() ?: 0) + 1)
            }
            startActivityForResult(intent, CONSTRUCTOR_CARD_TYPE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                Holder(parent.inflate(R.layout.activity_main_card))

        override fun getItemCount() = db.cards.data.size

        override fun onBindViewHolder(holder: Holder, position: Int) = holder.bindTo(db.cards.data[position])

        private inner class Holder(private val view: View) : RecyclerView.ViewHolder(view) {
            private lateinit var cardType: CardType

            init {
                view.setOnClickListener {
                    val intent = Intent(this@MainActivity, NoteActivity::class.java).also {
                        it.putExtra(EXTRA_CARD_TYPE, JSON.stringify(cardType))
                    }

                    startActivityForResult(intent, NOTE_TYPE)
                }
                view.list_layout.setOnClickListener {
                    val intent = Intent(this@MainActivity, ListActivity::class.java).also {
                        it.putExtra(EXTRA_CARD_TYPE, JSON.stringify(cardType))
                        it.putExtra(EXTRA_CARDS_DATA, JSON.stringify(db.cardsData.data[cardType.id] ?: CardData(mutableListOf())))
                    }

                    startActivity(intent)
                }
            }

            fun bindTo(cardType: CardType) {
                this.cardType = cardType
                view.name.text = cardType.name
                view.setBackgroundColor(cardType.color)
                view.logo.setImageResource(cardType.logo)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CONSTRUCTOR_CARD_TYPE -> {
                    val cardType = JSON.parse<CardType>(data.getStringExtra(EXTRA_CARD_TYPE))
                    db.cards.data.add(cardType)
                    card_view.adapter.notifyItemInserted(db.cards.data.lastIndex)
                }
                NOTE_TYPE -> {
                    val cardType = JSON.parse<CardType>(data.getStringExtra(EXTRA_CARD_TYPE))
                    val noteData = JSON.parse<NoteData>(data.getStringExtra(EXTRA_CARD_DATA))

                    db.cardsData.data.getOrPut(cardType.id, { CardData(mutableListOf()) }).data.add(noteData)
                }
                else -> error("impossible case")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        db.store()
    }
}