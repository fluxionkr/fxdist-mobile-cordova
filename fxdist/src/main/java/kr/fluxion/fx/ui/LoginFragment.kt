package kr.fluxion.fx.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.orhanobut.logger.Logger
import kr.fluxion.fx.data.*
import kr.fluxion.fx.utils.UserDataPrefHelper
import kr.fluxion.fx.utils.showToast
import kr.fluxion.fx.viewmodels.LoginViewModel
import kr.fluxion.fx.viewmodels.LoginViewModelFactory
import kr.fluxion.fx.ui.LoginFragmentDirections.Companion.actionLoginToMain
import kr.fluxion.fx.R
import kr.fluxion.fx.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(
            application = requireActivity().application,
            repository = LoginRepository.getInstance(
                UserDataPrefHelper.getInstance(requireContext().applicationContext)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@LoginFragment.viewModel
            loginClickListener = View.OnClickListener { login() }
        }
        context ?: return binding.root
        subscribeUI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
        startTransitions()

        // FIXME ????????? ????????? ??????
        binding.aetUserId.setText("del1")
        binding.aetPassword.setText("1")

        viewModel.autoLogin()
    }

    private fun startTransitions() {
        binding.executePendingBindings()
        startPostponedEnterTransition()
    }

    private fun login() {
        val id = binding.aetUserId.text.toString()
        val password = binding.aetPassword.text.toString()
        val isAutoLogin = binding.cbLoginAutomatically.isChecked
        try {
            with(LoginData(id, password, isAutoLogin)) {
                checkValidLoginData(this)
                viewModel.login(this)
            }
        } catch (e: IllegalStateException) {
            Logger.w("${e.message}")
            showToast(e.message)
        }
    }

    @Suppress("COMPATIBILITY_WARNING")
    private fun subscribeUI() {
        viewModel.navigateToMainDest().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { goMain(it) }
        }

        viewModel.handleNetworkState().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { networkState ->
                when (networkState) {
                    NetworkState.LOADING -> Logger.d("NetworkState::LOADING....")
                    NetworkState.LOADED -> Logger.d("NetworkState::LOADED!!!!")
                    else -> {
                        Logger.d("NetworkState::ERROR!!!! :(")
                        if (networkState.status == Status.FAILED) {
                            networkState.msg?.let { showToast(it) }
                        }
                    }
                }
            }
        }

        viewModel.handleError().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let {
                val message = when (it.code) {
                    ErrorCode.Auth.EMPTY_DATA -> "????????? ????????? ??????????????????."
                    ErrorCode.Auth.FAILED -> "???????????? ??????????????????."
                    else -> it.message
                }
                Logger.w("Error: $it >> $message")
                showToast(message)
            }
        }
    }

    @Throws(IllegalStateException::class)
    private fun checkValidLoginData(loginData: LoginData) = with(loginData) {
        check(userId.isEmpty().not()) { getString(R.string.login_error_msg_no_user_id) }
        check(password.isEmpty().not()) { getString(R.string.login_error_msg_no_password) }
    }

    private fun goMain(userData: UserData): Unit =
        findNavController().navigate(actionLoginToMain(userData))
}
