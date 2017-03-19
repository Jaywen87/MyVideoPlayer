package com.jiewen.myvideoplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {

    AsyncTask  mVideoUpdateTask;
    List<VideoItem> mVideoList;
    ListView mVideoListView;
    public static final String TAG = "ListActivity";

    //jinzilong add
    private  String[] sRequiredPermissions = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    private  boolean hasRequiredPermissions() {
        return hasPermissions(sRequiredPermissions);
    }
    private boolean hasPermissions(final String[] permissions) {
        for (final String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    private  boolean hasPermission(final String permission) {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission)!= PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
       }
        return true;

    }
    //jinzilong end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasRequiredPermissions()) {
            finish();
            final Intent intent = new Intent(this, PermissionCheckActivity.class);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.activity_video_list);

        mVideoList = new ArrayList<VideoItem>();
        mVideoListView = (ListView) findViewById(R.id.video_list);
        VideoItemAdapter adapter = new VideoItemAdapter(this,R.layout.video_item,mVideoList);
        mVideoListView.setAdapter(adapter);
        mVideoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoItem item = mVideoList.get(position);
//                Intent intent = new Intent();
//                intent.setClass(VideoListActivity.this,VideoPlayer.class);
                Intent intent = new Intent (VideoListActivity.this,VideoPlayer.class);
                intent.setData(Uri.parse(item.path));
                Log.d(TAG, "click video of path is " + item.path);
                startActivity(intent);
            }
        });

        updateVideoList();
    }


    private  MenuItem mMenuRefreshItem;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        mMenuRefreshItem = menu.findItem(R.id.menu_refresh);

        if((mVideoUpdateTask != null) &&(mVideoUpdateTask.getStatus() ==AsyncTask.Status.RUNNING) ) {
            mMenuRefreshItem.setTitle(R.string.stop_refresh);
        } else {
            mMenuRefreshItem.setTitle(R.string.refresh);
        }
        return  true;
    }

    private void updateVideoList()
    {
        mVideoUpdateTask = new VideoUpdateTask();
        mVideoUpdateTask.execute();
        if(mMenuRefreshItem != null) {
            mMenuRefreshItem.setTitle(R.string.stop_refresh);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                if((mVideoUpdateTask != null) &&(mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING) ) {
                    mVideoUpdateTask.cancel(true);
                    mVideoUpdateTask = null;
                } else {
                    updateVideoList();
                }
                break;


            default :
                return super.onOptionsItemSelected(item);
        }
        return  true;
    }

    private void updateResult() {
        if(mMenuRefreshItem !=  null) {
            mMenuRefreshItem.setTitle(R.string.refresh);
        }

        for(int i = 0;i< mDataList.size();i++) {
            if(!mDataList.contains(mVideoList.get(i))) {
               // mVideoList.get(i).releaseThumb();
                mVideoList.remove(i);
                i--;
            }
        }
        mDataList.clear();

        VideoItemAdapter adapter = (VideoItemAdapter) mVideoListView.getAdapter();
        adapter.notifyDataSetChanged();
    }

    List<VideoItem> mDataList = new ArrayList<VideoItem>();

    class VideoUpdateTask extends AsyncTask<Object,VideoItem,Void> {

        @Override
        protected Void doInBackground(Object... params) {
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            String searchKey []  = new String[] {
                    MediaStore.Video.Media.TITLE,
//                    MediaStore.Images.Media.DATA,
                    MediaStore.Video.Media.DATA,
//                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Video.Media.DATE_ADDED,
            };


//            Log.d(TAG, "MediaStore.Video.Media.TITLE  = " + MediaStore.Video.Media.TITLE);
//            Log.d(TAG, "MediaStore.Images.Media.DATA  = " + MediaStore.Images.Media.DATA);
//            Log.d(TAG, "MediaStore.Images.Media.DATE_ADDED  = " + MediaStore.Images.Media.DATE_ADDED);
//            Log.d(TAG, "MediaStore.Video.Media.DATA  = " + MediaStore.Video.Media.DATA);
//            Log.d(TAG, "MediaStore.Video.Media.DATE_ADDED  = " + MediaStore.Video.Media.DATE_ADDED);


            //String where = MediaStore.Images.Media.DATA + "like \"%" + "/Video" + "%\"";
            String where = MediaStore.Video.Media.DATA + " like \"%"+"/Video"+"%\"";
            String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;

            ContentResolver  resolver = getContentResolver();
            Cursor cursor = resolver.query(
                    uri,
                    searchKey,
                    where,
                    null,
                    sortOrder
            );

            if(cursor != null) {
                while(cursor.moveToNext() && !isCancelled()) {
                    //MediaStore.Images.Media.DATA and MediaStore.Video.Media.DATA are same , they are string _data
//                    String  path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    String  path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                    String  name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
//                    String  createdTime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                    String  createdTime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));

                    VideoItem item  = new VideoItem(path,name,createdTime);

                    if(mVideoList.contains(item) == false) {
                        item.createThumb();
                        publishProgress(item);
                    }
                    mDataList.add(item);


                }
                cursor.close();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateResult();
        }

        @Override
        protected void onProgressUpdate(VideoItem... values) {
            VideoItem data  = values[0];
            mVideoList.add(data);
            VideoItemAdapter adapter = (VideoItemAdapter) mVideoListView.getAdapter();
            adapter.notifyDataSetChanged();

        }

        @Override
        protected void onCancelled() {
            updateResult();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if((mVideoUpdateTask != null) &&
                ( mVideoUpdateTask.getStatus() == AsyncTask.Status.RUNNING )) {
            mVideoUpdateTask.cancel(true);
        }
    }
}
