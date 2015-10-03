package xiaoguang.notepad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.Time;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xiaoguang.tools.BitmapTools;
import xiaoguang.tools.Constants;
import xiaoguang.tools.SpanText;
import xiaoguang.tools.ToastUtils;

/**
 * Created by xiaoguang on 2015/8/25.
 */
public class AddNoteActivity extends Activity implements View.OnClickListener{

    private TextView timeView;
    private ImageButton btn_back;
    private ImageButton btn_save,btn_usingPic,btn_camera;
    private static final int TAKE_PHOTO = 1;
    private static final int USING_GALLERY = 2;
    public SpanText spanContentText = null;
    /** 重载TextView后的Text框，与记事本的编辑框绑定 **/
    private String currImgPath = "";
    /** 当前这次的图片路径，在该APP文件夹里的MainActivity的IMG_DIR目录下 **/
    public static AddNoteActivity addNoteActivity;
    private DbManager dbManager;
    private int noteId = -1;
    private int editOrNewState = 0;
    /**  editOrNewState: 0表示编辑 1表示新建 **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_add);
        addNoteActivity = this;
        dbManager = new DbManager(this);

        initView();
        initEvent();

        Intent intent = getIntent();
        noteId = intent.getIntExtra("noteId",-1);
        if(noteId != -1){       //读取已有
            editOrNewState = 0;
            getNoteFromDb(noteId);
            spanContentText.setSelection(spanContentText.getText().toString().length());//光标位置
        }else{
            editOrNewState = 1;
            timeView.setText(getCurTime());
            spanContentText.setText("");
        }
    }

    private void initView(){
        btn_back = (ImageButton)findViewById(R.id.id_btn_add_back);
        btn_save = (ImageButton)findViewById(R.id.id_btn_save);
        btn_usingPic = (ImageButton)findViewById(R.id.id_btn_usingpic);
        btn_camera = (ImageButton)findViewById(R.id.id_btn_start_camera);
        spanContentText = (SpanText)findViewById(R.id.id_editContent);
        timeView = (TextView)findViewById(R.id.id_viewTime);
    }

    private void initEvent(){
        btn_back.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        btn_usingPic.setOnClickListener(this);
        btn_camera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_btn_add_back:  //go back
                backToMainActivity();
                break;
            case R.id.id_btn_save:
                Bitmap ico = getOnePic(spanContentText.getText().toString());
                if( null != ico ){
                    saveNote(ico);
                }else{
                    saveNote();
                }
                backToMainActivity();
                break;
            case R.id.id_btn_usingpic:
                useGallery();
                break;
            case R.id.id_btn_start_camera:
                takePhoto();
                break;
        }
    }

    private void backToMainActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    private void getNoteFromDb(int id) {
        new getDataAsyncTask().execute(id);
    }

    /**
     * 读取数据库数据用，防止阻了UI，造成卡顿
     */
    class getDataAsyncTask extends AsyncTask<Integer,Void,ItemBean>{
        @Override
        protected ItemBean doInBackground(Integer... params) {
            ItemBean note = dbManager.getData(params[0]);
            return note;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ItemBean note) {
            super.onPostExecute(note);
            spanContentText.setSpanContentPic_Cam(note.getContent());
            timeView.setText(note.getTime());
        }
    }

    /**
     * 纯文本情况下保存，默认用壁纸当图标
     */
    private void saveNote(){
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        Bitmap bitmap = ((BitmapDrawable)wallpaperManager.getDrawable()).getBitmap();
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        if( editOrNewState == 0) {
            dbManager.updateData(noteId, spanContentText.getText().toString(), getCurTime());
        }else{
            dbManager.dbWriting(bitmap, spanContentText.getText().toString(), getCurTime());
        }
        ToastUtils.showShort(this, "save");
    }
    private void saveNote(Bitmap bitmap){
        if( editOrNewState == 0){
            dbManager.updateData(noteId, spanContentText.getText().toString(), getCurTime());
        }else {
            dbManager.dbWriting(bitmap, spanContentText.getText().toString(), getCurTime());
        }
        ToastUtils.showShort(this, "save");
    }


