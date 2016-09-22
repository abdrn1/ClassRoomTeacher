package com.abd.classroom1;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.esotericsoftware.kryonet.Client;

import org.apache.commons.io.FilenameUtils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CmImageViewerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CmImageViewerFragment extends Fragment {

    private ImageView imgView;
    private String imagePath;
    String fileName;
    private int imgWidth = 100;
    private int imgHeight = 100;
    Client client;
    UserLogin iam;
    private GestureDetectorCompat mDetector;


    class MyGestureListener implements GestureDetector.OnGestureListener {
        private static final String DEBUG_TAG = "GEST";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG, "onDown: " + event.toString());
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.d(DEBUG_TAG, "onScroll: " + e1.toString()+e2.toString());
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            PopupMenu popup = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                popup = new PopupMenu(getActivity(), imgView, Gravity.CENTER);
            }
            popup.getMenuInflater().inflate(R.menu.monitor_item_display, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.forward_Item) {

                    } else if (item.getItemId() == R.id.show_on_monitor) {
                        showOnBoard(0);
                    } else if (item.getItemId() == R.id.zoomIN) {
                        client.sendTCP(new CommandsMessages(1.3));
                    } else if (item.getItemId() == R.id.zoomOUT) {
                        client.sendTCP(new CommandsMessages(0.8));
                    } else if (item.getItemId() == R.id.rotateIMG) {
                        CommandsMessages comM = new CommandsMessages();
                        comM.setCommnadType(1);
                        client.sendTCP(comM);
                    }
                    return true;
                }
            });

            popup.show();
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
            CommandsMessages cm = new CommandsMessages();
            int scrollOnY = (int) Math.abs(event1.getY() - event2.getY());
            int scrollOnX = (int) Math.abs(event1.getX() - event2.getX());
            if (scrollOnY > scrollOnX) {
                if (event1.getY() < event2.getY()) {
                    // scrol down

                    cm.setCommnadType(3);
                    client.sendTCP(cm);
                } else {
                    cm.setCommnadType(4);
                    client.sendTCP(cm);
                }
            } else {
                if (event1.getX() < event2.getX()) {
                    // scrol down

                    cm.setCommnadType(5);
                    client.sendTCP(cm);
                } else {
                    cm.setCommnadType(6);
                    client.sendTCP(cm);
                }

            }
            return true;
        }
    }


    private OnFragmentInteractionListener mListener;

    public CmImageViewerFragment() {
        // Required empty public constructor
    }


    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cm_image_viewer, container, false);


    }

    public void showImage(String path) {
        Bitmap tempImg = ScalDownImage.decodeSampledBitmapFromResource(path, 100, 100);
        imgView.setImageBitmap(tempImg);
    }

    public void showImage(Bitmap img) {
        imgView.setImageBitmap(img);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        imgView = (ImageView) getActivity().findViewById(R.id.img_curr_image);
        ImageButton btnZoomin = (ImageButton) getActivity().findViewById(R.id.btn_zoomin);
        ImageButton btnZoomout = (ImageButton) getActivity().findViewById(R.id.btn_zoomout);
        ImageButton btnRotate = (ImageButton) getActivity().findViewById(R.id.btn_rotate);
        ImageButton showOnBoard = (ImageButton) getActivity().findViewById(R.id.show_on_borad);
        GeneralUtil.buttonEffect(btnZoomin);
        GeneralUtil.buttonEffect(btnZoomout);
        GeneralUtil.buttonEffect(btnRotate);
        GeneralUtil.buttonEffect(showOnBoard);
        this.mDetector = new GestureDetectorCompat(getActivity(), new MyGestureListener());

        showOnBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOnBoard(0);
            }
        });

        imgView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                return true;
            }
        });


        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bitmap tm = ScalDownImage.decodeSampledBitmapFromResource(imagePath, 100, 100);
                // imgView.setImageBitmap(tm);
                CommandsMessages comM = new CommandsMessages();
                comM.setCommnadType(1);
                client.sendTCP(comM);
            }
        });

        btnZoomin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    imgHeight += 40;
                    imgWidth += 40;

                    Bitmap tm = ScalDownImage.decodeSampledBitmapFromResource(imagePath, imgWidth, imgHeight);
                    imgView.setImageBitmap(tm);
                } catch (Exception ex) {

                }
            }
        });


        btnZoomout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    client.sendTCP(new CommandsMessages(1.3));
                } catch (Exception ex) {

                }
            }

        });

        showImage(imagePath);
    }

    private void showOnBoard(int itemIndex) {
        // ChatMessageModel chatMessageModel = this.l1.get(itemIndex);
        ShowOnBoardMessage sob = new ShowOnBoardMessage();
        sob.setSenderID(iam.getUserID());
        //if(chatMessageModel.getMessageType().equals("IMG")){
        sob.setFileName(FilenameUtils.getName(imagePath));
        sob.setMessageType("IMG");
        //}
        client.sendTCP(sob);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
