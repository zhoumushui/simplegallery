package com.zms.simplegallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

public class Main extends ActionBarActivity {
    /**
     * 常规Gallery
     */
    private Gallery galleryNormal;

    /**
     * 循环Gallery
     */
    private Gallery galleryLoop;

    /**
     * 3D Gallery
     */
    private Gallery3D gallery3D;

    /**
     * HorizontalScrollView Gallery
     */
    private static final String PATH_SDCARD = "/mnt/sdcard";
    private LinearLayout hsvGallery;
    private LinearLayout hsvGalleryFromSD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Integer[] images = {
                R.drawable.pic_1,
                R.drawable.pic_2,
                R.drawable.pic_3,
                R.drawable.pic_4,
                R.drawable.pic_5,
                R.drawable.pic_6,
                R.drawable.pic_7,
                R.drawable.pic_8,
                R.drawable.pic_9
        };

        galleryNormal = (Gallery) findViewById(R.id.galleryNormal);
        galleryNormal.setAdapter(new ImageAdapter(this, images, false));
        galleryNormal.setSpacing(5); // 条目间距
        galleryNormal.setOnItemClickListener(new MyItemOnClickListener());

        galleryLoop = (Gallery) findViewById(R.id.galleryLoop);
        galleryLoop.setAdapter(new ImageAdapter(this, images, true));
        galleryLoop.setSpacing(5);
        galleryLoop.setOnItemClickListener(new MyItemOnClickListener());


        Gallery3DAdapter gallery3DAdapter = new Gallery3DAdapter(this, images);
        gallery3DAdapter.createReflectedImages(); // 创建倒影效果
        gallery3D = (Gallery3D) this.findViewById(R.id.gallery3d);
        gallery3D.setFadingEdgeLength(0);
        gallery3D.setSpacing(-100);
        gallery3D.setAdapter(gallery3DAdapter);
        gallery3D.setOnItemClickListener(new MyItemOnClickListener());
        gallery3D.setSelection(5 - 1);

        hsvGallery = (LinearLayout) findViewById(R.id.hsvGallery);
        hsvGalleryFromSD = (LinearLayout) findViewById(R.id.hsvGalleryFromSD);

        for (Integer id : images) {
            hsvGallery.addView(insertImage(id));
        }

        // pic in camera
        String targetPath = PATH_SDCARD + "/DCIM/Camera/";

        File targetDirector = new File(targetPath);
        File[] imageFiles = targetDirector.listFiles();
        if (imageFiles != null) {
            for (File file : imageFiles) {
                hsvGalleryFromSD.addView(insertPhoto(file.getAbsolutePath()));
            }
        }
    }

    private class MyItemOnClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TextView tvHint = (TextView) findViewById(R.id.tvHint);
            if (view == galleryNormal) {
                int num = position + 1;
                tvHint.setText("常规Gallery：点击了" + num);
            } else if (view == galleryLoop) {
                int num = (position + 1) % 9;
                if (num == 0) {
                    num = 9;
                }
                tvHint.setText("循环Gallery：点击了" + num);
            } else if (view == gallery3D) {
                int num = position + 1;
                tvHint.setText("3D Gallery：点击了" + num);
            }
        }
    }

    // Gallery的Adapter
    class ImageAdapter extends BaseAdapter {
        private Context context;
        private Integer[] imageInteger;
        private boolean isLoop = false;

        /**
         * @param context Context
         * @param images  图像资源整形数组
         * @param isLoop  是否是循环Gallery
         */
        public ImageAdapter(Context context, Integer[] images, boolean isLoop) {
            this.context = context;
            this.imageInteger = images;
            this.isLoop = isLoop;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        // 常规Gallery和循环Gallery，此函数返回值不同
        public int getCount() {
            if (isLoop) {
                return Integer.MAX_VALUE;
            } else {
                return imageInteger.length;
            }
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);
            if (isLoop) {
                imageView.setImageResource(imageInteger[position % imageInteger.length]);
            } else {
                imageView.setImageResource(imageInteger[position]);
            }
            imageView.setScaleType(ImageView.ScaleType.FIT_XY); // 显示比例类型
            imageView.setLayoutParams(new Gallery.LayoutParams(100, 100)); //图片大小
            return imageView;
        }
    }

    private View insertImage(Integer id) {
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(new LayoutParams(320, 320));
        layout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new LayoutParams(300, 300));
        imageView.setBackgroundResource(id);

        layout.addView(imageView);
        return layout;
    }

    private View insertPhoto(String absolutePath) {
        Bitmap bm = decodeSampleBitmapFromUri(absolutePath, 200, 200);
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(new LayoutParams(250, 250));
        layout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new LayoutParams(220, 220));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bm);

        layout.addView(imageView);
        return layout;
    }

    private Bitmap decodeSampleBitmapFromUri(String absolutePath, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absolutePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(absolutePath, options);
        return bitmap;
    }


    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                      int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
