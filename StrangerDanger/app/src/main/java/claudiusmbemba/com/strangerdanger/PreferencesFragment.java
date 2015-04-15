package claudiusmbemba.com.strangerdanger;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by ClaudiusThaBeast on 4/9/15.
 */
public class PreferencesFragment extends Fragment implements View.OnClickListener{


    public PreferencesFragment() {
        // Required empty public constructor
    }

    private CheckBox sms;
    private CheckBox email;
    private CheckBox alerts;
    private Button save;
    private View email_cred;
    private EditText emailAddress;
    private EditText emailPass;
    private EditText userName;

    SharedPreferences prefs;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preferences, container, false);

        prefs = this.getActivity().getSharedPreferences("claudiusmbemba.com.strangerdanger", Context.MODE_PRIVATE);

        sms = (CheckBox) view.findViewById(R.id.sms_checkbox);
        email = (CheckBox) view. findViewById(R.id.email_checkbox);
        alerts = (CheckBox) view.findViewById(R.id.alert_checkbox);

        save = (Button) view.findViewById(R.id.save_prefs);
        save.setOnClickListener(this);

        email_cred = view.findViewById(R.id.email_credentials);
        emailAddress = (EditText) view.findViewById(R.id.email_cred_text);
        emailPass = (EditText) view.findViewById(R.id.email_pass_cred_text);

        userName = (EditText) view.findViewById(R.id.userName_text);

        String uName = prefs.getString("UserName", "");
        if(!uName.matches("")){
            userName.setText(uName);
        }

        if(prefs.getBoolean("sms", false)){
            sms.setChecked(true);
        }
        if(prefs.getBoolean("email", false)){
            email.setChecked(true);
            email_cred.setEnabled(true);
            emailAddress.setText(prefs.getString("emailAddress", ""));
            emailPass.setText(prefs.getString("emailPass", ""));

        }else{
            email_cred.setEnabled(false);
        }
        if(prefs.getBoolean("alerts", false)){
            alerts.setChecked(true);
        }

        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_preferences, container, false);
        return view;
    }


    @Override
    public void onClick(View v) {

        Boolean smsPref = sms.isChecked();
        Boolean emailPref = email.isChecked();
        Boolean alertPref = alerts.isChecked();

        String emailAdd = emailAddress.getText().toString();
        String pass = emailPass.getText().toString();

        prefs.edit().putBoolean("sms", smsPref).putBoolean("email", emailPref).putBoolean("alerts", alertPref).putString("emailAddress", emailAdd).putString("emailPass", pass).putString("UserName", userName.getText().toString()).commit();

        Toast.makeText(this.getActivity(), "Changes saved!", Toast.LENGTH_LONG).show();
    }
}