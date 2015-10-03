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

    public void dbWriting(Bitmap bitmap, String content, String time) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

        ContentValues values = new ContentValues();
        values.put(Constants.KEY_IMAGE, os.toByteArray());
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
                byte[] blob = cursor.getBlob(cursor.getColumnIndex(Constants.KEY_IMAGE));
                note.setImage(BitmapFactory.decodeByteArray(blob, 0, blob.length));
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
        byte[] blob = cursor.getBlob(cursor.getColumnIndex(Constants.KEY_IMAGE));
        note.setImage(BitmapFactory.decodeByteArray(blob, 0, blob.length));
        note.setContent(cursor.getString(cursor.getColumnIndex(Constants.KEY_CONTENT)));
        note.setTime(cursor.getString(cursor.getColumnIndex(Constants.KEY_TIME)));
        cursor.close();
        return note;
    }

    public void updateData(int id,String content,String time){
        ContentValues cv = new ContentValues();
        cv.put(Constants.KEY_CONTENT, content);
        cv.put(Constants.KEY_TIME, time);
        dbWritable.update(Constants.TABLE_NAME, cv, Constants.KEY_ID + "=?", new String[]{id + ""});
    }

    /**
     * 数据库中插入图片，为了等会在文本中显示对应的图片，也顺便返回它的ID
     * @param bitmap
     * @return 这个图片在数据库中的ID
     */
    public int dbInsertPic(Bitmap bitmap){
        ContentValues cv = new ContentValues();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        cv.put(Constants.PIC_IMG, os.toByteArray());
        dbWritable.insert(Constants.PIC_TABLE, null, cv);
        Cursor cursor = dbReadable.rawQuery("select last_insert_rowid() ",null);
//        Cursor cursor = dbReadable.rawQuery("select last_insert_rowid() from "+Constants.PIC_TABLE,null);
        int lastId = -1;
        if(cursor.moveToFirst())
            lastId = cursor.getInt(0);
        cursor.close();
        return lastId;
    }

    public Bitmap getPicById(int id){
        Cursor cursor = dbReadable.rawQuery("SELECT * FROM "
                + Constants.PIC_TABLE + " WHERE "
                + Constants.PIC_ID + " = ?", new String[]{id+""});
        if(!cursor.moveToFirst())
            return null;
        byte[] blob = cursor.getBlob(cursor.getColumnIndex(Constants.PIC_IMG));
        cursor.close();
        return BitmapFactory.decodeByteArray(blob, 0, blob.length);
    }

    public void delNoteById(int id){
        dbWritable.execSQL("DELETE FROM "+ Constants.TABLE_NAME + " WHERE "
                + Constants.TABLE_ID + " = ?", new String[]{id+""});
    }
}