package com.jiewen.myvideoplayer;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jie.Wen on 2016/11/8.
 */
public class VideoItem {
    String path;
    String name;
    Bitmap thumb;
    String createdTime;

    VideoItem(String strPath, String strName,String strCreatedTime) {
        this.path = strPath;
        this.name = strName;

        SimpleDateFormat sdf  = new SimpleDateFormat("yy年MM月dd日HH时mm分");
        Date d = new Date(Long.valueOf(strCreatedTime)*1000);
        this.createdTime = sdf.format(d);

    }

    void createThumb () {
        if(this.thumb == null) {
            this.thumb = ThumbnailUtils.createVideoThumbnail(this.path,MediaStore.Images.Thumbnails.MINI_KIND);
        }
    }

    void releaseThumb () {
        if(this.thumb != null) {
            this.thumb.recycle();
            this.thumb = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        VideoItem item =  (VideoItem) o;

        return item.path.equals(this.path);
    }
}
