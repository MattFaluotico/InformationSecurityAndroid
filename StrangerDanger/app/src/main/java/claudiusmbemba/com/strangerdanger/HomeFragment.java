package claudiusmbemba.com.strangerdanger;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by ClaudiusThaBeast on 4/10/15.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    public HomeFragment() {
        //empty constructor
    }

    Button notify;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        notify = (Button) view.findViewById(R.id.danger_button);
        notify.setOnClickListener(this);

        notify.requestFocus();

        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_home, container, false);
        return view;
    }

    @Override
    public void onClick(View v) {
        //api to notify ICEs
        Toast.makeText(this.getActivity(), "ICEs will be notified!", Toast.LENGTH_LONG).show();
    }
}
