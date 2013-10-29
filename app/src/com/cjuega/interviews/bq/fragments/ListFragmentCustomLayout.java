package com.cjuega.interviews.bq.fragments;

import com.cjuega.interviews.bq.R;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

/**
 * 
 * @author cjuega
 *
 * ListFragment used to show/hide a ProgressBar when a custom layout is supplied. Note 
 * that standard {@code ListFragment} does not allow it.
 *
 */
public class ListFragmentCustomLayout extends ListFragment{
	private boolean mListShown;
	private View mProgressContainer;
	private View mListContainer;
	private View mListFooter;
	private View mListFooterProgress;

	public void setListShown(boolean shown, boolean animate){
	    if (mListShown == shown) {
	        return;
	    }
	    
	    if (getActivity() == null || mProgressContainer == null || mListContainer == null){
	    	return;
	    }
	    
	    mListShown = shown;
	    if (shown) {
	        if (animate) {
	            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_out));
	            mListContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_in));
	        }
	        mProgressContainer.setVisibility(View.GONE);
	        mListContainer.setVisibility(View.VISIBLE);
	    } else {
	        if (animate) {
	            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_in));
	            mListContainer.startAnimation(AnimationUtils.loadAnimation(
	                    getActivity(), android.R.anim.fade_out));
	        }
	        mProgressContainer.setVisibility(View.VISIBLE);
	        mListContainer.setVisibility(View.INVISIBLE);
	    }
	}
	
	public void setListShown(boolean shown){
	    setListShown(shown, true);
	}
	
	public void setListShownNoAnimation(boolean shown) {
	    setListShown(shown, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View root = inflater.inflate(R.layout.fragment_file_list, container, false);

	    mListContainer =  root.findViewById(R.id.list_container);
	    mProgressContainer = root.findViewById(R.id.progress_container);
	    mListFooter = inflater.inflate(R.layout.list_footer_progress_element, null, false);
	    mListFooterProgress = mListFooter.findViewById(R.id.progress);
	    mListShown = true;
	    mListFooterProgress.setVisibility(View.GONE);
	    return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().addFooterView(mListFooter);
	}
	
	public View getLoadingFooterView(){
		return mListFooterProgress;
	}
}
