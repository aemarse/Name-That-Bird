package com.alexmarse.namethatbird.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.alexmarse.namethatbird.LessonPracticeFragment;
import com.alexmarse.namethatbird.LessonTestFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	Fragment lFrag;
	Bundle args;
	
	// CONSTRUCTOR
	public TabsPagerAdapter(FragmentManager fm, Bundle args) {
		super(fm);
		this.args = args;
	}
	
	@Override
	public Fragment getItem(int index) {
		
		switch(index) {
			case 0:
				// LessonPractice Activity
				lFrag = new LessonPracticeFragment();
				lFrag.setArguments(args);
				return lFrag;
			case 1:
				// LessonTest Activity
				lFrag = new LessonTestFragment();
				lFrag.setArguments(args);
				return lFrag;
		}
		return null;
	}
	
	@Override
	public int getCount() {
		// get item count (num of tabs)
		return 2;
	}
	
}
