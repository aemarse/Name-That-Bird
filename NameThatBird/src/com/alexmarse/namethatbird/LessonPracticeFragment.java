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
	
	String playlistId;
	String playlistName;
	int ntbLesson;
	int lesson;
	int numLessons;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_lesson_practice, container, false);
		
		tv_lesson = (TextView)rootView.findViewById(R.id.tv_lesson_num);
		
		// Get fragment arguments (bundle of data) and set our class variables
		Bundle bundle = this.getArguments();
		playlistId = bundle.getString("playlist_id");
		playlistName = bundle.getString("playlist_name");
		ntbLesson = bundle.getInt("ntb_lesson");
		lesson = bundle.getInt("lesson");
		numLessons = bundle.getInt("num_lessons");
		
		// Set the lesson number text
		tv_lesson.setText("Lesson: " + Integer.toString(lesson+1) + "/" + Integer.toString(numLessons));
		
		
		
		return rootView;
		
	}
	
	public void onClickPlayer(View v) {
		
		// Figure out which player icon was clicked
		switch(v.getId()) {
    	
		case R.id.b_previous:
    		Log.e("player clicked: ", "previous");
		case R.id.b_pause:
			Log.e("player clicked: ", "pause");
		case R.id.b_play:
			Log.e("player clicked: ", "play");
		case R.id.b_next:
			Log.e("player clicked: ", "next");
		}
		
	}
	
}
