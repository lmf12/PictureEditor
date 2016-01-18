package com.lmf.pictureeditor;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int OPEN_CAMERA_CODE = 10;  //打开相机代码
    private static final int OPEN_GALLERY_CODE = 11;  //打开相册代码

    private LinearLayout panel, darkLayer, contentLayer, openCameraMenu, openGalleryMenu, toolLayer;
    private Button openPictureButton;

    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initComponent();
        setComponentListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == OPEN_GALLERY_CODE) {
                Uri uri = data.getData();

                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);

//                Intent intent = new Intent(this, PictureActivity.class);
//                intent.putExtra("filePath", path);
//                intent.putExtra("selectNum", selectNumber);
//
//                startActivity(intent);
                displayPicture(path);
            }
            else if (requestCode == OPEN_CAMERA_CODE) {
//                Intent intent = new Intent(this, PictureActivity.class);
//                intent.putExtra("filePath", filePath);
//                intent.putExtra("selectNum", selectNumber);
//
//                startActivity(intent);
                displayPicture(filePath);
            }

            hidePanel();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.open_picture_button:
                showPanel();
                break;
            case R.id.dark_layer:
                hidePanel();
                hideTool();
                break;
            case R.id.open_camera_menu:
                openCamera();
                break;
            case R.id.open_gallery_menu:
                openGallery();
                break;
            case R.id.open_panel_menu:
                showPanel();
                break;
            case R.id.open_tool_menu:
                showTool();
            default:
                break;
        }
    }

    /**
     * 获取组件
     * */
    private void initComponent() {

        panel = (LinearLayout)findViewById(R.id.panel);
        darkLayer = (LinearLayout)findViewById(R.id.dark_layer);
        openPictureButton = (Button)findViewById(R.id.open_picture_button);
        openCameraMenu = (LinearLayout)findViewById(R.id.open_camera_menu);
        openGalleryMenu = (LinearLayout)findViewById(R.id.open_gallery_menu);
        contentLayer = (LinearLayout)findViewById(R.id.content_layer);
        toolLayer = (LinearLayout)findViewById(R.id.tool_layer);
    }

    /**
     * 设置组件监听器
     * */
    private void setComponentListener() {

        openPictureButton.setOnClickListener(this);
        darkLayer.setOnClickListener(this);
        openCameraMenu.setOnClickListener(this);
        openGalleryMenu.setOnClickListener(this);
    }

    /**
     * 显示选择图片面板
     * */
    private void showPanel() {

        darkLayer.setVisibility(View.VISIBLE);
        panel.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏选择图片面板
     * */
    private void hidePanel() {

        darkLayer.setVisibility(View.GONE);
        panel.setVisibility(View.GONE);
    }

    /**
     * 显示工具选择面板
     * */
    private void showTool() {

        darkLayer.setVisibility(View.VISIBLE);
        toolLayer.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏选择图片面板
     * */
    private void hideTool() {

        darkLayer.setVisibility(View.GONE);
        toolLayer.setVisibility(View.GONE);
    }

    /**
    * 打开相册
    * */
    private void openGallery() {

        Intent intent = new Intent(Intent.ACTION_PICK);// 打开相册
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, OPEN_GALLERY_CODE);
    }

    /**
    * 打开相机
    * */
    private void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 打开相机
        String path = createSDCardDir();  //创建路径
        String name = DateFormat.format("yyyyMMdd_hhmmss",
                Calendar.getInstance(Locale.CHINA))+ ".jpg";
        filePath = path+"/"+name;
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath)));
        startActivityForResult(intent, OPEN_CAMERA_CODE);
    }

    /**
    * 创建文件夹
    * */
    private String createSDCardDir() {

        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File sdcardDir = Environment.getExternalStorageDirectory();  // 创建一个文件夹对象，赋值为外部存储器的目录
            String path = sdcardDir.getPath()+"/meitu/temp";  //得到一个路径，内容是sdcard的文件夹路径和名字
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();  //若不存在，创建目录，可以在应用启动的时候创建
            }
            return path;
        }
        else {
            return null;
        }
    }

    /**
     * 展示图片
     * */
    private void displayPicture(String imgPath) {

        RelativeLayout content = (RelativeLayout)View.inflate(this, R.layout.display_picture, null);
        content.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        TextView openPanelMenu = (TextView)content.findViewById(R.id.open_panel_menu);
        openPanelMenu.setOnClickListener(this);

        ImageView openToolMenu = (ImageView)content.findViewById(R.id.open_tool_menu);
        openToolMenu.setOnClickListener(this);

        ImageView contentPicture = (ImageView)content.findViewById(R.id.content_picture);
        if (imgPath != null) {
            File file = new File(imgPath);
            if(file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                bitmap = getUsableBitmap(bitmap);
                contentPicture.setImageBitmap(bitmap);
            }
        }

        contentLayer.removeAllViews();
        contentLayer.addView(content);
    }

    /**
     * 获取可用图片
     * */
    private Bitmap getUsableBitmap(Bitmap bitmap) {

        if (bitmap.getHeight() > 2000 || bitmap.getWidth() > 2000) {
            // 取得想要缩放的matrix参数
            Matrix matrix = new Matrix();
            if (bitmap.getHeight() > bitmap.getWidth()) {
                matrix.postScale((float)(2000.0  / bitmap.getHeight()),(float)(2000.0  / bitmap.getHeight()));
            }
            else {
                matrix.postScale((float)(2000.0  / bitmap.getWidth()),(float)(2000.0  / bitmap.getWidth()));
            }
            // 得到新的图片
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                    true);
        }
        return bitmap;
    }

}
