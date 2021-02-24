package kr.co.aiblab.cordova.plugin.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

@Suppress("UNCHECKED_CAST")
fun <F : Fragment> AppCompatActivity.findFragment(fragmentClass: Class<F>): F? =
    (supportFragmentManager.fragments.first() as NavHostFragment)
        .childFragmentManager.fragments.find {
            fragmentClass.isAssignableFrom(it.javaClass)
        }?.let { it as F }