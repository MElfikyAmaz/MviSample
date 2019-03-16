package com.example.mvisample.presentation.movies

import android.os.Parcelable
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.domain.entity.Movie
import com.example.domain.entity.RequestResult
import com.example.mvisample.presentation.base.BaseViewModel
import com.example.mvisample.presentation.base.ShowSnackBar
import com.example.mvisample.presentation.base.ShowToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class MoviesViewModel : BaseViewModel<MoviesAction, MoviesResult, MoviesState>(
    MoviesState()
) {

    private var currentPage = 1
    var layoutManagerState: Parcelable? = null

    init {
        sendAction(Started)
    }

    abstract suspend fun getMovies(page: Int): RequestResult<List<Movie>>

    override fun actionToResult(action: MoviesAction): LiveData<MoviesResult> {
        val resultLiveData = MutableLiveData<MoviesResult>()

        when (action) {
            Started -> {
                viewModelScope.launch(Dispatchers.Main) {
                    resultLiveData.value = MoviesResult.Loading
                    val result = withContext(Dispatchers.IO) { getMovies(currentPage) }
                    when (result) {
                        is RequestResult.Success -> {
                            resultLiveData.value =
                                MoviesResult.Success(result.data)
                        }
                        is RequestResult.Error -> {
                            resultLiveData.value =
                                MoviesResult.Error(result.exception)
                        }
                    }
                }
            }
            Refresh -> {
                viewModelScope.launch(Dispatchers.Main) {
                    currentPage = 1
                    resultLiveData.value = MoviesResult.RefreshLoading
                    val result = withContext(Dispatchers.IO) { getMovies(currentPage) }
                    when (result) {
                        is RequestResult.Success -> {
                            resultLiveData.value =
                                MoviesResult.RefreshSuccess(result.data)
                        }
                        is RequestResult.Error -> {
                            resultLiveData.value =
                                MoviesResult.RefreshError(result.exception)
                        }
                    }
                }
            }
            LoadMore -> {
                viewModelScope.launch(Dispatchers.Main) {
                    resultLiveData.value = MoviesResult.LoadMoreLoading
                    val result = withContext(Dispatchers.IO) { getMovies(++currentPage) }
                    when (result) {
                        is RequestResult.Success -> {
                            resultLiveData.value =
                                MoviesResult.LoadMoreSuccess(result.data)
                        }
                        is RequestResult.Error -> {
                            resultLiveData.value =
                                MoviesResult.LoadMoreError(result.exception)
                        }
                    }
                }
            }
        }

        return resultLiveData
    }

    override fun reducer(previousState: MoviesState, result: MoviesResult): MoviesState {
        return when (result) {
            MoviesResult.Loading -> {
                previousState.copy(loadingVisibility = View.VISIBLE)
            }
            MoviesResult.RefreshLoading -> {
                previousState.copy(isRefreshing = true)
            }
            MoviesResult.LoadMoreLoading -> {
                previousState.copy(isLoadingMore = true)
            }
            is MoviesResult.Success -> {
                previousState.copy(
                    loadingVisibility = View.GONE,
                    mainViewVisibility = View.VISIBLE,
                    movies = result.movies
                )
            }
            is MoviesResult.RefreshSuccess -> {
                previousState.copy(isRefreshing = false, movies = result.movies)
            }
            is MoviesResult.LoadMoreSuccess -> {
                previousState.copy(isLoadingMore = false, movies = previousState.movies.plus(result.movies))
            }
            is MoviesResult.Error -> {
                previousState.copy(loadingVisibility = View.GONE, errorViewVisibility = View.VISIBLE)
            }
            is MoviesResult.LoadMoreError -> {
                previousState.copy(isLoadingMore = false)
            }
            is MoviesResult.RefreshError -> {
                previousState.copy(isRefreshing = false)
            }
        }
    }

    override fun sideEffect(result: MoviesResult) {
        when (result) {
            is MoviesResult.RefreshError -> {
                sendSingleEvent(ShowSnackBar("Something went wrong. Please try again."))
            }
            is MoviesResult.LoadMoreLoading -> {
                sendSingleEvent(ShowToast("Loading more data."))
            }
            is MoviesResult.LoadMoreError -> {
                sendSingleEvent(ShowToast("Load more error."))
            }
        }
    }


}