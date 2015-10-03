package xiaoguang.tools;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Display;

import java.io.ByteArrayOutputStream;

import xiaoguang.notepad.AddNoteActivity;

/**
 * Created by xiaoguang on 2015/9/14.
 */
public class BitmapTools {
    /**
     * 对图片进行按比例设置
     * @param bitmap 要处理的图片
     * @return 返回处理好的图片
     */
    public static Bitmap RawgetScaleBitmap(Bitmap bitmap, float widthScale, float heightScale){
        Matrix matrix = new Matrix();
        matrix.postScale(widthScale, heightScale);
        if(bitmap == null){
            return null;
        }
        Bitmap resizeBmp  =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,true);
        return resizeBmp;
    }
    public static Bitmap getScaleBitmap(Bitmap bitmap, float widthScale, float heightScale){
        Matrix matrix = new Matrix();
        matrix.postScale(widthScale, heightScale);
        if(bitmap == null){
            return null;
        }
        final Display display = AddNoteActivity.addNoteActivity.getWindow().getWindowManager().getDefaultDisplay();
        if (display != null)
        {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            // 获取屏幕大小
            int screenWidth = display.getWidth();
            int screenHeight = display.getHeight();
            if( (w < screenWidth / 2) && (h < screenHeight / 2) )
                return bitmap;
            //图片少于屏幕的一半就不剪了
        }

        Bitmap resizeBmp  =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight()-1, matrix,true);
        return resizeBmp;
    }
    public static Bitmap getScaleBitmap(Bitmap bitmap, int screenWidth,
                                   int screenHight) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix matrix = new Matrix();
        float scale = (float) screenWidth / w;
        float scale2 = (float) screenHight / h;

        //取比例小的值 可以把图片完全缩放在屏幕内
        scale = scale < scale2 ? scale : scale2;

        // 都按照宽度scale 保证图片不变形.根据宽度来确定高度
        matrix.postScale(scale, scale);
        // w,h是原图的属性.
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }
    public static byte[] bitmap2Bytes(Bitmap bitmap){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,os);
        return os.toByteArray();
    }

}
