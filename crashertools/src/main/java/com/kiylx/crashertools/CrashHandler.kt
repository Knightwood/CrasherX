package com.kiylx.crashertools

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.kiylx.crashertools.utils.CrashUtils
import com.kiylx.crashertools.utils.buildIntent
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.concurrent.thread
import kotlin.system.exitProcess

/**
 * 全局捕获异常
 */
class CrashHandler private constructor(private val application: Application) :
    Thread.UncaughtExceptionHandler {
    private var mCustomExpHandler: CustomExceptionHandler? = null
    private var mDefaultCrashHandler: Thread.UncaughtExceptionHandler? = null

    init {
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    fun registerHandler(customExceptionHandler: CustomExceptionHandler): CrashHandler {
        this.mCustomExpHandler = customExceptionHandler
        return this
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        mCustomExpHandler?.let {
            if (!it.process(application, t, e)) {//如果返回false(用户不处理异常)，交给系统处理
                mDefaultCrashHandler?.uncaughtException(t, e)
            } else {//用户已处理了异常，退出进程
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    Log.e(TAG, "error : ", e)
                }
                //退出程序
                Process.killProcess(Process.myPid())
                exitProcess(1)
            }
        } ?: let {
            mDefaultCrashHandler?.uncaughtException(t, e)
        }
    }

    companion object {
        const val TAG = "tty1-CrashHandler"

        @Volatile
        private var crashHandler: CrashHandler? = null
        fun crashHandlerInstance(context: Application): CrashHandler =
            crashHandler ?: synchronized(this) {
                crashHandler ?: CrashHandler(context)
            }
    }

    /**
     * 自定义处理异常
     */
    interface CustomExceptionHandler {
        fun process(application: Application, t: Thread, e: Throwable): Boolean
    }

}

object CrashHelper {
    private lateinit var crashHandler: CrashHandler
    private lateinit var myCrashHandler: MyCrashHandler

    /**
     * 生成crashhandler实例，
     * 重复调用将只进行配置
     */
    fun instance(context: Application, block: MyCrashHandler.Config.() -> Unit) {
        if (!this::myCrashHandler.isInitialized || !this::crashHandler.isInitialized) {
            myCrashHandler = MyCrashHandler()
            crashHandler =
                CrashHandler.crashHandlerInstance(context).registerHandler(myCrashHandler)
        }
        myCrashHandler.config.block()
    }

    fun registerListener(listener: OnCrashListener) {
        if (!this::myCrashHandler.isInitialized) {
            throw Exception("not init")
        }
        myCrashHandler.config.addListener(listener)
    }

    fun unRegisterListener(listener: OnCrashListener) {
        if (!this::myCrashHandler.isInitialized) {
            throw Exception("not init")
        }
        myCrashHandler.config.removeListener(listener)
    }

    fun configCrashHandler(block: MyCrashHandler.Config.() -> Unit) {
        if (this::myCrashHandler.isInitialized) {
            myCrashHandler.config.block()
        }
    }
}

interface OnCrashListener {
    fun onCrash(t: Thread, e: Throwable)
}

/**
 * 自定义异常处理
 */
class MyCrashHandler : CrashHandler.CustomExceptionHandler {
    val config: Config = Config()

    class Config {
        val listeners: MutableList<OnCrashListener> = mutableListOf()

        /**
         * 是否只在debug模式下启用
         */
        var onlyUseInDebug = false

        /**
         * 是否在crash时跳转浏览器进行搜索，url由[stackOverflowUrl]指定
         * 如果同时将[isStackOverflow]和[isCrashActivity]设置为true
         * 则[isStackOverflow]优先级高于跳转activity
         */
        var isStackOverflow = false

        /**
         * 指定在跳转浏览器时使用的搜索引擎地址
         */
        var stackOverflowUrl = "http://stackoverflow.com/search?q=[java][android]"

        /**
         * 是否在crash时跳转activity
         */
        var isCrashActivity = true

        /**
         * 当crash时跳转到哪个activity
         */
        var clazz: Class<out Activity> = CrashActivity::class.java

        /**
         * 是否在crash时仅发送一条通知
         * 点击通知再跳转到[clazz]指定的activity
         */
        var isBackground = false

        var email: String? = null
        var debugMessage: String? = null
        var color: Int? = null

        fun addListener(listener: OnCrashListener): Config {
            listeners.add(listener)
            return this
        }

        fun removeListener(listener: OnCrashListener): Config {
            listeners.remove(listener)
            return this
        }
    }

    override fun process(application: Application, t: Thread, e: Throwable): Boolean {
        Log.e(TAG, e.message, e)
        thread {
            Looper.prepare()
            application.let {
                Toast.makeText(
                    it, "程序出现异常.",
                    Toast.LENGTH_LONG
                ).show()
            }
            if (config.onlyUseInDebug) {
                if (BuildConfig.DEBUG) {
                    myHandle(application, t, e)
                }
            } else {
                myHandle(application, t, e)
            }
            Looper.loop()
        }
        return true
    }

    private fun myHandle(application: Application, t: Thread, e: Throwable) {
        var intent: Intent? = null

        val writer = StringWriter()
        e.printStackTrace(PrintWriter(writer))
        val stack = writer.toString()

        if (config.isStackOverflow) {
            Log.d(
                "Crasher",
                "Exception thrown: " + e.javaClass.name + ". Opening StackOverflow query for \"" + e.message + "\"."
            )
            intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(config.stackOverflowUrl + e.message)
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        } else if (config.isCrashActivity) {
            intent = application.buildIntent(config.clazz)
            intent.putExtra(CrashActivity.EXTRA_NAME, e.javaClass.name)
            intent.putExtra(CrashActivity.EXTRA_MESSAGE, e.localizedMessage)
            intent.putExtra(CrashActivity.EXTRA_STACK_TRACE, stack)
            if (config.email != null) intent.putExtra(CrashActivity.EXTRA_EMAIL, config.email)
            if (config.debugMessage != null) intent.putExtra(
                CrashActivity.EXTRA_DEBUG_MESSAGE,
                config.debugMessage
            )
            if (config.color != null) intent.putExtra(CrashActivity.EXTRA_COLOR, config.color)
        }

        if (intent != null) {
            if (config.isBackground) {
                val manager =
                    application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val builder: NotificationCompat.Builder =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        manager.createNotificationChannel(
                            NotificationChannel(
                                "crashNotifications",
                                application.getString(R.string.title_crasher_crash_notifications),
                                NotificationManager.IMPORTANCE_DEFAULT
                            )
                        )
                        NotificationCompat.Builder(application, "crashNotifications")
                    } else NotificationCompat.Builder(application)
                manager.notify(
                    0,
                    builder.setContentTitle(
                        String.format(
                            application.getString(R.string.title_crasher_crash_notification),
                            application.getString(R.string.app_name),
                            e.javaClass.name,
                            CrashUtils.getCause(application, stack)
                        )
                    )
                        .setContentText(application.getString(R.string.msg_crasher_crash_notification))
                        .setSmallIcon(R.drawable.ic_crasher_bug)
                        .setContentIntent(PendingIntent.getActivity(application, 0, intent, 0))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build()
                )

            } else {
                application.startActivity(intent)
            }
        }

        for (listener in config.listeners) {
            listener.onCrash(t, e)
        }
    }

    companion object {
        const val TAG = "tty1-MyCrashHandler"
    }
}