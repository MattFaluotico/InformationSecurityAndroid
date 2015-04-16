package claudiusmbemba.com.strangerdanger;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import static android.widget.AdapterView.OnItemClickListener;

/**
 * Created by ClaudiusThaBeast on 4/10/15.
 */
public class AddContactFragment extends Fragment implements View.OnClickListener, OnItemClickListener {

    public AddContactFragment() {
        //empty constructor
    }

    private Button save_btn;
    private EditText name;
    private EditText phone;
    private EditText email;
    private SharedPreferences prefs;
    ListView contacts;
    private int contact_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(
                R.layout.fragment_add, container, false);

        prefs = this.getActivity().getSharedPreferences("claudiusmbemba.com.strangerdanger", Context.MODE_PRIVATE);

        contacts = (ListView) view.findViewById(R.id.list);

        save_btn = (Button) view.findViewById(R.id.save_contact);
        save_btn.setOnClickListener(this);

        name = (EditText) view.findViewById(R.id.name_text);
        phone = (EditText) view.findViewById(R.id.phone_text);
        email = (EditText) view.findViewById(R.id.email_text);

        //disable editTexts until user clicks a individual item
        name.setEnabled(false);
        phone.setEnabled(false);
        email.setEnabled(false);
        save_btn.setEnabled(false);

        contacts.requestFocus();

        populate_listView();
        contacts.setOnItemClickListener(this);

        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_add, container, false);
        return view;
    }

    private void populate_listView() {
        String contact1,contact2,contact3,contact4,contact5;

        contact1 = (prefs.getString("name_0", "").matches("")? "Click to add contact" : prefs.getString("name_0", "") );
        contact2 = (prefs.getString("name_1", "").matches("")? "Click to add contact" : prefs.getString("name_1", "") );
        contact3 = (prefs.getString("name_2", "").matches("")? "Click to add contact" : prefs.getString("name_2", "") );
        contact4 = (prefs.getString("name_3", "").matches("")? "Click to add contact" : prefs.getString("name_3", "") );
        contact5 = (prefs.getString("name_4", "").matches("")? "Click to add contact" : prefs.getString("name_4", "") );

        String[] ICEs = new String[]{contact1,contact2,contact3,contact4,contact5};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, ICEs);
        contacts.setAdapter(adapter);
    }


    @Override
    public void onClick(View v) {
        String contact_num = String.valueOf(contact_id);

        if (!TextUtils.isEmpty(name.getText().toString())) {

            if (TextUtils.isEmpty(phone.getText().toString()) || TextUtils.isEmpty(email.getText().toString())) {
                Toast.makeText(this.getActivity(), "Please add contact's phone # and email", Toast.LENGTH_SHORT).show();
            } else {

                //save contact to preferences
                prefs.edit().putString("name_"+contact_num, name.getText().toString()).putString("phone_"+contact_num, phone.getText().toString()).putString("email_"+contact_num, email.getText().toString()).apply();

                //clear fields
                name.setText("");
                phone.setText("");
                email.setText("");

                Toast.makeText(this.getActivity(), "Contact saved!", Toast.LENGTH_SHORT).show();

                //disable edit options
                name.setEnabled(false);
                phone.setEnabled(false);
                email.setEnabled(false);
                save_btn.setEnabled(false);

                //refresh listView
                populate_listView();
            }
        } else {
            Toast.makeText(this.getActivity(), "Please add a contact before saving", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        contact_id = position;

//        Log.d("POSITION", String.valueOf(position));
//        Log.d("ID", String.valueOf(id));

        //enable edit options
        name.setEnabled(true);
        phone.setEnabled(true);
        email.setEnabled(true);
        save_btn.setEnabled(true);

        name.requestFocus();

        //clear fields
        name.setText("");
        phone.setText("");
        email.setText("");


        String contact_id = String.valueOf(position);
        //set text fields
        String person = prefs.getString("name_"+contact_id, "");
        if(!person.matches("")){
            name.setText(person);
            phone.setText(prefs.getString("phone_"+contact_id, ""));
            email.setText(prefs.getString("email_"+contact_id, ""));
        }
    }

}
