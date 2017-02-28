package com.abd.classroom1;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.esotericsoftware.kryonet.Client;


/**
 * A simple {@link Fragment} subclass.
 */
public class SingleMonitorFragment extends Fragment {

    Bitmap bm;
    Client client;
    private GestureDetectorCompat mDetector;
    ImageView im;


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
    public SingleMonitorFragment() {
        // Required empty public constructor
    }

    public void setScreenshot(Bitmap bm){
        this.bm = bm;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_single_monitor, container, false);
        if(bm != null){
            ImageView im = (ImageView) view.findViewById(R.id.screenView);
            im.setImageBitmap(bm);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mDetector = new GestureDetectorCompat(getActivity(), new MyGestureListener());
        ImageView im = (ImageView) getActivity().findViewById(R.id.screenView);
        im.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                return true;
            }
        });
    }
}
