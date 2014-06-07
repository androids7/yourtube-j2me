package com.derevenetz.oleg.yourtube;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class CustomDialogFragment extends DialogFragment {
	private static final int TYPE_MESSAGE           = 0;
	private static final int TYPE_PROGRESS_METADATA = 1;
	private static final int TYPE_PROGRESS_VIDEO    = 2;
	private static final int TYPE_FORMAT_SELECTION  = 3;

	public static interface CustomDialogFragmentListener {
		public abstract void onMetadataProgressCancelled();
		public abstract void onVideoProgressCancelled();
		public abstract void onFormatSelected(String video_title, String itag, String extension, String url);
	}
	
	public static CustomDialogFragment newMessageInstance(String title, String message) {
		CustomDialogFragment fragment = new CustomDialogFragment();
		
		Bundle args = new Bundle();
		
		args.putInt   ("type",    TYPE_MESSAGE);
		args.putString("title",   title);
		args.putString("message", message);
		
		fragment.setArguments(args);
		
		return fragment;
	}
	
	public static CustomDialogFragment newMetadataProgressInstance() {
		CustomDialogFragment fragment = new CustomDialogFragment();
		
		Bundle args = new Bundle();
		
		args.putInt("type", TYPE_PROGRESS_METADATA);
		
		fragment.setArguments(args);
		
		return fragment;
	}
	
	public static CustomDialogFragment newVideoProgressInstance() {
		CustomDialogFragment fragment = new CustomDialogFragment();
		
		Bundle args = new Bundle();
		
		args.putInt("type", TYPE_PROGRESS_VIDEO);
		
		fragment.setArguments(args);
		
		return fragment;
	}
	
	public static CustomDialogFragment newFormatSelectionInstance(String video_title, String preferred_itag, ArrayList<String> itags, ArrayList<String> formats, ArrayList<String> extensions, ArrayList<String> urls) {
		CustomDialogFragment fragment = new CustomDialogFragment();
		
		Bundle args = new Bundle();
		
		args.putInt            ("type",           TYPE_FORMAT_SELECTION);
		args.putString         ("video_title",    video_title);
		args.putString         ("preferred_itag", preferred_itag);
		args.putStringArrayList("itags",          itags);
		args.putStringArrayList("formats",        formats);
		args.putStringArrayList("extensions",     extensions);
		args.putStringArrayList("urls",           urls);
		
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		
		if (getArguments().getInt("type") == TYPE_MESSAGE) {
			return new AlertDialog.Builder(getActivity()).setTitle(getArguments().getString("title"))
					                                     .setIcon(R.drawable.ic_alert)
					                                     .setMessage(getArguments().getString("message")).create();
		} else if (getArguments().getInt("type") == TYPE_PROGRESS_METADATA) {
    		final ProgressDialog dialog = new ProgressDialog(getActivity());
    		
			dialog.setMessage(getString(R.string.dialog_title_loading_metadata));
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);

			return dialog;
		} else if (getArguments().getInt("type") == TYPE_PROGRESS_VIDEO) {
    		final ProgressDialog dialog = new ProgressDialog(getActivity());
    		
			dialog.setMessage(getString(R.string.dialog_title_loading_video));
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setIndeterminate(false);
			dialog.setCancelable(true);

			return dialog;
		} else if (getArguments().getInt("type") == TYPE_FORMAT_SELECTION) {
			ArrayList<String> itags   = getArguments().getStringArrayList("itags");
			ArrayList<String> formats = getArguments().getStringArrayList("formats");

			int      checked_item = 0;
			String[] choices      = new String[formats.size()];

			for (int i = 0; i < formats.size(); i++) {
				if (itags.get(i).equals(getArguments().getString("preferred_itag"))) {
					checked_item = i;
				}

				choices[i] = formats.get(i);
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			
			builder.setTitle(getString(R.string.dialog_title_choose_format));
			builder.setSingleChoiceItems(choices, checked_item, null);
			builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ArrayList<String> itags      = getArguments().getStringArrayList("itags");
					ArrayList<String> extensions = getArguments().getStringArrayList("extensions");
					ArrayList<String> urls       = getArguments().getStringArrayList("urls");
					
					CustomDialogFragmentListener activity = (CustomDialogFragmentListener)getActivity();
					
					int position = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
					
					if (position >= 0 && position < itags.size()) {
						activity.onFormatSelected(getArguments().getString("video_title"), itags.get(position), extensions.get(position), urls.get(position));
					}
				}
			});
			
			return builder.create();
		} else {
			return null;
		}
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		
		CustomDialogFragmentListener activity = (CustomDialogFragmentListener)getActivity();
		
		if (getArguments().getInt("type") == TYPE_PROGRESS_METADATA) {
			activity.onMetadataProgressCancelled();
		} else if (getArguments().getInt("type") == TYPE_PROGRESS_VIDEO) {
			activity.onVideoProgressCancelled();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		
		CustomDialogFragmentListener activity = (CustomDialogFragmentListener)getActivity();
		
		if (getArguments().getInt("type") == TYPE_PROGRESS_METADATA) {
			activity.onMetadataProgressCancelled();
		} else if (getArguments().getInt("type") == TYPE_PROGRESS_VIDEO) {
			activity.onVideoProgressCancelled();
		}
	}
}
