package com.amk2.musicrunner.setting;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.login.LoginActivity;
import com.amk2.musicrunner.utilities.RestfulUtility;
import com.amk2.musicrunner.utilities.SharedPreferencesUtility;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment implements View.OnClickListener {
    private String account = null;
    private EditText fullNameET = null;
    private EditText birthdayET = null;
    private EditText weightET = null;
    private EditText heightET = null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.setting_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// This function is like onCreate() in activity.
		// You can start from here.
        account = SharedPreferencesUtility.getAccount(getActivity());
        TextView accountTextView = (TextView) getView().findViewById(R.id.setting_account);
        accountTextView.setText(account);

        Button updateButton = (Button)getView().findViewById(R.id.setting_update);
        updateButton.setOnClickListener(this);

        Button logoutButton = (Button) getView().findViewById(R.id.setting_logout);
        logoutButton.setOnClickListener(this);

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("userAccount", account));
        HttpResponse response = RestfulUtility.restfulPostRequest(RestfulUtility.GET_SETTING_INFO, pairs);
        String settingString = RestfulUtility.getStatusCode(response);
        JSONObject settingJson = null;
        /*try{
            if(settingString != null)
                settingJson = new JSONObject(settingString);

            fullNameET = (EditText) getView().findViewById(R.id.setting_fullName);
            fullNameET.setText(settingJson.getString("fullName"));
            birthdayET = (EditText) getView().findViewById(R.id.setting_birthday);
            birthdayET.setText(settingJson.getString("birthday"));
            weightET = (EditText) getView().findViewById(R.id.setting_weight);
            weightET.setText(settingJson.getString("weight"));
            heightET = (EditText) getView().findViewById(R.id.setting_height);
            heightET.setText(settingJson.getString("height"));
        } catch (JSONException je){

        }*/
	}

    public void updateSettings(View view){

    }
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.setting_update:

                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("userAccount", account));
                pairs.add(new BasicNameValuePair("fullName", fullNameET.getText().toString()));
                pairs.add(new BasicNameValuePair("birthday", birthdayET.getText().toString()));
                pairs.add(new BasicNameValuePair("weightValue", weightET.getText().toString()));
                pairs.add(new BasicNameValuePair("heightValue", heightET.getText().toString()));
                HttpResponse response = RestfulUtility.restfulPostRequest(RestfulUtility.UPDATE_SETTINGS, pairs);

                TextView updateStatusTV = (TextView) getView().findViewById(R.id.setting_status);
                updateStatusTV.setText("update!");
                break;

            case R.id.setting_logout:
                SharedPreferencesUtility.clearSharedPreference(getActivity());
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                break;
        }
    }

}
