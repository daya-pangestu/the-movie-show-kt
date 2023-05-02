package com.taufik.themovieshow.data.paging.tvshow

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.taufik.themovieshow.data.remote.api.ApiService
import com.taufik.themovieshow.model.response.tvshow.popularairingtoday.TvShowsMainResult
import com.taufik.themovieshow.utils.CommonConstants
import retrofit2.HttpException

class TvShowsPopularPagingSource(
    private val apiService: ApiService
) : PagingSource<Int, TvShowsMainResult>() {
    override fun getRefreshKey(state: PagingState<Int, TvShowsMainResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TvShowsMainResult> {
        return try {
            val currentPage = params.key ?: CommonConstants.STARTING_PAGE_INDEX
            val response = apiService.getTvShowsPopular(currentPage)
            val data = response.body()?.results
            val nextKey = if (data.isNullOrEmpty()) {
                null
            } else {
                currentPage + (params.loadSize / CommonConstants.LOAD_MAX_PER_PAGE)
            }
            LoadResult.Page(
                data = data ?: emptyList(),
                prevKey = if (currentPage == CommonConstants.STARTING_PAGE_INDEX) null else currentPage - 1,
                nextKey = nextKey?.plus(1)
            )
        } catch (httpEx: HttpException) {
            LoadResult.Error(httpEx)
        } catch (ex: Exception) {
            LoadResult.Error(ex)
        }
    }
}