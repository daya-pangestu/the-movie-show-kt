package com.taufik.themovieshow.ui.favorite.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.taufik.themovieshow.R
import com.taufik.themovieshow.data.local.entity.movie.FavoriteMovieEntity
import com.taufik.themovieshow.databinding.FragmentFavoriteMovieBinding
import com.taufik.themovieshow.model.response.movie.nowplayingupcoming.MovieMainResult
import com.taufik.themovieshow.ui.favorite.adapter.FavoriteMovieAdapter
import com.taufik.themovieshow.ui.favorite.adapter.SortFilteringAdapter
import com.taufik.themovieshow.ui.favorite.viewmodel.FavoriteMovieViewModel
import com.taufik.themovieshow.utils.navigateToDetailMovie
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteMovieFragment : Fragment() {

    private var _binding: FragmentFavoriteMovieBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FavoriteMovieViewModel by viewModels()
    private val sortFilteringAdapter by lazy { SortFilteringAdapter { showFilteringData(it) }}
    private val favoriteMovieAdapter by lazy { FavoriteMovieAdapter {
        navigateToDetailMovie(it.id, it.title, FavoriteMovieViewModel.position)
    }}

    private var favoriteList: List<FavoriteMovieEntity> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()
        showFavoriteMoviesByPosition(FavoriteMovieViewModel.position)
        searchData()
        setFilteringAdapter()
        showSortFilteringData()
    }

    private fun setAdapter() {
        binding.rvDiscoverFavoriteMovies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = favoriteMovieAdapter
        }
    }

    private fun showFavoriteMoviesByPosition(position: Int) {
        when (position) {
            0 -> getAllFavoriteMovies(FavoriteMovieViewModel.position)
            1 -> getFavoriteMovieByTitle(FavoriteMovieViewModel.position)
            2 -> getFavoriteMovieByRelease(FavoriteMovieViewModel.position)
            3 -> getFavoriteMovieByRating(FavoriteMovieViewModel.position)
        }
    }

    private fun setFilteringAdapter() {
        val flexLayoutManager = FlexboxLayoutManager(requireContext())
        flexLayoutManager.apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
        }

        binding.rvSortFiltering.apply {
            layoutManager = flexLayoutManager
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            adapter = sortFilteringAdapter
        }
    }

    private fun showSortFilteringData() {
        sortFilteringAdapter.submitList(viewModel.getSortFiltering(requireContext()))
        sortFilteringAdapter.setDefaultSelectedItemPosition(FavoriteMovieViewModel.position)
    }

    private fun showFilteringData(position: Int) {
        when (position) {
            0 -> {
                FavoriteMovieViewModel.position = 0
                getAllFavoriteMovies(FavoriteMovieViewModel.position)
                scrollToTopPositionItem()
            }
            1 -> {
                FavoriteMovieViewModel.position = 1
                getFavoriteMovieByTitle(FavoriteMovieViewModel.position)
                scrollToTopPositionItem()
            }
            2 -> {
                FavoriteMovieViewModel.position = 2
                getFavoriteMovieByRelease(FavoriteMovieViewModel.position)
                scrollToTopPositionItem()
            }
            3 -> {
                FavoriteMovieViewModel.position = 3
                getFavoriteMovieByRating(FavoriteMovieViewModel.position)
                scrollToTopPositionItem()
            }
        }
        sortFilteringAdapter.setDefaultSelectedItemPosition(FavoriteMovieViewModel.position)
        favoriteMovieAdapter.setData(mapList(favoriteList), FavoriteMovieViewModel.position)
    }

    private fun scrollToTopPositionItem() {
        lifecycleScope.launch {
            delay(100)
            binding.rvDiscoverFavoriteMovies.smoothScrollToPosition(0)
        }
    }

    private fun getAllFavoriteMovies(position: Int) {
        viewModel.getAllFavoriteMovies.observe(viewLifecycleOwner) {
            favoriteList = it
            showFavoriteMovies(it, position)
        }
    }

    private fun getFavoriteMovieByTitle(position: Int) {
        viewModel.getFavoriteMoviesByTitle.observe(viewLifecycleOwner) {
            favoriteList = it
            showFavoriteMovies(it, position)
        }
    }

    private fun getFavoriteMovieByRelease(position: Int) {
        viewModel.getFavoriteMoviesByRelease.observe(viewLifecycleOwner) {
            favoriteList = it
            showFavoriteMovies(it, position)
        }
    }

    private fun getFavoriteMovieByRating(position: Int) {
        viewModel.getFavoriteMoviesByRating.observe(viewLifecycleOwner) {
            favoriteList = it
            showFavoriteMovies(it, position)
        }
    }

    private fun showFavoriteMovies(favoriteMovieList: List<FavoriteMovieEntity>?, position: Int) {
        if (!favoriteMovieList.isNullOrEmpty()) {
            when (position) {
                0, 1, 2, 3 -> favoriteMovieAdapter.setData(mapList(favoriteMovieList), position)
            }
            showNoFavorite(false)
        } else {
            showNoFavorite(true)
        }
    }

    private fun searchData() {
        binding.etSearch.apply {
            setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard()
                    return@OnEditorActionListener true
                }
                false
            })

            addTextChangedListener(afterTextChanged = { p0 ->
                favoriteMovieAdapter.filter.filter(p0.toString())
            })
        }
    }

    private fun showNoFavorite(isShow: Boolean) {
        binding.apply {
            if (isShow) {
                layoutNoFavorite.apply {
                    root.isVisible = true
                    tvErrorTitle.apply {
                        isVisible = true
                        text = getString(R.string.tvNoFavoriteData)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.colorOrange))
                    }
                    tvErrorDesc.apply {
                        isVisible = true
                        text = getString(R.string.tvSaveFavoriteMovie)
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.colorOrange))
                    }
                }
            } else {
                layoutNoFavorite.root.isVisible = false
            }
        }
    }

    private fun mapList(movies: List<FavoriteMovieEntity>): ArrayList<MovieMainResult> {
        val listMovies = ArrayList<MovieMainResult>()
        movies.forEach { movie ->
            val movieMapped =
                MovieMainResult(
                    movie.movieId,
                    movie.moviePoster,
                    movie.movieReleaseData,
                    movie.movieTitle,
                    movie.movieRating
                )
            listMovies.add(movieMapped)
        }

        return listMovies
    }

    private fun hideKeyboard() {
        binding.apply {
            etSearch.clearFocus()
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val POSITION_KEY = "position_key"
    }
}