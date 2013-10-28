package com.cjuega.interviews.bq.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class DoubleClickSupportedListView extends ListView {

	private GestureDetector mDetector;
	private OnItemDoubleClickListener mDoubleClickListener;
	
	public interface OnItemDoubleClickListener{
		public void OnItemDoubleClick(AdapterView<?> parent, View view, int position, long id);
	}
	
	public DoubleClickSupportedListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mDetector = new GestureDetector(context, new DoubleClickGestureListener());
	}

	public DoubleClickSupportedListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDetector = new GestureDetector(context, new DoubleClickGestureListener());
	}

	public DoubleClickSupportedListView(Context context) {
		super(context);
		mDetector = new GestureDetector(context, new DoubleClickGestureListener());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!mDetector.onTouchEvent(ev))
			return super.onTouchEvent(ev);
		return true;
	}

	public void setOnItemDoubleClickListener(OnItemDoubleClickListener listener){
		mDoubleClickListener = listener;
	}
	
	public OnItemDoubleClickListener getOnItemDoubleClickListener(){
		return mDoubleClickListener;
	}
	
	private class DoubleClickGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mDoubleClickListener != null){
				
				int position = DoubleClickSupportedListView.this.pointToPosition((int)e.getX(), (int)e.getY());
				
				if (position != INVALID_POSITION){
					mDoubleClickListener.OnItemDoubleClick(DoubleClickSupportedListView.this,
														   DoubleClickSupportedListView.this.getChildAt(position),
														   position,
														   DoubleClickSupportedListView.this.getAdapter().getItemId(position));
					return true;
				}
			}
			return false;
		}
	}
}
