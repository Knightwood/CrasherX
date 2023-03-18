package com.kiylx.crasherx

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.kiylx.crashertools.CrashHelper

/**
 *
 * 　　　　　　　　┏┓　　　┏┓
 * 　　　　　　　┏┛┻━━━┛┻┓
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃　　　━　　　┃
 * 　　　　　　　┃　＞　　　＜　┃
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃...　⌒　...　┃
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┗━┓　　　┏━┛
 * 　　　　　　　　　┃　　　┃　Code is far away from bug with the animal protecting
 * 　　　　　　　　　┃　　　┃   神兽保佑,代码无bug
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┗━━━┓
 * 　　　　　　　　　┃　　　　　　　┣┓
 * 　　　　　　　　　┃　　　　　　　┏┛
 * 　　　　　　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　　　　　　┃┫┫　┃┫┫
 * 　　　　　　　　　　┗┻┛　┗┻┛
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        mContext = this
        CrashHelper.instance(this){
//            email="your email address" //设置邮件地址
//            clazz=CrashActivity::class.java //设置当crash时跳转到哪个activity
//            isBackground=false//true：crash后发送一条通知到通知栏，点击后跳转activity
//            isStackOverflow=false//是否跳转浏览器进行搜索
//            stackOverflowUrl=""//设置跳转浏览器的搜索引擎
//            onlyUseInDebug=false//是否只在debug下使用
//            isCrashActivity=false//crash后是否跳转activity
        }
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context
    }

}