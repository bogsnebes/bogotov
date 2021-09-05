package com.bogotov.prog_gifs.ui.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bogotov.prog_gifs.data.NetworkService
import com.bogotov.prog_gifs.data.dto.GifDto
import com.bogotov.prog_gifs.data.dto.ResponseWrapperDto
import com.bogotov.prog_gifs.domain.Event
import com.bogotov.prog_gifs.domain.PageSection
import com.bogotov.prog_gifs.domain.Panel
import com.bogotov.prog_gifs.domain.State
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

internal class PageViewModel : ViewModel() {

    var pageSection: PageSection = PageSection.RANDOM

    val gifDtoState: LiveData<State<GifDto?>>
        get() = _gifState

    val panel: LiveData<Panel>
        get() = _panel

    val infoPanel: LiveData<Boolean>
        get() = _infoPanel

    private val _gifState = MutableLiveData<State<GifDto?>>()

    private var _panel: MutableLiveData<Panel> =
        MutableLiveData<Panel>().apply {
            this.postValue(Panel(backButton = false, nextButton = false, refreshButton = false))
        }

    private var currentIndex = -1

    private val _infoPanel = MutableLiveData<Boolean>()

    private var totalCount = -1

    private var currentPage = 1

    private val loadedGIFs = mutableListOf<GifDto>()

    init {
        Timber.plant(Timber.DebugTree())
    }

    fun initLoad() {
        if (isFirstLoad()) {
            Timber.d("First loading...")
            nextGif()
        }
    }

    fun refresh() {
        if (_panel.value?.refreshButton == true) {
            loadGifByPageSection()
        }
    }

    fun doInfoPanel() {
        if (_infoPanel.value != true) {
            _infoPanel.postValue(true)
        } else {
            _infoPanel.postValue(false)
        }
    }

    fun nextGif() {
        if (hasNext() || isFirstLoad()) {
            Timber.d("Load a next gif...")
            if (needLoadGif()) {
                loadGifByPageSection()
            } else {
                // Передвигаем текщий индекс на 1.
                currentIndex += 1

                // Говорим панели, что рефреш не нужен, а кнопки принимаю соответствующие значения.
                postPanel(backButton = !isFirstCurrentGIF(), nextButton = hasNext())
                // Уведомляем о новом состоянии:
                postState(Event.SUCCESS, gifDto = loadedGIFs.elementAt(currentIndex))
            }
        } else {
            Timber.w("Unable to upload next gif.")
        }
    }

    fun prevGif() {
        if (!isFirstCurrentGIF()) {
            Timber.d("Load a previous gif...")
            if (_panel.value?.refreshButton == false) {
                currentIndex -= 1
            }
            // Говорим панели, что рефреш не нужен, а кнопки принимаю соответствующие значения.
            postPanel(!isFirstCurrentGIF(), true)
            // Уведомляем о новом состоянии:
            postState(Event.SUCCESS, throwable = null, gifDto = loadedGIFs.elementAt(currentIndex))
        } else {
            Timber.w("Unable to upload previous gif.")
        }
    }

    private fun onFailed(throwable: Throwable) {
        Timber.e(throwable)

        postPanel(backButton = !isFirstCurrentGIF(), refreshButton = true)
        postState(Event.ERROR, throwable)
    }

    private fun onSuccess() {
        currentIndex += 1
        currentPage += 1

        Timber.i("Successful load. New element by index: $currentIndex")

        postPanel(backButton = !isFirstCurrentGIF(), nextButton = hasNext())
        postState(Event.SUCCESS, gifDto = loadedGIFs.elementAt(currentIndex))
    }

    private fun loadRandomGif() {
        if (pageSection != PageSection.RANDOM) {
            Timber.e(
                "Происходит загрузка случайной гифки, в то время когда section = %s",
                pageSection
            )
        }

        return NetworkService.retrofitService()
            .getRandomGif()
            .enqueue(
                object : Callback<GifDto> {
                    override fun onResponse(call: Call<GifDto>, response: Response<GifDto>) {
                        val randomGif = response.body()

                        if (response.code() == 200 && randomGif != null) {
                            loadedGIFs.add(randomGif)
                            onSuccess()
                        } else {
                            onFailed(
                                Throwable(
                                    "Ответ сервера: ${response.code()}; randomGif = $randomGif"
                                )
                            )
                        }
                    }

                    override fun onFailure(call: Call<GifDto>, t: Throwable) {
                        onFailed(t)
                    }
                }
            )
    }

    private fun loadGIFs() {
        if (pageSection == PageSection.RANDOM) {
            Timber.e("Происходит загрузка гифок, в то время когда выбран раздел со случайными")
        }

        return NetworkService.retrofitService()
            .getSectionGIFs(section = pageSection.toString(), page = currentPage)
            .enqueue(
                object : Callback<ResponseWrapperDto> {
                    override fun onResponse(
                        call: Call<ResponseWrapperDto>,
                        responseDto: Response<ResponseWrapperDto>
                    ) {
                        val responseWrapper = responseDto.body()
                        if (responseDto.code() == 200 &&
                                responseWrapper != null &&
                                !responseWrapper.result.isNullOrEmpty()
                        ) {
                            loadedGIFs.addAll(responseWrapper.result)
                            totalCount = responseWrapper.totalCount
                            onSuccess()
                        } else {
                            onFailed(
                                Throwable(
                                    "Ответ сервера: ${responseDto.code()}; " +
                                        "res = ${responseWrapper?.result}"
                                )
                            )
                        }
                    }

                    override fun onFailure(call: Call<ResponseWrapperDto>, t: Throwable) {
                        onFailed(t)
                    }
                }
            )
    }

    private fun loadGifByPageSection() {
        _panel.postValue(Panel(backButton = false, nextButton = false, refreshButton = false))
        postState(Event.LOADING, throwable = null, gifDto = null)

        return when (pageSection) {
            PageSection.RANDOM -> loadRandomGif()
            else -> loadGIFs()
        }
    }

    private fun needLoadGif(): Boolean {
        return loadedGIFs.isEmpty() || loadedGIFs.size - currentIndex <= 1
    }

    private fun isFirstCurrentGIF(): Boolean {
        return currentIndex <= 0
    }

    private fun isFirstLoad(): Boolean {
        return currentIndex == -1
    }

    private fun hasNext(): Boolean {
        return when (pageSection) {
            PageSection.RANDOM -> loadedGIFs.isNotEmpty()
            else -> loadedGIFs.isNotEmpty() && totalCount - currentIndex > 1
        }
    }

    private fun postState(event: Event, throwable: Throwable? = null, gifDto: GifDto? = null) {
        Timber.d(
            "Post was been updated. Event = %s, Throwable = %s, Data = %s",
            event,
            throwable.toString(),
            gifDto
        )

        _gifState.postValue(State(event = event, throwable = throwable, data = gifDto))
    }

    private fun postPanel(
        backButton: Boolean = false,
        nextButton: Boolean = false,
        refreshButton: Boolean = false
    ) {
        Timber.d(
            "Panel was been updated. Back Button = %s, Next Button = %s, Refresh Button = %s",
            backButton,
            nextButton,
            refreshButton
        )

        _panel.postValue(
            Panel(backButton = backButton, nextButton = nextButton, refreshButton = refreshButton)
        )
    }
}
