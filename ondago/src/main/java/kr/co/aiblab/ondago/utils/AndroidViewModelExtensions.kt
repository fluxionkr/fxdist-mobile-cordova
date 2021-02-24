package kr.co.aiblab.ondago.utils

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

fun AndroidViewModel.getContext(): Context = getApplication<Application>().applicationContext
