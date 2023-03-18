package com.kiylx.crashertools.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentActivity

/*
 * 创建者 kiylx
 * 创建时间 2022/9/7 20:06
 * 描述：
*/


fun FragmentActivity.setWindowEdgeToEdge(
    rootLayout: View,
    topIds: Array<Int> = emptyArray(),
    alsoApplyNavigationBar: Boolean = true,
    bottomIds: Array<Int> = emptyArray(),
) {
    setWindowEdgeToEdge(
        rootLayout = rootLayout,
        topIds = topIds,
        stateBarColor = Color.TRANSPARENT,
        navBarColor = Color.TRANSPARENT,
        alsoApplyNavigationBar = alsoApplyNavigationBar,
        bottomIds = bottomIds
    )
}

/**
 * 使内容布局拓展到状态栏下面
 * @param rootLayout 根布局
 * @param topIds 当内容布局拓展到状态栏下面时，需要偏移的视图的布局id集合
 * @param alsoApplyNavigationBar true:让视图延展到底部导航栏
 */
fun FragmentActivity.setWindowEdgeToEdge(
    rootLayout: View,
    topIds: Array<Int> = emptyArray(),
    stateBarColor: Int,
    navBarColor: Int,
    alsoApplyNavigationBar: Boolean = true,
    bottomIds: Array<Int> = emptyArray(),
) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = stateBarColor
    if (alsoApplyNavigationBar)
        window.navigationBarColor = navBarColor
    ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view_, windowInsetsCompat ->
        val systemBarInsets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
        val navBarInsets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.navigationBars())
        topIds.forEach {
            view_.findViewById<View>(it).updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBarInsets.top
            }
        }
        bottomIds.forEach {
            view_.findViewById<View>(it).updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = navBarInsets.bottom + 8
            }
        }
        WindowInsetsCompat.CONSUMED
    }
}

/**
 * @param type: 显示或隐藏 状态栏、导航栏软键盘。 WindowInsetsCompat.Type.statusBars() 、navigationBars()、systemBar()
 * @param hide: true:隐藏状态栏，false:显示状态栏
 */
@RequiresApi(Build.VERSION_CODES.R)
fun FragmentActivity.hideShow(
    type: Int = WindowInsetsCompat.Type.statusBars(),
    hide: Boolean = true,
) {
    if (hide)
        window.insetsController?.hide(type)
    else
        window.insetsController?.show(type)
}

/**
 * @param type: 状态栏、导航栏、软键盘等，是否可见。WindowInsetsCompat.Type.statusBars() 、navigationBars()、systemBar()
 */
fun View.isVisibility(
    type: Int = WindowInsetsCompat.Type.statusBars(),
): Boolean {
    return ViewCompat.getRootWindowInsets(this)
        ?.isVisible(type) ?: true
}

/**
 * 给状态栏、导航栏 设置颜色
 * 状态栏底色和字体颜色应该相反才能看清楚。
 * @param isLightStateBar  true：状态栏字体和图标改成黑色。false:状态栏字体和图标改成白色
 * @param alsoApplyNavigationBar true:底部导航栏也变色
 */
fun View.setColor(
    isLightStateBar: Boolean = true,
    alsoApplyNavigationBar: Boolean = true,
) {
    ViewCompat.getWindowInsetsController(this)?.isAppearanceLightStatusBars = isLightStateBar
    if (alsoApplyNavigationBar)
        ViewCompat.getWindowInsetsController(this)?.isAppearanceLightNavigationBars =
            isLightStateBar
}

/**
 * 当前窗口亮度
 * 范围为0~1.0,1.0时为最亮，-1为系统默认设置
 */
var Activity.windowBrightness
    get() = window.attributes.screenBrightness
    set(brightness) {
        //小于0或大于1.0默认为系统亮度
        window.attributes = window.attributes.apply {
            screenBrightness = if (brightness > 1.0 || brightness < 0) -1.0F else brightness
        }
    }

infix fun Activity.brightnessTo(brightness: Float) {
    windowBrightness = brightness
}

/**
 * 隐藏键盘
 */
fun View.hideKeyboard() {
    val v = this
    val manager: InputMethodManager =
        this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (manager != null) {
        manager.hideSoftInputFromWindow(
            v.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
    v.clearFocus()
}