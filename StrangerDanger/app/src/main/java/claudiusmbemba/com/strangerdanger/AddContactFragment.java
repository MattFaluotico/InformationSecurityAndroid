package claudiusmbemba.com.strangerdanger;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ClaudiusThaBeast on 4/10/15.
 */
public class AddContactFragment extends Fragment implements View.OnClickListener {

    public AddContactFragment() {
        //empty constructor
    }

    Button btn;
    TextView contacts;
    EditText name;
    EditText phone;
    EditText email;
    SharedPreferences prefs;
    String names = null;
    String phones = null;
    String emails = null;
    String[] allNames = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(
                R.layout.fragment_add, container, false);

        prefs = this.getActivity().getSharedPreferences("claudiusmbemba.com.strangerdanger", Context.MODE_PRIVATE);

        contacts = (TextView) view.findViewById(R.id.contacts_view);

        btn = (Button) view.findViewById(R.id.save_contact);
        btn.setOnClickListener(this);

        name = (EditText) view.findViewById(R.id.name_text);
        name.requestFocus();


        names = prefs.getString("names", "");
        phones = prefs.getString("phones", "");
        emails = prefs.getString("emails", "");

        if(!names.matches("")){
            String[] allPhones = null;
            String[] allEmails = null;

            if (!names.matches("")) {
                allNames = names.split(",");
            }
            if (!phones.matches("")) {
                allPhones = phones.split(",");
            }
            if (!emails.matches("")) {
                allEmails = emails.split(",");
            }

            String cont = "";
            for (int i = 0; i <= allNames.length-1; i++) {
                cont += allNames[i] + "\n" + allPhones[i] + "\n" + allEmails[i]+"\n\n";
            }

            contacts.setText(cont);

            if(allNames.length >= 5){
                btn.setEnabled(false);
            }

        }

        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_add, container, false);
        return view;
    }


    @Override
    public void onClick(View v) {

//        save(v);
//        Toast.makeText(this.getActivity(),
//                "Button is clicked!", Toast.LENGTH_LONG).show();
        contacts = (TextView) getActivity().findViewById(R.id.contacts_view);
//        Button save = (Button) getActivity().findViewById(R.id.save_contact);
        name = (EditText) getActivity().findViewById(R.id.name_text);
        phone = (EditText) getActivity().findViewById(R.id.phone_text);
        email = (EditText) getActivity().findViewById(R.id.email_text);

        if (!TextUtils.isEmpty(name.getText().toString())) {

            if (TextUtils.isEmpty(phone.getText().toString()) || TextUtils.isEmpty(email.getText().toString())) {
                Toast.makeText(this.getActivity(), "Please add contacts phone # and email", Toast.LENGTH_SHORT).show();
            } else {
                //save contact to preferences
                names += (names.matches(""))? name.getText(): ","+name.getText();
                String[] nameCount = names.split(",");

                if(nameCount.length <= 5){
                    phones += (phones.matches(""))? phone.getText():","+phone.getText();
                    emails += (emails.matches(""))? email.getText():","+email.getText();

                    prefs.edit().putString("names", names).putString("phones", phones).putString("emails", emails).commit();

//                if (TextUtils.isEmpty(phone.getText().toString())) {
//                    contacts.setText(contacts.getText().toString() + "\n" + name.getText().toString() + "\n" + email.getText().toString() + "\n");
//                } else if (TextUtils.isEmpty(email.getText().toString())) {
//                    contacts.setText(contacts.getText().toString() + "\n" + name.getText().toString() + "\n" + phone.getText().toString() + "\n");
//                } else {
                    contacts.setText(contacts.getText().toString() + name.getText().toString() + "\n" + phone.getText().toString() + "\n" + email.getText().toString() + "\n");
//                }

                    name.setText("");
                    phone.setText("");
                    email.setText("");

                    Toast.makeText(this.getActivity(), "Contact saved!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this.getActivity(), "Reached max of 5 contacts saved!", Toast.LENGTH_SHORT).show();
                    btn.setEnabled(false);
                }
            }
        } else {
            Toast.makeText(this.getActivity(), "Please add a contact before saving", Toast.LENGTH_SHORT).show();
        }
    }
}
