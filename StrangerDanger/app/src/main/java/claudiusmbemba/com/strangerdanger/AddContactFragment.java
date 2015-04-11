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

import java.util.Map;
/**
 * Created by ClaudiusThaBeast on 4/10/15.
 */
public class AddContactFragment extends Fragment implements View.OnClickListener {

    private final static String STORETEXT="storetext.txt";
    public AddContactFragment() {
        //empty constructor
    }

    Button btn;
    TextView contacts;
    EditText name ;
    EditText phone ;
    EditText email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(
                R.layout.fragment_add, container, false);

        btn = (Button) view.findViewById(R.id.save_contact);
        btn.setOnClickListener(this);

        name = (EditText) view.findViewById(R.id.name_text);
        name.requestFocus();

        Context context = getActivity();
        SharedPreferences settings = context.getSharedPreferences(STORETEXT, Context.MODE_PRIVATE);

        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_add, container, false);
        return view;
    }


//    public void save(View view){
//
//        TextView contacts = (TextView) getActivity().findViewById(R.id.contacts_view);
////        Button save = (Button) getActivity().findViewById(R.id.save_contact);
//        EditText name = (EditText) getActivity().findViewById(R.id.name_text);
//        EditText phone = (EditText) getActivity().findViewById(R.id.phone_text);
//        EditText email = (EditText) getActivity().findViewById(R.id.email_text);
//
//        if (name.getText().toString() != ""){
//
//            if( phone.getText().toString() == "" && email.getText().toString() == "" ){
//                Toast.makeText(this.getActivity(), "Please add either phone # or email", Toast.LENGTH_SHORT).show();
//            }else{
//                contacts.setText(contacts.getText().toString()+"\n" + name.getText().toString() + "\n" + phone.getText().toString()+ "\n" + email.getText().toString() + "\n");
//            }
//        }else{
//            Toast.makeText(this.getActivity(), "Please add a contact before saving", Toast.LENGTH_SHORT).show();
//        }
//    }
//

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

        if (!TextUtils.isEmpty(name.getText().toString())){

            if(TextUtils.isEmpty(phone.getText().toString()) && TextUtils.isEmpty(email.getText().toString())){
                Toast.makeText(this.getActivity(), "Please add either phone # or email", Toast.LENGTH_SHORT).show();
            }else{
//                if(TextUtils.isEmpty(phone.getText().toString())){
//                    contacts.setText(contacts.getText().toString()+"\n" + name.getText().toString() + "\n" + email.getText().toString()+ "\n");
//                }else if(TextUtils.isEmpty(email.getText().toString())){
//                    contacts.setText(contacts.getText().toString()+"\n" + name.getText().toString() + "\n" + phone.getText().toString() + "\n");
//                }else{
//                    contacts.setText(contacts.getText().toString()+"\n" + name.getText().toString() + "\n" + phone.getText().toString()+ "\n" + email.getText().toString() + "\n");
//                }
                name.clearComposingText();
                phone.setText("");
                email.setText("");
                Toast.makeText(this.getActivity(), "Contact saved!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this.getActivity(), "Please add a contact before saving", Toast.LENGTH_SHORT).show();
        }

        saveContact();
//        Toast.makeText(context.getApplicationContext(), str, Toast.LENGTH_LONG).show();
 //       setContactOnScreen();
    }


    public void saveContact() {
        Context context = getActivity();
        SharedPreferences sharedPref = context.getSharedPreferences(STORETEXT, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();

        //int num = getActivity().findViewById(R.id.phone_text).toInt()

        String nameRedo =  getActivity().findViewById(R.id.name_text).toString();

        prefEditor.putInt(nameRedo , 0);

        prefEditor.commit();
    }

//    public static int getContact(Context context) {
//        SharedPreferences share = context.getSharedPreferences(STORETEXT, context.MODE_WORLD_READABLE);
//
//        EditText nameRedo = (EditText) getActivity().findViewById(R.id.name_text);
//        return share.getInt(nameRedo.toString(), 0);
//    }

    public void setContactOnScreen() {
        contacts = (TextView) getActivity().findViewById(R.id.contacts_view);

        Context context = getActivity();
        SharedPreferences share = context.getSharedPreferences(STORETEXT, context.MODE_WORLD_READABLE);
        EditText nameRedo = (EditText) getActivity().findViewById(R.id.name_text);
        int str = share.getInt(nameRedo.toString(), 0);

        //String key = findKey(share, str);

//        contacts.setText(share);
//        contacts.setText(nameRedo.toString() + "\n" + Integer.toString(str));

    }

//    public String findKey(SharedPreferences share, int value) {
//        for (Map.Entry<String, ?> entry: share.getAll()) {
//            if (valiue.equals(entry.getValue())) {
//                return entry.getKey();
//            }
//        }
//        return null;
//    }

//    @Override
//    protected void saveThis (View v) {
//        super.saveThis();
//        SharedPreferences storage = getSharedPreferences(STORETEXT, MODE_PRIVATE);
//        SharedPreferences.Editor storageEditor = storage.edit();
//
//        storageEditor.putString("ContactName", "John Doe");
//        storageEditor.commit();
//
//        String str = storage.getString("ContactName", "John Doe");
//        Toast.makeText(getContext(), str + "", Toast.LENGTH_LONG).show();
//    }

}
