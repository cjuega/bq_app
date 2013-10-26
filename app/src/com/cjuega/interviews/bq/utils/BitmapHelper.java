package com.cjuega.interviews.bq.utils;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import com.cjuega.interviews.bq.main.DropboxLibraryApplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class BitmapHelper {

	public void loadBitmapFromInputStream(ImageView imageViewToAttach, InputStream input){
		StreamBitmapWorkerTask task = new StreamBitmapWorkerTask(imageViewToAttach);
		task.execute(input);
	}
	
	public void loadBitmapFromResources(ImageView imageViewToAttach, int resId){
		ResourcesBitmapWorkerTask task = new ResourcesBitmapWorkerTask(imageViewToAttach);
		task.execute(resId);
	}
	
	class StreamBitmapWorkerTask extends AsyncTask<InputStream, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;

		public StreamBitmapWorkerTask (ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected Bitmap doInBackground(InputStream... params) {
			InputStream input = params[0];
			return BitmapFactory.decodeStream(input);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (imageViewReference != null && result != null) {
	            final ImageView imageView = imageViewReference.get();
	            if (imageView != null) {
	                imageView.setImageBitmap(result);
	            }
	        }
		}
	}
	
	class ResourcesBitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;

		public ResourcesBitmapWorkerTask (ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}
		
		@Override
		protected Bitmap doInBackground(Integer... params) {
			int id = params[0];
			return BitmapFactory.decodeResource(DropboxLibraryApplication.getAppContext().getResources(), id);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (imageViewReference != null && result != null) {
	            final ImageView imageView = imageViewReference.get();
	            if (imageView != null) {
	                imageView.setImageBitmap(result);
	            }
	        }
		}
	}
}
