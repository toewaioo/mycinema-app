package com.two.channelmyanmar.util;


import static android.os.Build.ID;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;

import java.nio.charset.Charset;
import java.security.AllPermission;
import java.security.MessageDigest;

public class BlurTransform extends BitmapTransformation
{
    final byte[] ID_BYTES=ID.getBytes(Charset.forName("UTF-8"));

    private Context context;
    int radius;
    public BlurTransform(Context context,int radius){
        this.context=context;
        this.radius=radius;
    }
    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap bluredImgg= TransformationUtils.centerCrop(pool,toTransform,outWidth,outHeight);
        bluredImgg=Bitmap.createBitmap(bluredImgg);
        RenderScript rs=RenderScript.create(context);
        Allocation input= Allocation.createFromBitmap(rs,bluredImgg,Allocation.MipmapControl.MIPMAP_NONE,Allocation.USAGE_SCRIPT);
        Allocation outpit=Allocation.createTyped(rs,input.getType());
        ScriptIntrinsicBlur scrript=ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        scrript.setInput(input);
        scrript.setRadius(radius);
        scrript.forEach(outpit);
        outpit.copyTo(bluredImgg);
        return bluredImgg;
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

        messageDigest.update(ID_BYTES);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof BlurTransform;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }
}

