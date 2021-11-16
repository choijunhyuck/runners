package com.runners.choi.runners.challenge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.runners.choi.runners.R;

import java.util.HashMap;
import java.util.List;

public class ChallengeListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;

    private List<ChallengeItem> challengeItem;

    private TextView challengeNum;
    private TextView challengeName;
    private TextView challengeCondition;

    public ChallengeListAdapter(Activity activity, List<ChallengeItem> challengeItem) {
        this.activity = activity;
        this.challengeItem = challengeItem;
    }

    @Override
    public int getCount() {
        return challengeItem.size();
    }

    @Override
    public Object getItem(int location) {
        return challengeItem.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.challenge_item, null);

        final ChallengeItem item = challengeItem.get(position);

        challengeNum = (TextView) convertView
                .findViewById(R.id.challenge_num);
        challengeNum.setText(ChallengeNumber.setNumber(item.getSalt()));

        challengeName = (TextView) convertView
                .findViewById(R.id.challenge_name);
        challengeName.setText(ChallengeName.setName(item.getSalt()));

        challengeCondition = (TextView) convertView
                .findViewById(R.id.challenge_condition);
        challengeCondition.setText(ChallengeCondition.setCondition(item.getSalt()));



        return convertView;
        }


}