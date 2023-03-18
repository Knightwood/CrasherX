package com.kiylx.crashertools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kiylx.crashertools.databinding.ActivityCrashBinding
import com.kiylx.crashertools.utils.*
import java.util.*

class CrashActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var page: ActivityCrashBinding
    private var actionBar: ActionBar? = null
    private var body: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        page = ActivityCrashBinding.inflate(layoutInflater)
        setContentView(page.root)
        setSupportActionBar(page.toolbar)
        actionBar = supportActionBar

        //颜色设置
        val color =
            intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.colorPrimary))
        val colorDark = ColorUtils.darkColor(color)
        val isColorDark = ColorUtils.isColorDark(color)

        if (isColorDark) {
            page.root.setColor(false)
        } else {
            page.root.setColor(true)
        }
        setWindowEdgeToEdge(
            rootLayout = page.root,
            topIds = arrayOf(page.toolbar.id),
            stateBarColor = color,
            navBarColor = color,
        )

        page.toolbar.setBackgroundColor(color);
        page.toolbar.setTitleTextColor(textColor(isColorDark));

        page.copy.setBackgroundColor(color);
        page.copy.setTextColor(textColor(isColorDark));
        page.copy.setOnClickListener(this)

        page.share.setBackgroundColor(color);
        page.share.setTextColor(textColor(isColorDark));
        page.share.setOnClickListener(this)

        page.email.setBackgroundColor(color);
        page.email.setTextColor(textColor(isColorDark));
        if (intent.hasExtra(EXTRA_EMAIL)) {
            page.email.setOnClickListener(this)
        } else page.email.visibility = View.GONE
        if (actionBar != null) {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar!!.title = String.format(
                Locale.getDefault(), getString(R.string.title_crasher_crashed), getString(
                    R.string.app_name
                )
            )
            //actionBar.setHomeAsUpIndicator(ImageUtils.getVectorDrawable(this, R.drawable.ic_crasher_back, isColorDark ? Color.WHITE : Color.BLACK));
            actionBar!!.setHomeAsUpIndicator(
                ImageUtils.getVectorDrawable(
                    this,
                    R.drawable.ic_crasher_back,
                    Color.WHITE
                )
            )
        }

        val stack = intent.getStringExtra(EXTRA_STACK_TRACE)
        val stackCause = CrashUtils.getCause(this, stack)
        val nameString =
            intent.getStringExtra(EXTRA_NAME) + if (stackCause != null) " at $stackCause" else ""
        val messageString = intent.getStringExtra(EXTRA_NAME)
        page.name.text = nameString
        if ((messageString != null) && messageString.isNotEmpty()) {
            page.message.text = messageString
        } else {
            page.message.visibility = View.GONE
        }
        page.description.text = String.format(
            Locale.getDefault(), getString(R.string.msg_crasher_crashed), getString(
                R.string.app_name
            )
        )
        page.stackTrace.text = stack
        page.stackTraceHeader.setOnClickListener(this)
        if (BuildConfig.DEBUG) page.stackTraceHeader.callOnClick()
        val deviceText = """
            Android Version: ${Build.VERSION.SDK_INT}
            Device Manufacturer: ${Build.MANUFACTURER}
            Device Model: ${Build.MODEL}
            
            ${if (intent.hasExtra(EXTRA_DEBUG_MESSAGE)) intent.getStringExtra(EXTRA_DEBUG_MESSAGE) else ""}
            """.trimIndent()
        body = """
            $nameString
            
            $deviceText
            """.trimIndent()
        page.device.text = deviceText
    }

    private fun textColor(colorDark: Boolean): Int {
        return if (colorDark) Color.WHITE else Color.BLACK
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.copy) {
            val service = getSystemService(CLIPBOARD_SERVICE)
            if (service is ClipboardManager) {
                service.primaryClip = ClipData.newPlainText(
                    page.name.text.toString(), page.stackTrace.text.toString()
                )
            } else if (service is android.text.ClipboardManager) {
                service.text =
                    page.stackTrace.text.toString()
            }
            Toast.makeText(this, getString(R.string.has_copied_text), Toast.LENGTH_SHORT).show()
        } else if (v.id == R.id.share) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, body)
            startActivity(Intent.createChooser(intent, getString(R.string.title_crasher_share)))
        } else if (v.id == R.id.email) {
            val intent = Intent(Intent.ACTION_SENDTO)
            //intent.setType("text/plain");
            intent.data =
                Uri.parse("mailto:" + getIntent().getStringExtra(EXTRA_EMAIL))
            intent.putExtra(Intent.EXTRA_EMAIL, getIntent().getStringExtra(EXTRA_EMAIL))
            intent.putExtra(
                Intent.EXTRA_SUBJECT, String.format(
                    Locale.getDefault(),
                    getString(R.string.title_crasher_exception),
                    page.name.text.toString(),
                    getString(
                        R.string.app_name
                    )
                )
            )
            intent.putExtra(Intent.EXTRA_TEXT, body)
            startActivity(
                Intent.createChooser(
                    intent,
                    getString(R.string.title_crasher_send_email)
                )
            )
        } else if (v.id == R.id.stackTraceHeader) {
            if (page.stackTrace.visibility == View.GONE) {
                page.stackTrace.visibility = View.VISIBLE
                page.stackTraceArrow.animate().scaleY(-1f).start()
            } else {
                page.stackTrace.visibility = View.GONE
                page.stackTraceArrow.animate().scaleY(1f).start()
            }
        }
    }

    companion object {
        const val EXTRA_NAME = "kiylx.crashertools.EXTRA_NAME"
        const val EXTRA_MESSAGE = "kiylx.crashertools.EXTRA_MESSAGE"
        const val EXTRA_STACK_TRACE = "kiylx.crashertools.EXTRA_STACK_TRACE"
        const val EXTRA_EMAIL = "kiylx.crashertools.EXTRA_EMAIL"
        const val EXTRA_DEBUG_MESSAGE = "kiylx.crashertools.EXTRA_DEBUG_MESSAGE"
        const val EXTRA_COLOR = "kiylx.crashertools.EXTRA_COLOR"
    }
}