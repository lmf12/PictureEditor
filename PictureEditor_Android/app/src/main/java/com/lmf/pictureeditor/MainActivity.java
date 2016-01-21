package com.lmf.pictureeditor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int OPEN_CAMERA_CODE = 10;  //打开相机代码
    private static final int OPEN_GALLERY_CODE = 11;  //打开相册代码

    private static final String BASE_URL = "http://115.29.144.170/pictureEditor";

    private int TAG_RECEIVE_URL = 1;
    private int TAG_RECEIVE_PICTURE = 2;

    private LinearLayout panel, darkLayer, contentLayer, openCameraMenu, openGalleryMenu, toolLayer;
    private Button openPictureButton;
    private RelativeLayout grayscaleEffect, blurEffect, gammaCorrectionEffect, colorizeEffect, imageWatermarkingEffect;
    private TextView openPanelMenu, uploadPictureMenu, savePictureMenu;
    private ImageView openToolMenu, contentPicture;

    private ProgressDialog loading;
    private Toast toast;

    private String filePath;  //照片路径
    private int currentSelectedId = -1;  //当前选中的效果的id
    private int effectGroupCode = -1;  //效果组的编码
    private int effectCode = -1;  //某个效果组下的某个效果的编码

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {

            if (msg.what == TAG_RECEIVE_URL) {
                receivePicture((String) msg.obj);
            }
            else if (msg.what == TAG_RECEIVE_PICTURE) {
                Bitmap resultBitmap = (Bitmap)msg.obj;

                if (resultBitmap != null) {
                    resultBitmap = getUsableBitmap(resultBitmap);
                    contentPicture.setImageBitmap(resultBitmap);
                }
                else {
                    showToast("网络较差,请稍后尝试");
                }
                loading.dismiss();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initComponent();
        setComponentListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (toast != null) {
            toast.cancel();
        }
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
                hideEditMenu();
                break;
            case R.id.tool_layer:
                hideTool();
                showEditMenu();
                break;
            case R.id.upload_picture_menu:
                if (effectGroupCode == -1 || effectCode == -1) {
                    showToast("请先选择一种效果");
                }
                else {
                    loading = ProgressDialog.show(MainActivity.this, null,
                            "正在上传...");
                    uploadPicture(filePath);
                }
                break;
            case R.id.grayscale_effect:
            case R.id.blur_effect:
            case R.id.gammaCorrection_effect:
            case R.id.colorize_effect:
            case R.id.imageWatermarking_effect:
                cancelSelectedEffect();
                selectEffect(v.getId());
                hideTool();
                showEditMenu();
                break;
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
        grayscaleEffect = (RelativeLayout)findViewById(R.id.grayscale_effect);
        blurEffect = (RelativeLayout)findViewById(R.id.blur_effect);
        gammaCorrectionEffect = (RelativeLayout)findViewById(R.id.gammaCorrection_effect);
        colorizeEffect = (RelativeLayout)findViewById(R.id.colorize_effect);
        imageWatermarkingEffect = (RelativeLayout)findViewById(R.id.imageWatermarking_effect);
    }

    /**
     * 设置组件监听器
     * */
    private void setComponentListener() {

        openPictureButton.setOnClickListener(this);
        darkLayer.setOnClickListener(this);
        openCameraMenu.setOnClickListener(this);
        openGalleryMenu.setOnClickListener(this);
        toolLayer.setOnClickListener(this);
        grayscaleEffect.setOnClickListener(this);
        blurEffect.setOnClickListener(this);
        gammaCorrectionEffect.setOnClickListener(this);
        colorizeEffect.setOnClickListener(this);
        imageWatermarkingEffect.setOnClickListener(this);
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

        toolLayer.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏选择图片面板
     * */
    private void hideTool() {

        toolLayer.setVisibility(View.GONE);
    }

    /**
     * 显示编辑菜单
     * */
    private void showEditMenu() {

        openPanelMenu.setVisibility(View.VISIBLE);
        openToolMenu.setVisibility(View.VISIBLE);
        uploadPictureMenu.setVisibility(View.VISIBLE);
        savePictureMenu.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏编辑菜单
     * */
    private void hideEditMenu() {

        openPanelMenu.setVisibility(View.INVISIBLE);
        openToolMenu.setVisibility(View.INVISIBLE);
        uploadPictureMenu.setVisibility(View.INVISIBLE);
        savePictureMenu.setVisibility(View.INVISIBLE);
    }

    /**
     * 选择中一种效果,高亮
     * */
    private void selectEffect(int viewId) {

        RelativeLayout effectMenu = (RelativeLayout)findViewById(viewId);
        effectMenu.setBackgroundResource(R.drawable.pic_green);
        ((TextView)effectMenu.getChildAt(0)).setTextColor(getResources().getColor(android.R.color.white));
        currentSelectedId = viewId;

        switch (viewId) {
            case R.id.grayscale_effect:
                effectGroupCode = 1;
                effectCode = 1;
                break;
            case R.id.blur_effect:
                effectGroupCode = 1;
                effectCode = 2;
                break;
            case R.id.gammaCorrection_effect:
                effectGroupCode = 1;
                effectCode = 3;
                break;
            case R.id.colorize_effect:
                effectGroupCode = 1;
                effectCode = 4;
                break;
            case R.id.imageWatermarking_effect:
                effectGroupCode = 1;
                effectCode = 5;
                break;
            default:
                break;
        }
    }

    /**
     * 取消选中的效果
     * */
    private void cancelSelectedEffect() {

        if (currentSelectedId == -1) {
            return;
        }
        else {
            RelativeLayout effectMenu = (RelativeLayout) findViewById(currentSelectedId);
            effectMenu.setBackgroundResource(R.drawable.selector_tool_picture);
            ((TextView)effectMenu.getChildAt(0)).setTextColor(new TextView(this).getCurrentTextColor());
        }
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
                Calendar.getInstance(Locale.CHINA)) + ".jpg";
        filePath = path + "/" + name;
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

        openPanelMenu = (TextView)content.findViewById(R.id.open_panel_menu);
        openPanelMenu.setOnClickListener(this);

        uploadPictureMenu = (TextView)content.findViewById(R.id.upload_picture_menu);
        uploadPictureMenu.setOnClickListener(this);

        savePictureMenu = (TextView)content.findViewById(R.id.save_picture_menu);
        savePictureMenu.setOnClickListener(this);

        openToolMenu = (ImageView)content.findViewById(R.id.open_tool_menu);
        openToolMenu.setOnClickListener(this);

        contentPicture = (ImageView)content.findViewById(R.id.content_picture);
        if (imgPath != null) {
            File file = new File(imgPath);
            if(file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                bitmap = getUsableBitmap(bitmap);
                contentPicture.setImageBitmap(bitmap);
            }
        }
        filePath = imgPath;  //将路径赋值回来,用于上传图片用

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

    /**
     * 上传图片函数
     * */
    private void uploadPicture(final String file) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                uploadFile(BASE_URL+"/pictureUpload.php", file);
            }
        }).start();
    }

    /**
     *上传文件至Server，uploadUrl：接收文件的处理页面
     * */
    private void uploadFile(String uploadUrl, String srcPath) {

        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url
                    .openConnection();
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(
                    httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
                    + effectCode+"@"+ srcPath.substring(srcPath.lastIndexOf("/") + 1)
                    + "\""
                    + end);
            dos.writeBytes(end);

            FileInputStream fis = new FileInputStream(srcPath);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            // 读取文件
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            fis.close();

            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();

            Message message = new Message();
            message.what = TAG_RECEIVE_URL;
            message.obj = result;

            mHandler.sendMessage(message);

            dos.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据图片的url路径获得Bitmap对象
     * @param url
     * @return
     */
    private Bitmap returnBitmap(String url) {
        URL fileUrl = null;
        Bitmap bitmap = null;

        try {
            fileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) fileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 接收图片
     * */
    private void receivePicture(final String url) {

        loading.dismiss();
        loading = ProgressDialog.show(MainActivity.this, null,
                "正在获取...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = returnBitmap(BASE_URL+url);

                Message message = new Message();
                message.what = TAG_RECEIVE_PICTURE;
                message.obj = bitmap;

                mHandler.sendMessage(message);
            }
        }).start();

    }

    /**
     * 弹出Toast
     * */
    private void showToast(String text) {

        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.show();
    }

}
