package xiaoguang.notepad;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import xiaoguang.tools.Constants;

/**
 * Created by xiaoguang on 2015/8/25.
 */
public class DbHelper extends SQLiteOpenHelper {


    public DbHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists "+ Constants.TABLE_NAME+
                " ("+Constants.TABLE_ID+" integer primary key AUTOINCREMENT, image BLOB,title TEXT,content TEXT,time TEXT)";
        String sql_pic = "create table if not exists "+Constants.PIC_TABLE+
                " ("+Constants.PIC_ID+" integer primary key AUTOINCREMENT, img BLOB)";
        db.execSQL(sql);
        db.execSQL(sql_pic);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+Constants.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+Constants.PIC_TABLE);
        onCreate(db);
    }

}
