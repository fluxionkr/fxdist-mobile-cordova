package kr.co.kit.kitdist.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.navigation.fragment.findNavController
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch
import kr.co.kit.kitdist.R
import kr.co.kit.kitdist.data.ErrorCode.Common.Companion.APP_ROOTED
import kr.co.kit.kitdist.databinding.FragmentSplashBinding
import kr.co.kit.kitdist.ui.ConfirmDialogFragment.Companion.ENTRY_KEY_CONFIRM
import kr.co.kit.kitdist.ui.ConfirmDialogFragment.Companion.RESULT_KEY_ID
import kr.co.kit.kitdist.ui.ConfirmDialogFragment.Companion.RESULT_KEY_IS_CONFIRM

import kr.co.kit.kitdist.viewmodels.SplashViewModel
import kr.co.kit.kitdist.ui.SplashFragmentDirections.Companion.actionOpenDialog

class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding

    private val viewModel: SplashViewModel by viewModels()

    init {
        lifecycleScope.launch {
            whenCreated {
                viewModel.doSplashTask()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        context ?: return binding.root
        subscribeUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleNavInteraction()
    }

    private fun goMain() {
        // FIXME
//        findNavController().navigate(actionSplashToMain(UserData("SEOJAEHWA")))
    }

    @Suppress("COMPATIBILITY_WARNING")
    private fun subscribeUI() {
        viewModel.result.observe(viewLifecycleOwner) {
            if (it) goMain()
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { error ->
                when (error.code) {
                    APP_ROOTED -> {
                        // TODO show confirm dialog and finish app
                        Logger.e("App is rooted. finish app. bye~")
                        showFinishAppConfirmDialog("루팅되었음")
                    }
                }
            }
        }
    }

    private fun handleNavInteraction() {
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.flow_splash_dest)
        val observer = LifecycleEventObserver { _, event ->
            event.takeIf { it == Lifecycle.Event.ON_RESUME }?.apply {
                val stateHandle = navBackStackEntry.savedStateHandle
                stateHandle.get<Bundle>(ENTRY_KEY_CONFIRM)?.let { result ->
                    when (result.getInt(RESULT_KEY_ID)) {
                        DIALOG_ID_CONFIRM_FINISH_APP -> if (result.getBoolean(RESULT_KEY_IS_CONFIRM)) {
                            requireActivity().finish()
                        }
                    }
                }
                stateHandle.remove<Bundle>(ENTRY_KEY_CONFIRM)
            }
        }
        navBackStackEntry.lifecycle.addObserver(observer)

        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            event.takeIf { it == Lifecycle.Event.ON_DESTROY }?.apply {
                navBackStackEntry.lifecycle.removeObserver(observer)
            }
        })
    }

    private fun showFinishAppConfirmDialog(message: String): Unit = findNavController().navigate(
        actionOpenDialog(
            id = DIALOG_ID_CONFIRM_FINISH_APP,
            title = "알림",
            message = message
        )
    )

    companion object {
        const val DIALOG_ID_CONFIRM_FINISH_APP = 101
    }
}