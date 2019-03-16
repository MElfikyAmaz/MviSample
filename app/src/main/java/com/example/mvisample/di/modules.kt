package com.example.mvisample.di

import com.example.data.BuildConfig
import com.example.data.remote.Api
import com.example.data.remote.createNetworkClient
import com.example.data.repos.MoviesRepo
import com.example.domain.repos.IMoviesRepo
import com.example.domain.usecases.GetNowPlayingMoviesUseCase
import com.example.domain.usecases.GetPopularMoviesUseCase
import com.example.domain.usecases.GetTopRatedMoviesUseCase
import com.example.domain.usecases.GetUpcomingMoviesUseCase
import com.example.mvisample.presentation.base.BaseViewModel
import com.example.mvisample.presentation.movies.MoviesAction
import com.example.mvisample.presentation.movies.MoviesResult
import com.example.mvisample.presentation.movies.MoviesState
import com.example.mvisample.presentation.now_playing.NowPlayingViewModel
import com.example.mvisample.presentation.popular.PopularViewModel
import com.example.mvisample.presentation.top_rated.TopRatedViewModel
import com.example.mvisample.presentation.upcoming.UpcomingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

const val BASE_URL = "https://api.themoviedb.org/3/"

private val retrofit: Retrofit = createNetworkClient(BASE_URL, BuildConfig.DEBUG)
private val moviesApi = retrofit.create(Api::class.java)

private val networkModule = module {
    factory { moviesApi }
}

private val repositoryModule = module {
    single<IMoviesRepo> { MoviesRepo(get()) }
}

private val useCaseModule = module {
    factory { GetPopularMoviesUseCase(get()) }
    factory { GetTopRatedMoviesUseCase(get()) }
    factory { GetNowPlayingMoviesUseCase(get()) }
    factory { GetUpcomingMoviesUseCase(get()) }
}

private val viewModelModule = module {
    viewModel { PopularViewModel(get()) }
    viewModel { TopRatedViewModel(get()) }
    viewModel { NowPlayingViewModel(get()) }
    viewModel { UpcomingViewModel(get()) }
}

fun getModules(): Array<Module> {
    return arrayOf(
        networkModule,
        repositoryModule,
        useCaseModule,
        viewModelModule
    )
}