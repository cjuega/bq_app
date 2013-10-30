package com.cjuega.interviews.bq.widgets;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author cjuega
 *
 * This class is an extension of {@link SortedListAdapter SortedListAdapter} that implements a 
 * sorted list without ending. Every time a user scrolls down the list, this adapter will 
 * automatically request new data to a {@link DataRequester DataRequester}. 
 *
 * @param <T>	The element's type this adapter will hold.
 */
public class EndlessSortedListAdapter<T> extends SortedListAdapter<T> {
			
	protected DataRequester mRequester;
	private boolean mLoading;
	private boolean mMoreDataToLoad;
	
	/**
	 * 
	 * @author cjuega
	 *
	 * Simple interface used by {@link EndlessSortedListAdapter EndlessSortedListAdapter} to request new data.
	 *
	 */
	public interface DataRequester {
		public void requestData();
	}
	
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
		
		mRequester.requestData();
	}

	@Override
	public void addAll(T... items) {
		if(items.length > 0)
			super.addAll(items);
		else
			mMoreDataToLoad = false;
	}

	@Override
	public void addAll(Collection<? extends T> collection) {
		if (!collection.isEmpty())
			super.addAll(collection);
		else
			mMoreDataToLoad = false;		
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
	
	public void setNoMoreDataToLoad() {
		mMoreDataToLoad = false;
	}

	protected boolean shouldLoadMoreData(List<T> list, int position){
		boolean scrollRangeReached = (position > list.size()/2);
		return (scrollRangeReached && !mLoading && mMoreDataToLoad);
	}
}
