package com.lopez.richard.nova_v6_en2.snowboy;

import android.os.Environment;

import java.io.File;

public class Constants {
    public static final String ASSETS_RES_DIR = "snowboy";
    public static final String DEFAULT_WORK_SPACE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/snowboy/";
    //public static final String ACTIVE_UMDL = "alexa.umdl";
    public static final String ACTIVE_UMDL = "Nova.pmdl";
    public static final String ACTIVE_RES = "common.res";
    public static final String SAVE_AUDIO = Constants.DEFAULT_WORK_SPACE + File.separatorChar + "recording.pcm";
    public static final int SAMPLE_RATE = 16000;
}
