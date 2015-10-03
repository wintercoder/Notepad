package xiaoguang.tools;

/**
 * 常量表
 * Created by xiaoguang on 2015/9/26.
 */

public class Constants {
    public static final String TABLE_NAME = "notepadTable";

    public static final String TABLE_ID = "_id";
    public static final String DB_NAME = "notepad.db";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_TIME = "time";
    public static final String KEY_ID = "_id";
    public static final String IMG_DIR = "NotePad_IMG";

    /** 按两次才离开的最大允许间隔时间 **/
    public static final long EXIT_TIME = 3000;

    /** 图文混排里图片的大小 **/
    public static final int SPANTEXT_PIC_WIDTH = 600;
    public static final int SPANTEXT_PIC_HEIGHT = 600;

    /** 笔记的图标大小 **/
    public static final int ICO_PIC_WIDTH = 170;
    public static final int ICO_PIC_HEIGHT = 150;

    public static final String PicPatten = "/storage/.*?\\.\\.*(jpg|png|gif|jpeg|WebP)";
}
