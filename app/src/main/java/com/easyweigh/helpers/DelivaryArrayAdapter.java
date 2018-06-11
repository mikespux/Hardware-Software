package com.easyweigh.helpers;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;

public class DelivaryArrayAdapter extends ArrayAdapter<Delivary> {

	Context context;
	int layoutResourceId;
	ArrayList<Delivary> students = new ArrayList<Delivary>();

	public DelivaryArrayAdapter(Context context, int layoutResourceId,
								ArrayList<Delivary> studs) {
		super(context, layoutResourceId, studs);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.students = studs;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item = convertView;
		StudentWrapper StudentWrapper = null;

		if (item == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			item = inflater.inflate(layoutResourceId, parent, false);
			StudentWrapper = new StudentWrapper();
			StudentWrapper.number = (TextView) item.findViewById(R.id.tv_number);
			StudentWrapper.deldate = (TextView) item.findViewById(R.id.tv_date);
			StudentWrapper.totalkgs = (TextView) item.findViewById(R.id.txtTotalKgs);
			StudentWrapper.print = (Button) item.findViewById(R.id.btnPrint);

			item.setTag(StudentWrapper);
		} else {
			StudentWrapper = (StudentWrapper) item.getTag();
		}

		Delivary student = students.get(position);
		StudentWrapper.number.setText(student.getName());
		StudentWrapper.deldate.setText(student.getAge());
		StudentWrapper.totalkgs.setText(student.getAddress());

		StudentWrapper.print.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(context, "Edit", Toast.LENGTH_LONG).show();
			}
		});
		


		return item;

	}

	static class StudentWrapper {
		TextView number;
		TextView deldate;
		TextView totalkgs;
		Button print;

	}

}
