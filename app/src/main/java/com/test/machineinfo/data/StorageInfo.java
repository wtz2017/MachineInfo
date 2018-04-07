package com.test.machineinfo.data;

import android.os.StatFs;

import java.io.File;

/**
 * Created by WTZ on 2018/4/4.
 */

public class StorageInfo {
    public String path;
    public long blockSize;
    public long blockCount;
    public long availCount;

    public StorageInfo(File dir) {
        StatFs sf = new StatFs(dir.getPath());
        this.path = dir.getPath();
        this.blockSize = sf.getBlockSize();
        this.blockCount = sf.getBlockCount();
        this.availCount = sf.getAvailableBlocks();
    }
}
