package com.derevenetz.oleg.yourtube;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;
import android.os.Bundle;

public class VideoDownloader extends AsyncTask<Bundle, Long, String> {
	public static interface VideoDownloaderListener {
		public abstract void onVideoDownloadProgressUpdate(int percent);
		public abstract void onVideoDownloadComplete(String error_message);
	}
	
	public VideoDownloaderListener callbackListener;
	
	public VideoDownloader(VideoDownloaderListener listener) {
		callbackListener = listener;
	}
	
	@Override
	protected String doInBackground(Bundle... params) {
		try {
			URL    url         = new URL(params[0].getString("url"));
			String output_dir  = params[0].getString("output_dir");
			String output_file = params[0].getString("output_file");

			(new File(output_dir)).mkdirs();
			
			URLConnection conn = url.openConnection();
			
			conn.connect();
			
			int content_length = conn.getContentLength();
			
			InputStream      input_stream  = new BufferedInputStream(url.openStream());
			FileOutputStream output_stream = new FileOutputStream(output_dir + "/" + output_file);
			
			int  count;
			long total    = 0;
			byte buffer[] = new byte[65536];
			
			while ((count = input_stream.read(buffer)) != -1) {
				output_stream.write(buffer, 0, count);
				
				total = total + count;
				
				publishProgress(Long.valueOf((total * 100) / content_length));

				if (isCancelled()) {
					break;
				}
			}
			
			output_stream.flush();

			input_stream.close();
			output_stream.close();
			
			if (isCancelled()) {
				(new File(output_dir + "/" + output_file)).delete();
			}
			
			return "";
		} catch (Exception ex) {
			return ex.toString();
		}
	}
	
	@Override
	protected void onProgressUpdate(Long... progress) {
		super.onProgressUpdate(progress);
		
		callbackListener.onVideoDownloadProgressUpdate(progress[0].intValue());
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();

		callbackListener.onVideoDownloadComplete(null);
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		if (isCancelled()) {
			callbackListener.onVideoDownloadComplete(null);
		} else {
			callbackListener.onVideoDownloadComplete(result);
		}
	}
}
