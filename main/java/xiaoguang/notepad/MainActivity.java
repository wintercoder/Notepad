package xiaoguang.notepad;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.sax.RootElement;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import xiaoguang.tools.Constants;
import xiaoguang.tools.ToastUtils;


public class MainActivity extends Activity {
    private static final int ADD_NOTE = 1;
    private ListView listview;
    private List<ItemBean> dataList;
    public static MyListAdapter mAdapter;
    private DbManager dbManager;
    private ImageButton addBtn;
    private long oldTime = 0;
    private CheckBox cb;
    public boolean deleteMode;
    private boolean isALLselect;

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        dbManager = new DbManager(this);
        initView();
        initEvent();
        initDataFromDb();
        deleteMode = false;
        isALLselect = false;
    }
    public static void refreshUI(){
        mAdapter.notifyDataSetChanged();
    }

    private void initView(){
        listview = (ListView)findViewById(R.id.id_listView);
        addBtn = (ImageButton)findViewById(R.id.id_addNote);
        cb = (CheckBox)findViewById(R.id.id_lv_cb);
    }

    /**
     * 从数据库获取所有笔记，传给dataList
     */
    private void initDataFromDb(){
        dataList = new ArrayList<ItemBean>();
        dbManager.getDataList(dataList);
        mAdapter = new MyListAdapter(MainActivity.this,dataList);
        listview.setAdapter(mAdapter);
        refreshUI();
    }

    private void initEvent(){
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                startActivityForResult(intent, ADD_NOTE);
                MainActivity.this.finish();
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!deleteMode) {
                    MyListAdapter.ViewHolder viewHolder
                            = (MyListAdapter.ViewHolder) view.getTag();
                    String noteId = viewHolder.noteId.getText().toString().trim();
                    Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
                    intent.putExtra("noteId", Integer.parseInt(noteId));
                    startActivityForResult(intent, ADD_NOTE);
                    MainActivity.this.finish();
                    // 传递id
                } else {
                    MyListAdapter.ViewHolder holder = (MyListAdapter.ViewHolder) view.getTag();
                    holder.cb.toggle();
                }
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteMode = true;
                mAdapter.showCheckbox = true;
                beginDelMode();
                mAdapter.isSelected[position] = true;
                refreshUI();
                return true;
            }
        });
    }
    private void beginNormalMode(){
        RelativeLayout layoutDelTop = (RelativeLayout)findViewById(R.id.id_del_mode_top);
        layoutDelTop.setVisibility(View.GONE);
        RelativeLayout layoutNormalTop = (RelativeLayout)findViewById(R.id.id_normal_top);
        layoutNormalTop.setVisibility(View.VISIBLE);
        RelativeLayout layoutDelBottom = (RelativeLayout)findViewById(R.id.id_del_mode_bottom);
        layoutDelBottom.setVisibility(View.GONE);
        ImageButton imgBtnDel = (ImageButton)findViewById(R.id.id_btn_del_mode_bottom);
        imgBtnDel.setVisibility(View.GONE);
        selectAllToState(false);
        mAdapter.showCheckbox = false;
        deleteMode = false;
    }
    private void beginDelMode(){
        deleteMode = true;
        mAdapter.showCheckbox = true;
        RelativeLayout layoutDelTop = (RelativeLayout)findViewById(R.id.id_del_mode_top);
        layoutDelTop.setVisibility(View.VISIBLE);
        RelativeLayout layoutNormalTop = (RelativeLayout)findViewById(R.id.id_normal_top);
        layoutNormalTop.setVisibility(View.GONE);
        RelativeLayout layoutDelBottom = (RelativeLayout)findViewById(R.id.id_del_mode_bottom);
        layoutDelBottom.setVisibility(View.VISIBLE);
        TextView tvCancel = (TextView)findViewById(R.id.top_tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginNormalMode();
                mAdapter.showCheckbox = false;
                selectAllToState(false);
            }
        });
        TextView tvSelectAll = (TextView)findViewById(R.id.top_tv_selectAll);
        tvSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isALLselect){
                    TextView tv = (TextView)v;
                    tv.setText("全选");
                }else{
                    TextView tv = (TextView)v;
                    tv.setText("全不选");
                }
                selectAllToState(!isALLselect);
                isALLselect = !isALLselect;
                refreshUI();
            }
        });
        ImageButton imgBtnDel = (ImageButton)findViewById(R.id.id_btn_del_mode_bottom);
        imgBtnDel.setVisibility(View.VISIBLE);
        imgBtnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delSelectedItem();
                beginNormalMode();
                initDataFromDb();
            }
        });
    }
    public void selectAllToState(boolean state) {
        for (int i = 0; i < dataList.size(); i++) {
            mAdapter.isSelected[i] = state;
        }
    }

    public void delSelectedItem(){
        for(int i = 0; i < dataList.size(); i++){
            if(mAdapter.isSelected[i] == true){
                dbManager.delNoteById(dataList.get(i).getId());
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(deleteMode){
            beginNormalMode();
        }else if (keyCode == KeyEvent.KEYCODE_BACK) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - oldTime <= Constants.EXIT_TIME) {
                this.finish();
            } else {
                ToastUtils.showShort(this, "再按一次退出");
                oldTime = currentTime;
            }
        }
        return true;
    }
}