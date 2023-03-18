package com.kiylx.crashertools.utils

import android.app.Application
import android.content.ComponentName
import android.content.Intent


/**
 * 创建一个由ApplicationContext可以启动启动任意activity的intent
 */
fun <T : Any?> Application.buildIntent(cls: Class<T>): Intent {
    val componentName =
        ComponentName(packageName, cls.canonicalName)
    val intent = Intent()
    intent.component = componentName
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    return intent
}

