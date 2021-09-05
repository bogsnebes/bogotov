package com.bogotov.prog_gifs.ui.page

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bogotov.prog_gifs.R
import com.bogotov.prog_gifs.data.EventObserver
import com.bogotov.prog_gifs.data.RequestDrawable
import com.bogotov.prog_gifs.data.dto.GifDto
import com.bogotov.prog_gifs.databinding.FragmentPageBinding
import com.bogotov.prog_gifs.domain.Event
import com.bogotov.prog_gifs.domain.PageInfo
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

internal class PageFragment : Fragment(), EventObserver<GifDto> {

    private val messageErrorSnackBarText by lazy {
        binding.root.resources.getString(R.string.error_snackbar_text)
    }

    private val messageErrorGifText by lazy {
        binding.root.resources.getString(R.string.error_gif_text)
    }

    private var gifAnimateDuration: Int = DEFAULT_ANIMATE_DURATION

    // По примеру из документации
    private var _binding: FragmentPageBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var pageViewModel: PageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Достаём информацию о странице.
        val pageInfo = arguments?.getParcelable<PageInfo>(ARG_PAGE_INFO)

        // Инициализируем ViewModel
        pageViewModel =
            ViewModelProvider(this).get(PageViewModel::class.java).apply {
                pageInfo?.let { this.pageSection = it.pageSection }
            }
        // Инициализируем логгер.
        Timber.plant(Timber.DebugTree())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPageBinding.inflate(layoutInflater)

        pageViewModel.gifDtoState.observe(viewLifecycleOwner) {
            when (it.event) {
                Event.ERROR -> onError(it.throwable)
                Event.SUCCESS -> onSuccess(it.data!!)
                Event.LOADING -> onLoading()
            }
        }

        // Выставляем смотрителя для панели кнопок.
        pageViewModel.panel.observe(viewLifecycleOwner) {
            binding.fabBack.isEnabled = it.backButton
            binding.fabNext.isEnabled = it.nextButton
            binding.fabRefresh.isEnabled = it.refreshButton
        }

        binding.cardView.animate().alpha(0.0f)
        pageViewModel.infoPanel.observe(viewLifecycleOwner) {
            if (it) {
                binding.cardView.animate().alpha(0.6f)
            } else {
                binding.cardView.animate().alpha(0.0f)
            }
        }

        // Выставляем кнопкам слушателей:
        binding.fabBack.setOnClickListener { pageViewModel.prevGif() }
        binding.fabNext.setOnClickListener { pageViewModel.nextGif() }
        binding.fabRefresh.setOnClickListener { pageViewModel.refresh() }
        binding.fabInfo.setOnClickListener { pageViewModel.doInfoPanel() }
        // Проводим загрузку
        pageViewModel.initLoad()

        Timber.d("onCreateView was been successful")
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        gifAnimateDuration =
            PreferenceManager.getDefaultSharedPreferences(binding.root.context)
                .getInt("duration", DEFAULT_ANIMATE_DURATION)
    }

    override fun onSuccess(data: GifDto) {
        // Заполняем карточку:
        binding.textViewDescription.text = data.description
        "Дата: ${data.date}".also { binding.textViewDate.text = it }
        "Автор: ${data.author}".also { binding.textViewAuthor.text = it }
        binding.progressBar.visibility = View.VISIBLE

        Glide.with(binding.root)
            .load(data.gifUrl)
            .transition(
                GenericTransitionOptions.with { view ->
                    view.alpha = 0f
                    val fadeAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
                    fadeAnim.duration = gifAnimateDuration.toLong()
                    fadeAnim.start()
                }
            )
            .diskCacheStrategy(CACHE_STRATEGY)
            .thumbnail(Glide.with(binding.root).load(data.previewUrl))
            .listener(
                RequestListenerImpl(
                    object : RequestDrawable {
                        override fun onLoadFailed() {
                            binding.progressBar.visibility = View.GONE
                            showErrorSnackBar()
                        }

                        override fun onResourceReady() {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                )
            )
            .error(R.drawable.error_cat)
            .centerCrop()
            .into(binding.imageViewGif)
    }

    override fun onLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onError(throwable: Throwable?) {
        if (throwable == null) {
            Timber.w("Throwable is null.")
        } else {
            Timber.d(throwable)
        }

        Glide.with(binding.root).load(R.drawable.error_cat).centerCrop().into(binding.imageViewGif)

        binding.textViewDescription.text = messageErrorGifText
        binding.textViewDate.text = ""
        binding.textViewAuthor.text = ""
        binding.progressBar.visibility = View.GONE

        showErrorSnackBar()
    }

    private fun showErrorSnackBar() {
        Timber.d("Был вызван метод showErrorSnackBar.")

        if (isVisible) {
            Snackbar.make(binding.root, messageErrorSnackBarText, Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val DEFAULT_ANIMATE_DURATION: Int = 1000
        private val CACHE_STRATEGY: DiskCacheStrategy = DiskCacheStrategy.ALL
        private const val ARG_PAGE_INFO = "page_info"

        @JvmStatic
        fun newInstance(pageInfo: Parcelable): PageFragment {
            return PageFragment().apply {
                arguments = Bundle().apply { putParcelable(ARG_PAGE_INFO, pageInfo) }
            }
        }
    }
}
