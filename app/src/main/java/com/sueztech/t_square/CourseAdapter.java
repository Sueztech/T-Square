package com.sueztech.t_square;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static android.content.ContentValues.TAG;

class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {
	private List<JSONObject> mDataset;

	CourseAdapter (List<JSONObject> myDataset) {
		mDataset = myDataset;
	}

	@Override
	public CourseAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_course, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder (ViewHolder holder, int position) {
		holder.setItem(mDataset.get(position));
	}

	@Override
	public int getItemCount () {
		return mDataset.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		View mCardView;
		TextView mNameView;
		TextView mDescView;
		JSONObject mJsonObject;
		String mId;
		String mName;
		String mDesc;

		ViewHolder (View v) {
			super(v);
			v.setOnClickListener(this);
			mCardView = v;
			mNameView = (TextView) v.findViewById(R.id.course_name);
			mDescView = (TextView) v.findViewById(R.id.course_desc);
		}

		public void setItem (JSONObject jsonObject) {
			mJsonObject = jsonObject;
			try {
				mId = jsonObject.getString("id");
				mName = jsonObject.getString("title");
				mDesc = jsonObject.getString("shortDescription");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mNameView.setText(mName);
			mDescView.setText(mDesc);
		}

		@Override
		public void onClick (View view) {
			Log.d(TAG, "onClick " + getLayoutPosition() + " " + mId);
		}
	}
}

