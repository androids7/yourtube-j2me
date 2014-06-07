package com.derevenetz.oleg.yourtube;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

public class MetadataDownloader extends AsyncTask<String, Void, String> {
	public static interface MetadataDownloaderListener {
		public abstract void onMetadataDownloadComplete(String metadata);
	}
	
	public MetadataDownloaderListener callbackListener;
	
	public MetadataDownloader(MetadataDownloaderListener listener) {
		callbackListener = listener;
	}
	
	@Override
	protected String doInBackground(String... params) {
		try {
			URL url = new URL(params[0]);
			
			URLConnection conn = url.openConnection();
			
			conn.connect();
			
			InputStream           input_stream  = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream output_stream = new ByteArrayOutputStream(65536);
			
			int  count;
			byte buffer[] = new byte[65536];
			
			while ((count = input_stream.read(buffer)) != -1) {
				output_stream.write(buffer, 0, count);

				if (isCancelled()) {
					break;
				}
			}
			
			output_stream.flush();

			input_stream.close();
			
			return output_stream.toString("UTF-8");
		} catch (Exception ex) {
			// Ignored
		}
		
		return "";
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();

		callbackListener.onMetadataDownloadComplete(null);
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		
		if (isCancelled()) {
			callbackListener.onMetadataDownloadComplete(null);
		} else {
			callbackListener.onMetadataDownloadComplete(result);
		}
	}
}
