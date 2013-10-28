package com.cjuega.interviews.bq.widgets;

import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class EndlessSortedListAdapter<T> extends SortedListAdapter<T> {
			
	private DataRequester mRequester;
	private boolean mLoading;
	private boolean mMoreDataToLoad;
	
	/* Constructors */
	
	public EndlessSortedListAdapter(Context context, int textViewResourceId,
			Comparator<? super T> comparator, DataRequester requester) {
		super(context, textViewResourceId, comparator);
		init(requester);
	}

	public EndlessSortedListAdapter(Context context, int resource,
			int textViewResourceId, Comparator<? super T> comparator, DataRequester requester) {
		super(context, resource, textViewResourceId, comparator);
		init(requester);
	}

	public EndlessSortedListAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects,
			Comparator<? super T> comparator, DataRequester requester) {
		super(context, resource, textViewResourceId, objects, comparator);
		init(requester);
	}

	public EndlessSortedListAdapter(Context context, int resource,
			int textViewResourceId, T[] objects,
			Comparator<? super T> comparator, DataRequester requester) {
		super(context, resource, textViewResourceId, objects, comparator);
		init(requester);
	}

	public EndlessSortedListAdapter(Context context, int textViewResourceId,
			List<T> objects, Comparator<? super T> comparator, DataRequester requester) {
		super(context, textViewResourceId, objects, comparator);
		init(requester);
	}

	public EndlessSortedListAdapter(Context context, int textViewResourceId,
			T[] objects, Comparator<? super T> comparator, DataRequester requester) {
		super(context, textViewResourceId, objects, comparator);
		init(requester);
	}
	
	/* Methods */
	
	private void init(DataRequester requester){
		if (requester == null){
			Log.e("EndlessSortedListAdapter", "You must supply a DataRequester object");
			throw new IllegalStateException("EndlessSortedListAdapter requires a DataRequester");
		}
			
		mRequester = requester;
		mLoading = false;
		mMoreDataToLoad = true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);

		if (shouldLoadMoreData(mData, position)){
			mLoading = true;
			mRequester.requestData();
		}
		
		return view;
	}
	
	public boolean isLoading(){
		return mLoading;
	}
	
	public void setLoading(boolean loading) {
		mLoading = loading;
	}

	private boolean shouldLoadMoreData(List<T> list, int position){
		boolean scrollRangeReached = (position > list.size()/2);
		return (scrollRangeReached && !mLoading && mMoreDataToLoad);
	}
}
