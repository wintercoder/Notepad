package xiaoguang.notepad;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.text.format.Time;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xiaoguang.tools.BitMapUtil;
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
    /** 当前这次的图片路径 **/
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
                String icoPath = getOnePicPath(spanContentText.getText().toString());
                saveNote(icoPath);
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
     * 读取数据库的图片文本用，防止阻了UI，造成卡顿
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
            spanContentText.setPicText(note.getContent());
            timeView.setText(note.getTime());
        }
    }


    private void saveNote(String path){
        String text = spanContentText.getText().toString();
        if( editOrNewState == 0){
            dbManager.updateData(noteId,getOnePicPath(text),text,getCurTime());
        }else {
            if(path==null) {
                path = "/no_pic/test.jpg";
                //只是为了不挂掉随便给的，加载时为空
            }
            dbManager.dbWriting(path, text, getCurTime());
        }
//        ToastUtils.showShort(this, "save");
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
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //照相
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri); //指定图片输出地址
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, TAKE_PHOTO); //启动照相
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK != resultCode) {
//            ToastUtils.showShort(getApplicationContext(), "操作失败");
            return;
        }
        switch (requestCode) {
            case TAKE_PHOTO:
                spanContentText.addImgToText(currImgPath);
                break;
            case USING_GALLERY:     //图库
                if(data!=null) {
                    Uri imgUri = data.getData();
                    //获取图片路径
                    String[] ts1 = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(imgUri, ts1, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(column_index);
                    spanContentText.addImgToText(path);
                }break;
        }
        MainActivity.refreshUI(); //刷新UI
    }

    /**
     * 从文本中匹配出一张图片，直接选第一个匹配到的
     * @param content
     * @return 图片bitmap或者null
     */
    private String getOnePicPath(String content){
        String patternStr = Constants.PicPatten;
        Pattern pattern = Pattern.compile(patternStr);
        Matcher mc = pattern.matcher(content);
        if (mc.find()){
            return mc.group();
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
    private boolean dirExistOrMkDir(String dirPath) {
        String sdCard = Environment.getExternalStorageState();
        if (!sdCard.equals(Environment.MEDIA_MOUNTED)) {
            ToastUtils.showShort(this, "未检测到SD卡!");
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

    private void getCurImgPathToVar() {
        if(!dirExistOrMkDir(Constants.IMG_DIR)){
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
     * 重写按返回键，实现按两次退出效果 或者 新笔记直接返回|有内容的话询问是否放弃
     * @Override
     */
    public void onBackPressed() {
        if(noteId == -1){
            if( spanContentText.getText().length() == 0){   //getText后toString得到的不是空
                backToMainActivity();
            }else{
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
            }
            return ;
        }
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
