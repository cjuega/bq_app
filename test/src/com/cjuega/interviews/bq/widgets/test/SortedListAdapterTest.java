package com.cjuega.interviews.bq.widgets.test;

import java.util.ArrayList;
import java.util.Comparator;

import junit.framework.TestCase;

import com.cjuega.interviews.bq.main.DropboxLibraryApplication;
import com.cjuega.interviews.bq.widgets.SortedListAdapter;

public class SortedListAdapterTest extends TestCase  {

	public void test_include_collection(){
		SortedListAdapter<Integer> adapter = new SortedListAdapter<Integer>(DropboxLibraryApplication.getAppContext(),
																			0,
																			new Comparator<Integer>() {
																				@Override
																				public int compare(Integer lhs, Integer rhs) {
																					return lhs.compareTo(rhs);
																				}
																			});
		assertNotNull("The Adapter was not created!", adapter);
		
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		
		for (int i=10; i>0; i--){
			numbers.add(i);
		}
		
		adapter.addAll(numbers);
		
		boolean correctOrder = true;
		
		for (int i=0; i<adapter.getCount()-1; i++){
			if (adapter.getItem(i)>adapter.getItem(i+1)){
				correctOrder = false;
				break;
			}
		}
		
		assertTrue("The elements within the adapter are not sorted when use addAll", correctOrder);
	}
	
	public void test_include_element_by_element() {
		SortedListAdapter<Integer> adapter = new SortedListAdapter<Integer>(DropboxLibraryApplication.getAppContext(),
																			0,
																			new Comparator<Integer>() {
																				@Override
																				public int compare(Integer lhs, Integer rhs) {
																					return lhs.compareTo(rhs);
																				}
																			});
		
		for (int i=10; i>0; i--){
			adapter.add(i);
		}
		
		boolean correctOrder = true;
		
		for (int i=0; i<adapter.getCount()-1; i++){
			if (adapter.getItem(i)>adapter.getItem(i+1)){
				correctOrder = false;
				break;
			}
		}
		
		assertTrue("The elements within the adapter are not sorted when use add", correctOrder);
	}
}
