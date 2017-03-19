package com.jiewen.myvideoplayer;

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class VideoPlayer extends AppCompatActivity {
    public static final String TAG = "VideoPlayer";
    VideoView mVideoView;
    int mLastPlayedTime;
    public static final String  LAST_PLAYED_TIME = "last_played_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        Uri uri  = getIntent().getData();
        String path = uri.getPath();

        mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setVideoPath(path);

        Log.d(TAG, "Video path is " + path);
        MediaController controller = new MediaController(this);
        mVideoView.setMediaController(controller);

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();
            return;
        }

        String [] serchkey = new String[] {
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.WIDTH,
//                MediaStore.Images.Media.SIZE,
//                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED
        };

        String where  = MediaStore.Video.Media.DATA + "= '" + path + "'";
        String [] keywords = null;
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,serchkey,where,keywords,MediaStore.Video.DEFAULT_SORT_ORDER);

        if(cursor !=  null) {
            if(cursor.getCount()>0) {
                cursor.moveToNext();
                String createdTime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
                int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
                VideoItem videoItem = new VideoItem(path,name,createdTime);

                TextView title = (TextView)findViewById(R.id.video_title);
                title.setText(videoItem.name);

                TextView  created  = (TextView)findViewById(R.id.created_time);
                created.setText(videoItem.createdTime);

                TextView screen = (TextView)findViewById(R.id.video_width_height);
                screen.setText(width + "*" + height);

                TextView fileSize = (TextView)findViewById(R.id.video_size);
                fileSize.setText(String.valueOf(size/1000) + "KB");

            } else {

                TextView title = (TextView) findViewById(R.id.video_title);
                title.setText(R.string.unknown);

                TextView created = (TextView) findViewById(R.id.created_time);
                created.setText(R.string.unknown);

                TextView screen = (TextView) findViewById(R.id.video_width_height);
                screen.setText(R.string.unknown);

                TextView fileSize = (TextView) findViewById(R.id.video_size);
                fileSize.setText(R.string.unknown);
            }
            cursor.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLastPlayedTime = mVideoView.getCurrentPosition();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.start();
        mVideoView.seekTo(mLastPlayedTime);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LAST_PLAYED_TIME, mVideoView.getCurrentPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mLastPlayedTime = savedInstanceState.getInt(LAST_PLAYED_TIME);
    }
}