    /**
     * 调用相册
     */
    private void useGallery(){
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, USING_GALLERY);
    }

    /**
     * 拍照
     */
    private void takePhoto(){
        getCurImgPathToVar();
        //存储至DCIM文件夹
        File file = new File(currImgPath);
        //将File对象转换为Uri并启动照相程序
        Uri uri = Uri.fromFile(file);
        Toast.makeText(getApplicationContext(), currImgPath.toString(), Toast.LENGTH_SHORT)
                .show();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //照相
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri); //指定图片输出地址
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, TAKE_PHOTO); //启动照相
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) {
            ToastUtils.showShort(getApplicationContext(), "操作失败");
            return;
        }
        switch (requestCode) {
            case TAKE_PHOTO:
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 3;
                Bitmap bitmap = BitmapFactory.decodeFile(currImgPath,opts);
                //避免内存溢出
                Bitmap resizedBitmap = BitmapTools.getScaleBitmap(bitmap, 0.3f, 0.3f);
                // ↑获取缩略图   ↓如果bitmap非空，回收以减少内存消耗
                spanContentText.addImgToText(resizedBitmap, currImgPath);
                if(bitmap != null) {
                    bitmap.recycle();
                }
                break;
            case USING_GALLERY:     //保存选中的图片
                Uri imgUri =  data.getData();
                Bitmap image = null;
                ContentResolver cr = this.getContentResolver();
                if(imgUri != null){
                    try {
                        image = BitmapFactory.decodeStream(cr.openInputStream(imgUri));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                }else{
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        // 这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
                        image = extras.getParcelable("data");
                    }else{
                        ToastUtils.showShort(getApplicationContext(), "图片空");
                    }
                }
                Bitmap resizeBitmap = BitmapTools.getScaleBitmap(image,0.7f,0.7f);
                int lastId = dbManager.dbInsertPic(resizeBitmap);
                spanContentText.addImgToText(resizeBitmap, lastId);
                break;
        }
        MainActivity.refreshUI(); //刷新UI
    }

    /**
     * 从文本中匹配出一张图片，直接选第一个匹配到的，且拍照的图片优先
     * @param content
     * @return 图片bitmap或者null
     */
    private Bitmap getOnePic(String content){
        String patternCamStr = Environment.getExternalStorageDirectory()
                + "/" + Constants.IMG_DIR + "/.+?\\.\\w{3}";
        Pattern patternCam = Pattern.compile(patternCamStr);
        Matcher mCam = patternCam.matcher(content);
        if(mCam.find()){
            Bitmap bmpCam = BitmapFactory.decodeFile(mCam.group());
            Bitmap bitmapCam = BitmapTools.getScaleBitmap(bmpCam, 0.5f, 0.5f);
            if(bmpCam != null){
                bmpCam.recycle();
            }
            return bitmapCam;
        }

        String patternPicStr = "<pic_" + "\\d?"+">";
        Pattern patternPic = Pattern.compile(patternPicStr);
        Matcher mPic = patternPic.matcher(content);
        if(mPic.find()){
            String patStr = "[\\d]+";
            Matcher mc = Pattern.compile(patStr).matcher(mPic.group());
            if(mc.find()){
                DbManager dbManager = new DbManager(AddNoteActivity.addNoteActivity);
                Bitmap map = dbManager.getPicById(Integer.parseInt(mc.group()));
                Bitmap bitmap = BitmapTools.getScaleBitmap(map, 0.5f, 0.5f);
                if(map != null){
                    map.recycle();
                }
                return bitmap;
            }
        }
        return null;
    }

    private String getCurTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm");
        return formatter.format(new Date());
    }

    /**
     * 判断放图片的文件夹是否存在，不存在就创建
     * @param dirPath
     * @return true?false
     */
    private boolean dirIsExistAndMkdir(String dirPath) {
        String sdCard = Environment.getExternalStorageState();
        if (!sdCard.equals(Environment.MEDIA_MOUNTED)) {
            ToastUtils.showShort(this, "未检测到存储卡!");
            return false;
        }
        File f = new File(Environment.getExternalStorageDirectory() + "/"
                + dirPath);
        if (f.exists()) {
            return true;
        }
        boolean isSuccess = f.mkdir();
        if (isSuccess) {
            return true;
        }
        return false;
    }

    private void getCurImgPathToVar(){
        if(!dirIsExistAndMkdir(Constants.IMG_DIR)){
            ToastUtils.showShort(this, "创建文件失败");
            return ;
        }
        Time time = new Time();
        Random r = new Random();
        String imgName = time.year + "" + (time.month + 1) + ""
                + time.monthDay + "" + time.minute + "" + time.second
                + "" + r.nextInt(1000) + ".jpg";
        currImgPath = Environment.getExternalStorageDirectory() + "/"
                + Constants.IMG_DIR + "/" + imgName;
    }

    /**
     * 重写按返回键，实现按两次退出效果
     * @Override
     */
    public void onBackPressed() {
        String old = dbManager.getData(noteId).getContent();
        String newer = spanContentText.getText().toString();

        if( !old.equals(newer) ){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("放弃编辑？");
            builder.setPositiveButton("是",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    backToMainActivity();
                }
            });
            builder.setNegativeButton("否", null);
            builder.show();
        }else{
            backToMainActivity();
        }
    }
}
