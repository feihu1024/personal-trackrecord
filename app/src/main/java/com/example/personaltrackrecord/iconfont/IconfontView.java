package com.example.personaltrackrecord.iconfont;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class IconfontView extends TextView {
    public IconfontView(Context context) {
        super(context);
        init(context);
    }

    public IconfontView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconfontView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * 初始化
     * @param context
     */
    private void init(Context context) {
        //设置字体图标
        Typeface font = Typeface.createFromAsset(context.getAssets(), "iconfont.ttf");
        this.setTypeface(font);
    }
}
