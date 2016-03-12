package com.abd.classroom1;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.esotericsoftware.kryonet.Client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ActiveUsersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ActiveUsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActiveUsersFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ListView listview;
    private List<ClientModel> l1;
    private ClientListAdapter clientListAdapter;
    private Client client;
    private UserLogin iam;

    private static int FILE_SELECT_CODE = 2;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ActiveUsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActiveUsersFragment newInstance(String param1, String param2) {
        ActiveUsersFragment fragment = new ActiveUsersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ActiveUsersFragment() {
        // Required empty public constructor
    }

    public void setClient(Client cl) {
        this.client = cl;
    }

    public void setUserlogin(UserLogin ul) {
        this.iam = ul;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listview = (ListView) getActivity().findViewById(R.id.clients_listview);
        l1 = new ArrayList<ClientModel>();
        clientListAdapter = new ClientListAdapter(getActivity(), l1);
        l1.add(new ClientModel("3", "Hassan", R.drawable.a1));
        l1.add(new ClientModel("4", "Mohammed", R.drawable.a2));
        l1.add(new ClientModel("5", "ZAKI", R.drawable.a3));
        listview.setAdapter(clientListAdapter);
        Button sendfile = (Button) getActivity().findViewById(R.id.btnsendfile);
        Button btnsend = (Button) getActivity().findViewById(R.id.btnSend);
        final EditText inputMsg = (EditText) getActivity().findViewById(R.id.inputMsg);
        // Give Button Animation effect On press Button
        buttonEffect((View) sendfile);
        buttonEffect((View) btnsend);

        sendfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent, FILE_SELECT_CODE);
                Toast.makeText(getActivity(),
                        "INfo Message", Toast.LENGTH_LONG).show();
            }
        });


        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempMsg = inputMsg.getText().toString();
                //l1.add(new ClientModel("5", "ZAKI", R.drawable.a3));
                //clientListAdapter.notifyDataSetChanged();
                if (!(tempMsg.equals(""))) {
                    sendTextMessage(tempMsg);
                    inputMsg.setText("");
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ///  here get notified that file open completed

        if (requestCode == FILE_SELECT_CODE && resultCode == Activity.RESULT_OK
                && null != data) {
            final Uri uri = data.getData();
            String path = getRealPathFromURI(uri);
            Log.d("INFO", path);
            //  path = uri.getPath();
            Log.d("INFO", path);
            Toast.makeText(getActivity(),
                    "File Loadded Copletely", Toast.LENGTH_SHORT).show();

            Log.d("INFO", iam.getUserName());
            Log.d("INFO", iam.getUserType());

            FileChunkMessageV2 fblock = new FileChunkMessageV2();
            fblock.setSenderName(iam.getUserName());
            fblock.setSenderID(iam.getUserID());
            //  client.sendTCP(fblock);
            try {
                Log.d("INFO", "READ AND SEND FILE HERE");
                SendUtil.readAndSendFile(path, client, iam,getSelectedRecivers());

            } catch (IOException e) {
                Toast.makeText(getActivity(),
                        "Error While Sending file", Toast.LENGTH_SHORT).show();
                e.printStackTrace();


            }
        }

    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void sendTextMessage(String txtmsg) {
        String[] recivers = getSelectedRecivers();
        TextMeesage currTm = new TextMeesage();
        currTm.setSenderID(iam.getUserID());
        currTm.setSenderName(iam.getUserName());
        currTm.setMessageType("TXT");
        currTm.setTextMessage(txtmsg);
        currTm.setRecivers(recivers);
        client.sendTCP(currTm);

    }
    private String[] getSelectedRecivers(){
        String[] recivers = null;
        List<String> selectedClients = new ArrayList<String>();
        for (ClientModel temp : l1) {
            if (temp.isClientSelected()) {
                selectedClients.add(temp.getClientID());
            }
        }
        if (!(selectedClients.isEmpty())) {
            recivers = new String[selectedClients.size()];
            selectedClients.toArray(recivers);
        }

        return  recivers;

    }

    public void addNewClient(UserLogin ul) {
        ClientModel t = new ClientModel(ul.getUserID(), ul.getUserName(),R.drawable.a5);
        System.out.println();
        l1.add(t);
        clientListAdapter.notifyDataSetChanged();
    }

    public void buttonEffect(View button) {
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(getResources().getColor(R.color.test), PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_active_users, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
         //   mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
