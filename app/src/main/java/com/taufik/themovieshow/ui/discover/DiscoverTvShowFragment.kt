package com.taufik.themovieshow.ui.discover

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.taufik.themovieshow.R
import com.taufik.themovieshow.data.NetworkResult
import com.taufik.themovieshow.databinding.FragmentDiscoverTvShowBinding
import com.taufik.themovieshow.ui.main.tvshow.adapter.DiscoverTvShowsAdapter
import com.taufik.themovieshow.ui.main.tvshow.viewmodel.TvShowsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoverTvShowFragment : Fragment() {

    private var _binding: FragmentDiscoverTvShowBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<TvShowsViewModel>()
    private val discoverTvShowsAdapter by lazy { DiscoverTvShowsAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiscoverTvShowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initAdapter()
        initSearch()
    }

    private fun initToolbar() {
        binding.toolbarSearchTvShow.imgBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initAdapter()  {
        binding.rvSearchTvShow.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = discoverTvShowsAdapter
        }
    }

    private fun initSearch()  {
        binding.toolbarSearchTvShow.etSearch.apply {
            setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard()
                    return@OnEditorActionListener true
                }
                false
            })

            addTextChangedListener(afterTextChanged = { p0 ->
                showSearchData(p0)
            })
        }
    }

    private fun showSearchData(query: Editable?) {
        viewModel.apply {
            val q = query.toString()
            if (q.isNotEmpty()) {
                setDiscoverTvShows(q)
                    discoverTvShowsResponse.observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is NetworkResult.Loading -> {}
                        is NetworkResult.Success -> {
                            val data = response.data
                            if (data != null) {
                                if (data.results.isNotEmpty()) {
                                    discoverTvShowsAdapter.submitList(data.results)
                                    showNoResults(false)
                                } else {
                                    showNoResults(true)
                                }
                            }
                        }
                        is NetworkResult.Error -> {}
                    }
                }
            }
        }
    }

    private fun hideKeyboard() {
        binding.apply {
            toolbarSearchTvShow.apply {
                etSearch.clearFocus()
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etSearch.windowToken, 0)
            }
        }
    }

    private fun showNoResults(isShow: Boolean) {
        binding.apply {
            if (isShow) {
                layoutNoSearch.apply {
                    root.isVisible = true
                    imgError.setImageResource(R.drawable.ic_search_orange)
                    tvError.text = getString(R.string.tvNoSearchData)
                }
            } else {
                layoutNoSearch.root.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}