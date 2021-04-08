package com.taufik.themovieshow.ui.main.tvshow.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.taufik.themovieshow.R
import com.taufik.themovieshow.api.UrlEndpoint
import com.taufik.themovieshow.databinding.ItemsTvShowsBinding
import com.taufik.themovieshow.ui.main.tvshow.data.dummymain.DummyTvShowsMain
import com.taufik.themovieshow.ui.main.tvshow.ui.activity.DetailTvShowActivity

class TvShowsAdapter : RecyclerView.Adapter<TvShowsAdapter.TvShowsViewHolder>() {

    private var listTvShows = ArrayList<DummyTvShowsMain>()

    fun setTvShows(tvShowPopularResult: List<DummyTvShowsMain>) {
        this.listTvShows.clear()
        this.listTvShows.addAll(tvShowPopularResult)
        notifyDataSetChanged()
    }

    inner class TvShowsViewHolder(private val binding: ItemsTvShowsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tvShowPopularResult: DummyTvShowsMain) {
            with(binding) {
                Glide.with(itemView.context)
                        .load(UrlEndpoint.IMAGE_URL + tvShowPopularResult.imagePoster)
                        .apply(
                                RequestOptions.placeholderOf(R.drawable.ic_loading)
                                        .error(R.drawable.ic_error)
                        )
                        .into(imgPoster)

                tvTitle.text = tvShowPopularResult.title
                tvReleaseDate.text = tvShowPopularResult.firstAirDate
                tvRating.text = tvShowPopularResult.rating.toString()

                itemView.setOnClickListener {
                    val intent = Intent(itemView.context, DetailTvShowActivity::class.java).apply {
                        putExtra(DetailTvShowActivity.EXTRA_DETAIL_ID, tvShowPopularResult.id)
                        putExtra(DetailTvShowActivity.EXTRA_DETAIL_TITLE, tvShowPopularResult.title)
                    }
                    it.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TvShowsViewHolder {
        val itemsMovieShowBinding = ItemsTvShowsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TvShowsViewHolder(itemsMovieShowBinding)
    }

    override fun onBindViewHolder(holder: TvShowsViewHolder, position: Int) {
        val pos = listTvShows[position]
        holder.bind(pos)
    }

    override fun getItemCount(): Int = listTvShows.size
}