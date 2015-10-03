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
                " ("+Constants.TABLE_ID+" integer primary key AUTOINCREMENT, image TEXT,title TEXT,content TEXT,time TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+Constants.TABLE_NAME);
        onCreate(db);
    }
}
