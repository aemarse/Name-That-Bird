package com.alexmarse.namethatbird;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LessonPracticeFragment extends Fragment {

	TextView tv_lesson;
	
	int LESSON_NUM = 1;
	int NUM_LESSONS;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_lesson_practice, container, false);
		
		tv_lesson = (TextView)rootView.findViewById(R.id.tv_lesson_num);
		tv_lesson.setText(Integer.toString(LESSON_NUM));
		
		Log.e("args: ", this.getArguments().toString());
		
		return rootView;
		
	}
	
}
