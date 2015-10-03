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
     * 图库获取后调用，将图片以<pic_id>标记的形式加到文本里
     * @param bitmap
     * @param lastId
     */
    public void addImgToText(Bitmap bitmap,int lastId){
        if(getText().length() == 0){
            this.setText(" ");
        }
//        ImageSpan span = new ImageSpan(bitmap,ImageSpan.ALIGN_BASELINE);
        String s1 = "<pic_"+lastId+">";
        SpannableString ss = new SpannableString(s1);
//        ss.setSpan(span, 0, s1.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        this.append(ss);
        setSpanContentPic_Cam(this.getText().toString());
        this.setSelection(this.getText().toString().length());  //设置光标位置
    }

    /**
     * 拍照调用它，让文本框保存图片路径，再通过setSpanContentPic_Cam()使路径处的图片显示
     * @param bitmap
     * @param imgPath
     */
    public void addImgToText(Bitmap bitmap,String imgPath){
        ImageSpan span = new ImageSpan(bitmap,ImageSpan.ALIGN_BASELINE);
        SpannableString ss = new SpannableString(imgPath);
        ss.setSpan(span, 0, imgPath.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        this.append(ss);
        setSpanContentPic_Cam(this.getText().toString());
        this.setSelection(this.getText().toString().length());
    }

    /**
     * 用正则分别匹配<pic_id>和拍照的图片路径，显示出对应的图片
     * @param content
     */
    public void setSpanContentPic_Cam(String content){
        //匹配图库图片<pic_id>
        String patternPicStr = "<pic_" + "\\d*"+">";
        Pattern patternPic = Pattern.compile(patternPicStr);
        Matcher mPic = patternPic.matcher(content);
        SpannableString ssPic = new SpannableString(content);
        while(mPic.find()){
            String patStr = "[\\d]+";
            Matcher mc = Pattern.compile(patStr).matcher(mPic.group());
            if(mc.find()){
                DbManager dbManager = new DbManager(AddNoteActivity.addNoteActivity);
                Bitmap map = dbManager.getPicById(Integer.parseInt(mc.group()));
                Bitmap bitmap = BitmapTools.getScaleBitmap(map, 1f,1f);
                ImageSpan imgSpan = new ImageSpan(bitmap, ImageSpan.ALIGN_BASELINE);
                ssPic.setSpan(imgSpan, mPic.start(), mPic.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                if(map.toString() != bitmap.toString()){
//                    map.recycle();
//                }
            }
        }
//        this.setText(ssPic);
        //匹配拍照来的图片路径
        String patternCamStr = Environment.getExternalStorageDirectory()
                + "/" + Constants.IMG_DIR + "/.+?\\.\\w{3}";
        Pattern patternCam = Pattern.compile(patternCamStr);
        Matcher mCam = patternCam.matcher(ssPic);   //在Pic的基础上
        SpannableString ssCam = new SpannableString(ssPic);
        while(mCam.find()){
            Bitmap bmpCam = BitmapFactory.decodeFile(mCam.group());
            Bitmap bitmapCam = BitmapTools.getScaleBitmap(bmpCam, 0.4f, 0.3f);
            ImageSpan imgSpan = new ImageSpan(bitmapCam, ImageSpan.ALIGN_BASELINE);
            ssCam.setSpan(imgSpan, mCam.start(), mCam.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if(bmpCam.toString() != bitmapCam.toString()){
                bmpCam.recycle();
            }
        }
        this.setText(ssCam);

    }
}



