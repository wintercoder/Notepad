package xiaoguang.notepad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.util.List;

import xiaoguang.tools.BitMapUtil;
import xiaoguang.tools.Constants;


/**
 * Created by xiaoguang on 2015/9/24.
 */
public class DbManager {
    private Context context = null;
    private DbHelper helper = null;
    private SQLiteDatabase dbReadable = null;
    private SQLiteDatabase dbWritable = null;

    public DbManager(Context context) {
        this.context = context;
        helper = new DbHelper(context, Constants.DB_NAME, null, 1);
        dbReadable = helper.getReadableDatabase();
        dbWritable = helper.getWritableDatabase();
    }

    public SQLiteDatabase getDbReadable() {
        return dbReadable;
    }

    public SQLiteDatabase getDbWritable() {
        return dbWritable;
    }

    public void dbWriting(String icoPath, String content, String time) {
        ContentValues values = new ContentValues();
        values.put(Constants.KEY_IMAGE, icoPath);
        values.put(Constants.KEY_CONTENT, content);
        values.put(Constants.KEY_TIME, time);
        dbWritable.insert(Constants.TABLE_NAME, null, values);
        values.clear();
    }

    public void getDataList(List<ItemBean> list) {  //改地址里的内容
        Cursor cursor
                = dbReadable.rawQuery("SELECT * FROM " + Constants.TABLE_NAME, null);
        try {
            while (cursor.moveToNext()) {
                ItemBean note = new ItemBean();
                note.setId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_ID)));
                Bitmap bitmap = BitMapUtil.getBitmap(cursor.getString(cursor.getColumnIndex(Constants.KEY_IMAGE)), Constants.ICO_PIC_WIDTH, Constants.ICO_PIC_HEIGHT);
                note.setImage(bitmap);
                note.setContent(cursor.getString(cursor.getColumnIndex(Constants.KEY_CONTENT)));
                note.setTime(cursor.getString(cursor.getColumnIndex(Constants.KEY_TIME)));
                list.add(note);
            }
        } catch (Exception e) {
        }finally {
            cursor.close();
        }
    }

    /**
     * 数据库中获得指定ID的笔记内容
     * @param id
     * @return 笔记对象
     */
    public ItemBean getData(int id){
        Cursor cursor = dbReadable.rawQuery("SELECT * FROM "
                + Constants.TABLE_NAME + " WHERE "
                + Constants.KEY_ID + " = ?", new String[]{id+""});
        if(!cursor.moveToFirst())
            return null;
        ItemBean note = new ItemBean();
        note.setId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_ID)));
        Bitmap bitmap = BitMapUtil.getBitmap(cursor.getString(cursor.getColumnIndex(Constants.KEY_IMAGE)), Constants.ICO_PIC_WIDTH, Constants.ICO_PIC_HEIGHT);
        note.setImage(bitmap);
        note.setContent(cursor.getString(cursor.getColumnIndex(Constants.KEY_CONTENT)));
        note.setTime(cursor.getString(cursor.getColumnIndex(Constants.KEY_TIME)));
        cursor.close();
        return note;
    }

    /**
     * 实时更新图标的Update
     * @param id
     * @param path
     * @param content
     * @param time
     */
    public void updateData(int id,String path,String content,String time){
        ContentValues cv = new ContentValues();
        cv.put(Constants.KEY_IMAGE,path);
        cv.put(Constants.KEY_CONTENT, content);
        cv.put(Constants.KEY_TIME, time);
        dbWritable.update(Constants.TABLE_NAME, cv, Constants.KEY_ID + "=?", new String[]{id + ""});
    }

    /**
     * 不修改图标的Update
     * @param id
     * @param content
     * @param time
     */
    public void updateData(int id,String content,String time){
        ContentValues cv = new ContentValues();
        cv.put(Constants.KEY_CONTENT, content);
        cv.put(Constants.KEY_TIME, time);
        dbWritable.update(Constants.TABLE_NAME, cv, Constants.KEY_ID + "=?", new String[]{id + ""});
    }

    public void delNoteById(int id){
        dbWritable.execSQL("DELETE FROM "+ Constants.TABLE_NAME + " WHERE "
                + Constants.TABLE_ID + " = ?", new String[]{id+""});
    }
}