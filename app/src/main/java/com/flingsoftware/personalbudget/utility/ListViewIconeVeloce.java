/*
 * Copyright (c) This code was written by iClaude. All rights reserved.
 */

package com.flingsoftware.personalbudget.utility;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.flingsoftware.personalbudget.R;

import java.lang.ref.WeakReference;


public class ListViewIconeVeloce {
	
	public ListViewIconeVeloce(Context context) {
		this.mioContext = context;
	}
	
	
	public void loadBitmap(int icona, ImageView imageView, Bitmap mPlaceHolderBitmap, int larg, int alt) {
		this.larg = larg;
		this.alt = alt;
		
		int resId = arrIconeId[icona];
	    if (cancelPotentialWork(resId, imageView)) {
	        final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
	        final AsyncDrawable asyncDrawable = new AsyncDrawable(mioContext.getResources(), mPlaceHolderBitmap, task);
	        imageView.setImageDrawable(asyncDrawable);
	        task.execute(resId);
	    }
	}
	
	
	public void loadBitmapStandard(int resId, ImageView imageView, Bitmap mPlaceHolderBitmap, int larg, int alt) {
		this.larg = larg;
		this.alt = alt;
		
		if (cancelPotentialWork(resId, imageView)) {
	        final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
	        final AsyncDrawable asyncDrawable = new AsyncDrawable(mioContext.getResources(), mPlaceHolderBitmap, task);
	        imageView.setImageDrawable(asyncDrawable);
	        task.execute(resId);
	    }
	}
	

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
	
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	    
	static class AsyncDrawable extends BitmapDrawable {
	    private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	    public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
	        super(res, bitmap);
	        bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	    }

	    public BitmapWorkerTask getBitmapWorkerTask() {
	        return bitmapWorkerTaskReference.get();
	    }
	}
	
	
	private static boolean cancelPotentialWork(int data, ImageView imageView) {
	    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

	    if (bitmapWorkerTask != null) {
	        final int bitmapData = bitmapWorkerTask.data;
	        // If bitmapData is not yet set or it differs from the new data
	        if (bitmapData == 0 || bitmapData != data) {
	            // Cancel previous task
	            bitmapWorkerTask.cancel(true);
	        } else {
	            // The same work is already in progress
	            return false;
	        }
	    }
	    // No task associated with the ImageView, or an existing task was cancelled
	    return true;
	}
	
	
	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
	   if (imageView != null) {
	       final Drawable drawable = imageView.getDrawable();
	       if (drawable instanceof AsyncDrawable) {
	           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
	           return asyncDrawable.getBitmapWorkerTask();
	       }
	    }
	    return null;
	}
	
	
	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
	    private final WeakReference<ImageView> imageViewReference;
	    private int data = 0;

	    public BitmapWorkerTask(ImageView imageView) {
	        // Use a WeakReference to ensure the ImageView can be garbage collected
	        imageViewReference = new WeakReference<ImageView>(imageView);
	    }

	    // Decode image in background.
	    @Override
	    protected Bitmap doInBackground(Integer... params) {
	        data = params[0];
	        return decodeSampledBitmapFromResource(mioContext.getResources(), data, larg, alt);
	    }

	    // Once complete, see if ImageView is still around and set bitmap.
	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	        if (isCancelled()) {
	            bitmap = null;
	        }

	        if (imageViewReference != null && bitmap != null) {
	            final ImageView imageView = imageViewReference.get();
	            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
	            if (this == bitmapWorkerTask && imageView != null) {
	                imageView.setImageBitmap(bitmap);
	            }
	        }
	    }
	}
	
	
	//variabili
	private Context mioContext;
	public final static Integer[] arrIconeId = new Integer[] {R.drawable.tag_0, R.drawable.tag_1, R.drawable.tag_2, R.drawable.tag_3, R.drawable.tag_4, R.drawable.tag_5, R.drawable.tag_6, R.drawable.tag_7, R.drawable.tag_8, R.drawable.tag_9,
		R.drawable.tag_10, R.drawable.tag_11, R.drawable.tag_12, R.drawable.tag_13, R.drawable.tag_14, R.drawable.tag_15, R.drawable.tag_16, R.drawable.tag_17, R.drawable.tag_18, R.drawable.tag_19,
		R.drawable.tag_20, R.drawable.tag_21, R.drawable.tag_22, R.drawable.tag_23, R.drawable.tag_24, R.drawable.tag_25, R.drawable.tag_26, R.drawable.tag_27, R.drawable.tag_28, R.drawable.tag_29,
		R.drawable.tag_30, R.drawable.tag_31, R.drawable.tag_32, R.drawable.tag_33, R.drawable.tag_34, R.drawable.tag_35, R.drawable.tag_36, R.drawable.tag_37, R.drawable.tag_38, R.drawable.tag_39,
		R.drawable.tag_40, R.drawable.tag_41, R.drawable.tag_42, R.drawable.tag_43, R.drawable.tag_44, R.drawable.tag_45, R.drawable.tag_46, R.drawable.tag_47, R.drawable.tag_48, R.drawable.tag_49,
		R.drawable.tag_50, R.drawable.tag_51, R.drawable.tag_52, R.drawable.tag_53, R.drawable.tag_54, R.drawable.tag_55, R.drawable.tag_56, R.drawable.tag_57, R.drawable.tag_58};
	private int larg;
	private int alt;
}
