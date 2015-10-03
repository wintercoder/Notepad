package xiaoguang.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xiaoguang.notepad.AddNoteActivity;
import xiaoguang.notepad.DbManager;
import xiaoguang.notepad.MainActivity;

/**
 * Created by xiaoguang on 2015/9/14.
 */
public class SpanText extends EditText {

    public SpanText(Context context) {
        super(context);
    }
    public SpanText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 让图片以路径形式显示
     * @param imgPath
     */
    public void addImgToText(String imgPath){
        if(getText().length() == 0){
            this.setText(" ");
        }
        SpannableString ss = new SpannableString(imgPath);
        this.append(ss);
        setPicText(this.getText().toString());
        this.setSelection(this.getText().toString().length());  //光标位置
    }

    /**
     * 将路径显示为图片
     * @param content
     */
    public void setPicText(String content){
        SpannableString s1 = new SpannableString(content);
        String patternStr = Constants.PicPatten;
        Pattern pattern = Pattern.compile(patternStr);
        Matcher mc = pattern.matcher(content);
        while (mc.find()){
            Bitmap bitmap = BitMapUtil.getBitmap(mc.group(),Constants.SPANTEXT_PIC_WIDTH,Constants.SPANTEXT_PIC_HEIGHT);
            ImageSpan imgSpan = new ImageSpan(bitmap, ImageSpan.ALIGN_BASELINE);
            s1.setSpan(imgSpan, mc.start(), mc.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        this.setText(s1);
    }
}



