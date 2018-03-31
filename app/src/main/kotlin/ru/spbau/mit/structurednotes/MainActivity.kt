package ru.spbau.mit.structurednotes

import android.os.Bundle
import android.support.design.widget.Snackbar
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

import ru.spbau.mit.structurednotes.utils.inflate
import java.util.*

class MainActivity : AppCompatActivity() {

    private val cards: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        ItemTouchHelper(object : ItemTouchHelper.Callback() {

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
                    makeFlag(
                            ItemTouchHelper.ACTION_STATE_DRAG,
                            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    )

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val from = viewHolder.adapterPosition
                val to  = target.adapterPosition
                Collections.swap(cards, from, to)
                recyclerView.adapter.notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {}
        }).attachToRecyclerView(card_view)

        card_view.layoutManager = GridLayoutManager(this, 2)
        card_view.adapter = RecyclerAdapter()

        fab.setOnClickListener { view ->
            cards.add((cards.size + 1).toString())
            card_view.adapter.notifyItemInserted(cards.lastIndex)
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

        override fun getItemCount() = cards.size

        override fun onBindViewHolder(holder: Holder, position: Int) = holder.bindTo(cards[position])

        private inner class Holder(private val view: View) : RecyclerView.ViewHolder(view) {

            init {
                view.setOnClickListener {
                    Snackbar
                            .make(it, "Hella there!", Snackbar.LENGTH_LONG)
                            .show()
                }
            }

            fun bindTo(data: String) {
                view.name.text = data
            }
        }
    }
}
