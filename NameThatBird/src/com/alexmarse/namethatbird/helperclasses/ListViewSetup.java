package com.alexmarse.namethatbird.helperclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

public class ListViewSetup {

	// MEMBER VARIABLES
	String[] dataArr;
	ListView listView;
	GridView gridView;
	
	// CONSTRUCTORS
	public ListViewSetup(Context context, String[] playlistData, ListView listView) {
		this.dataArr = playlistData;
		this.listView = listView;
	}
	
	public ListViewSetup(Context context, String[] playlistData, GridView gridView) {
		this.dataArr = playlistData;
		this.gridView = gridView;
	}
	
	// METHODS
	
	// Set up the list
	public ListView setupList() {
		
		// Convert String[] to ArrayList<String>
		ArrayList<String> dataArrList = new ArrayList<String>();
		for (int i = 0; i < dataArr.length; i++) {
			dataArrList.add(dataArr[i]);
		}
		
		// Set the list adapter
		final StableArrayAdapter adapter = new StableArrayAdapter(listView.getContext(), android.R.layout.simple_list_item_1, dataArrList);
		listView.setAdapter(adapter);
		
		return listView;
	}
	
	// Set up the list
	public GridView setupGrid() {
		
		// Convert String[] to ArrayList<String>
		ArrayList<String> dataArrList = new ArrayList<String>();
		for (int i = 0; i < dataArr.length; i++) {
			dataArrList.add(dataArr[i]);
		}
		
		// Set the list adapter
		final StableArrayAdapter adapter = new StableArrayAdapter(gridView.getContext(), android.R.layout.simple_list_item_1, dataArrList);
		gridView.setAdapter(adapter);
		
		return gridView;
	}
	
}

// ArrayAdapter
class StableArrayAdapter extends ArrayAdapter<String> {

	HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();
	
	// The constructor
	public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
		super(context, textViewResourceId, objects);
		for(int i = 0; i < objects.size(); ++i) {
			mIdMap.put(objects.get(i), i);
		}
	}

	@Override
	public long getItemId(int position) {
		String item = getItem(position);
		return mIdMap.get(item);
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
}