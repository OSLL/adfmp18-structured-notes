package ru.spbau.mit.structurednotes.ui.attributes.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import kotlinx.android.synthetic.main.list_audio.view.*
import kotlinx.android.synthetic.main.note_audio.view.*
import kotlinx.android.synthetic.main.note_audio_record.view.*
import org.jetbrains.anko.runOnUiThread
import ru.spbau.mit.structurednotes.R
import ru.spbau.mit.structurednotes.ui.attributes.CardAttributeAction
import ru.spbau.mit.structurednotes.ui.constructor.ConstructorActivity
import ru.spbau.mit.structurednotes.ui.list.ListActivity
import ru.spbau.mit.structurednotes.ui.note.NoteActivity
import ru.spbau.mit.structurednotes.utils.inflate

class AudioAction : CardAttributeAction {
    override fun injectToConstructor(constructorActivity: ConstructorActivity, itemViewGroup: ViewGroup) {
        val imageView = ImageView(constructorActivity)
        imageView.setImageResource(R.drawable.ic_add_audio)
        itemViewGroup.addView(imageView)

    }

    override fun injectToList(listActivity: ListActivity, itemViewGroup: ViewGroup, data: List<String>) {
        val view = itemViewGroup.inflate(R.layout.list_audio)
        view.list_audio_records.adapter = RecyclerAdapter(listActivity, data)

        itemViewGroup.addView(view)
    }

    override fun injectToNote(noteActivity: NoteActivity, itemViewGroup: ViewGroup) {
        val view = itemViewGroup.inflate(R.layout.note_audio)
        view.note_audio_records.adapter = RecyclerAdapter(noteActivity, noteActivity.audioData())
        view.note_audio_records.layoutManager = LinearLayoutManager(noteActivity)
        view.setBackgroundColor(noteActivity.cardType.color)

        itemViewGroup.addView(view)
    }


    class RecyclerAdapter(val ctx: Context, val data: List<String>) : RecyclerView.Adapter<RecyclerHolder>() {
        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
            holder.bindTo(Uri.parse(data[position]))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                RecyclerHolder(ctx, parent.inflate(R.layout.note_audio_record))
    }

    class RecyclerHolder(val ctx: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        var progress = 0
        lateinit var mediaPlayer: MediaPlayer
        val handler = Handler()
        val period = 300


        fun bindTo(uri: Uri) {
            mediaPlayer = MediaPlayer.create(ctx, uri)

            itemView.progress.max = mediaPlayer.duration / period  - 1
            itemView.progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress * period)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}

                override fun onStopTrackingTouch(p0: SeekBar?) {}

            })

            itemView.play_button.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                } else {
                    mediaPlayer.start()
                    handler.post(object : Runnable {
                        override fun run() {
                            val self = this
                            ctx.runOnUiThread {
                                if (mediaPlayer.isPlaying) {
                                    itemView.progress.progress = mediaPlayer.currentPosition / period
                                    handler.postDelayed(self, period.toLong())
                                }
                            }
                        }
                    })
                }
            }
        }
    }
}