package com.kiylx.crashertools;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.kiylx.crashertools.utils.CrashUtils;
import com.kiylx.crashertools.utils.ImageUtils;

import java.util.Locale;

public class CrashActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_NAME = "kiylx.crashertools.EXTRA_NAME";
    public static final String EXTRA_MESSAGE = "kiylx.crashertools.EXTRA_MESSAGE";
    public static final String EXTRA_STACK_TRACE = "kiylx.crashertools.EXTRA_STACK_TRACE";

    public static final String EXTRA_EMAIL = "kiylx.crashertools.EXTRA_EMAIL";
    public static final String EXTRA_DEBUG_MESSAGE = "kiylx.crashertools.EXTRA_DEBUG_MESSAGE";
    public static final String EXTRA_COLOR = "kiylx.crashertools.EXTRA_COLOR";

    private Toolbar toolbar;
    private ActionBar actionBar;
    private TextView name;
    private TextView message;
    private TextView description;
    private MaterialButton copy;
    private MaterialButton share;
    private MaterialButton email;
    private View stackTraceHeader;
    private ImageView stackTraceArrow;
    private TextView stackTrace;

    private String body;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        toolbar = findViewById(R.id.toolbar);
        name = findViewById(R.id.name);
        message = findViewById(R.id.message);
        description = findViewById(R.id.description);
        copy = findViewById(R.id.copy);
        share = findViewById(R.id.share);
        email = findViewById(R.id.email);
        stackTraceHeader = findViewById(R.id.stackTraceHeader);
        stackTraceArrow = findViewById(R.id.stackTraceArrow);
        stackTrace = findViewById(R.id.stackTrace);

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        //int color = getIntent().getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.colorPrimary));
        //int colorDark = ColorUtils.darkColor(color);
        //boolean isColorDark = ColorUtils.isColorDark(color);

        //toolbar.setBackgroundColor(color);
        //toolbar.setTitleTextColor(isColorDark ? Color.WHITE : Color.BLACK);
        toolbar.setTitleTextColor(Color.WHITE);

        //copy.setBackgroundColor(colorDark);
        //copy.setTextColor(colorDark);
        copy.setOnClickListener(this);

        //share.setBackgroundColor(colorDark);
        //share.setTextColor(colorDark);
        share.setOnClickListener(this);

        //email.setBackgroundColor(colorDark);
        //email.setTextColor(colorDark);
        if (getIntent().hasExtra(EXTRA_EMAIL)) {
            email.setOnClickListener(this);
        } else email.setVisibility(View.GONE);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(String.format(Locale.getDefault(), getString(R.string.title_crasher_crashed), getString(R.string.app_name)));
            //actionBar.setHomeAsUpIndicator(ImageUtils.getVectorDrawable(this, R.drawable.ic_crasher_back, isColorDark ? Color.WHITE : Color.BLACK));
            actionBar.setHomeAsUpIndicator(ImageUtils.getVectorDrawable(this, R.drawable.ic_crasher_back, Color.WHITE ));
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(colorDark);
            getWindow().setNavigationBarColor(colorDark);
        }*/

        String stack = getIntent().getStringExtra(EXTRA_STACK_TRACE);
        String stackCause = CrashUtils.getCause(this, stack);

        String nameString = getIntent().getStringExtra(EXTRA_NAME) + (stackCause != null ? " at " + stackCause : "");
        String messageString = getIntent().getStringExtra(EXTRA_NAME);

        name.setText(nameString);
        if (messageString != null && messageString.length() > 0)
            message.setText(messageString);
        else message.setVisibility(View.GONE);

        description.setText(String.format(Locale.getDefault(), getString(R.string.msg_crasher_crashed), getString(R.string.app_name)));

        stackTrace.setText(stack);
        stackTraceHeader.setOnClickListener(this);
        if (BuildConfig.DEBUG)
            stackTraceHeader.callOnClick();

        body = nameString + "\n" + (messageString != null ? messageString : "") + "\n\n" + stack
                + "\n\nAndroid Version: " + Build.VERSION.SDK_INT
                + "\nDevice Manufacturer: " + Build.MANUFACTURER
                + "\nDevice Model: " + Build.MODEL
                + "\n\n" + (getIntent().hasExtra(EXTRA_DEBUG_MESSAGE) ? getIntent().getStringExtra(EXTRA_DEBUG_MESSAGE) : "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.copy) {
            Object service = getSystemService(CLIPBOARD_SERVICE);
            if (service instanceof android.content.ClipboardManager)
                ((android.content.ClipboardManager) service).setPrimaryClip(ClipData.newPlainText(name.getText().toString(), stackTrace.getText().toString()));
            else if (service instanceof android.text.ClipboardManager)
                ((android.text.ClipboardManager) service).setText(stackTrace.getText().toString());
            Toast.makeText(this,getString(R.string.has_copied_text),Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, body);

            startActivity(Intent.createChooser(intent, getString(R.string.title_crasher_share)));
        } else if (v.getId() == R.id.email) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setDataAndType(Uri.parse("mailto:" + getIntent().getStringExtra(EXTRA_EMAIL)),"text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, getIntent().getStringExtra(EXTRA_EMAIL));
            intent.putExtra(Intent.EXTRA_SUBJECT, String.format(Locale.getDefault(), getString(R.string.title_crasher_exception), name.getText().toString(), getString(R.string.app_name)));
            intent.putExtra(Intent.EXTRA_TEXT, body);

            startActivity(Intent.createChooser(intent, getString(R.string.title_crasher_send_email)));
        } else if (v.getId() == R.id.stackTraceHeader) {
            if (stackTrace.getVisibility() == View.GONE) {
                stackTrace.setVisibility(View.VISIBLE);
                stackTraceArrow.animate().scaleY(-1).start();
            } else {
                stackTrace.setVisibility(View.GONE);
                stackTraceArrow.animate().scaleY(1).start();
            }
        }
    }
}
