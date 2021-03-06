package demo.zkttestdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import demo.zkttestdemo.effect.alphatoolbar.AlphaToolbarActivity;
import demo.zkttestdemo.effect.loadingdialog.EatBaoZiActivity;
import demo.zkttestdemo.effect.pullAndload.PtrAndloadActivity;
import demo.zkttestdemo.effect.transition.TransitionActivity;
import demo.zkttestdemo.recyclerview.diffUtil.DiffUtilActivity;
import demo.zkttestdemo.recyclerview.multichoice.MultiChoiceActivity;
import demo.zkttestdemo.recyclerview.singlechoice.SingleChoiceActivity;
import demo.zkttestdemo.recyclerview.suspendmulti.SuspendMultiActivity;
import demo.zkttestdemo.recyclerview.suspendsingle.SuspendSingleActivity;
import demo.zkttestdemo.retrofit.RetrofitActivity;
import demo.zkttestdemo.utils.FileUtils;
import demo.zkttestdemo.utils.ImgUtil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView image_head;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**创建调用系统照相机待存储的临时文件*/
        createCameraTempFile(savedInstanceState);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        drawer.closeDrawer(GravityCompat.START);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerLayout = navigationView.getHeaderView(0);
        image_head = (ImageView) headerLayout.findViewById(R.id.image_head);

        image_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**弹出popwindow*/
                initPopWindow();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_Retrofit) {
            Intent intent = new Intent(this, RetrofitActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_Daggar2) {

        } else if (id == R.id.nav_RxJava) {

        } else if (id == R.id.nav_Volley) {

        } else if (id == R.id.nav_suspend_single) {
            Intent intent = new Intent(this, SuspendSingleActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_suspend_multi) {
            Intent intent = new Intent(this, SuspendMultiActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_recycler_singleChoice) {
            Intent intent = new Intent(this, SingleChoiceActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_recycler_multiChoice) {
            Intent intent = new Intent(this, MultiChoiceActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_eatBaoZi) {
            Intent intent = new Intent(this, EatBaoZiActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_pullAndLoad) {
            Intent intent = new Intent(this, PtrAndloadActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_alphaToolbar) {
            Intent intent = new Intent(this, AlphaToolbarActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_transition) {
            Intent intent = new Intent(this, TransitionActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_recycler_diff_util) {
            Intent intent = new Intent(this, DiffUtilActivity.class);
            startActivity(intent);
        }

        return true;
    }


    /***************************************************************************************
     * 以下都是相机拍照相关，注意在onCreate里调用 createCameraTempFile(savedInstanceState);
     ****************************************************************************************/
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE2 = 7;
    private static final int PHOTO_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PHOTO_CLIP = 3;
    //调用照相机返回图片临时文件
    private File tempFile;
    private String path;//图片的路径
    private String imgName;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String state = (String) msg.obj;
            if (state.equals("")) {
                // MyToast.show(PersonInfoActivity.this,"图片上传失败");
                imgName = state;
            } else {
                imgName = state;
            }
            /**个人信息更新*/
            //  updateData();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_REQUEST:
                switch (resultCode) {
                    case -1:// -1表示拍照成功
                        photoClip(Uri.fromFile(tempFile));
                        break;
                    default:
                        break;
                }
                break;
            case PHOTO_REQUEST:
                if (data != null) {
                    photoClip(data.getData());
                }
                break;
            case PHOTO_CLIP:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data");
                        image_head.setImageBitmap(photo);
                        //压缩头像大小
                        photo = FileUtils.decodeSampledBitmapFromBitmap(photo, 70, 70);
                        try {
                            path = saveFile(photo, "demo.jpg");
                            //  uploadImg(path);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }

    /**
     * 创建调用系统照相机待存储的临时文件
     *
     * @param savedInstanceState
     */
    private void createCameraTempFile(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("tempFile")) {
            tempFile = (File) savedInstanceState.getSerializable("tempFile");
        } else {
            tempFile = new File(checkDirPath(getExternalFilesDir(null) + "/image/"),
                    System.currentTimeMillis() + ".jpg");
        }
    }

    /**
     * 检查文件是否存在
     */
    private static String checkDirPath(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return "";
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath;
    }

    private void uploadImg(final String path) {
        final String imgUrl = "url";
        new Thread() {
            @Override
            public void run() {
                super.run();
                String string = ImgUtil.uploadFile(imgUrl, path);
                Message msg = Message.obtain();
                msg.obj = string;
                handler.sendMessage(msg);
            }
        }.start();
    }

    private void photoClip(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_CLIP);
    }

    /**
     * 上传头像
     */
    private void initPopWindow() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_popupwindow, null);
        TextView btnCarema = (TextView) view.findViewById(R.id.btn_camera);
        TextView btnPhoto = (TextView) view.findViewById(R.id.btn_photo);
        TextView btnCancel = (TextView) view.findViewById(R.id.btn_cancel);
        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
        popupWindow.setOutsideTouchable(true);
        View parent = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        //popupWindow在弹窗的时候背景半透明
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.5f;
        getWindow().setAttributes(params);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });

        btnCarema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    //跳转到调用系统相机
                    getPicFromCamera();
                    popupWindow.dismiss();
                }

            }
        });
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_CALL_PHONE2);
                } else {
                    //跳转到调用系统图库
                    getPicFromPhoto();
                    popupWindow.dismiss();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    public String saveFile(Bitmap bm, String fileName) throws IOException {
        String path = getFilesDir() + "/temp/";
        File dirFile = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(path + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
        return myCaptureFile.getAbsolutePath();
    }

    private void getPicFromPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    private void getPicFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 下面这句指定调用相机拍照后的照片存储的路径
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        startActivityForResult(intent, CAMERA_REQUEST);
    }
}
