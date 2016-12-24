package com.sueztech.t_square;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
	private List<JSONObject> mDataset;

	// Provide a suitable constructor (depends on the kind of dataset)
	public CourseAdapter (List<JSONObject> myDataset) {
		mDataset = myDataset;
	}

	// Create new views (invoked by the layout manager)
	@Override
	public CourseAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_course, parent, false);
		ViewHolder vh = new ViewHolder(v);
		return vh;
	}

	// Replace the contents of a view (invoked by the layout manager)
	@Override
	public void onBindViewHolder (ViewHolder holder, int position) {
		try {
			((TextView) holder.mCardView.findViewById(R.id.course_name)).setText(mDataset.get(position).getString("title"));
			((TextView) holder.mCardView.findViewById(R.id.course_desc)).setText(mDataset.get(position).getString("shortDescription"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	// Return the size of your dataset (invoked by the layout manager)
	@Override
	public int getItemCount () {
		return mDataset.size();
	}

	// Provide a reference to the views for each data item
	// Complex data items may need more than one view per item, and
	// you provide access to all the views for a data item in a view holder
	public static class ViewHolder extends RecyclerView.ViewHolder {

		public View mCardView;

		public ViewHolder (View v) {
			super(v);
			mCardView = v;
		}
	}
}

