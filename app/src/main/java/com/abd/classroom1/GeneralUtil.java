package com.abd.classroom1;

import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Abd on 3/17/2016.
 */
public class GeneralUtil {
    public static void buttonEffect(final View button) {
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(button.getResources().getColor(R.color.press_button), PorterDuff.Mode.SRC);
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

    public static boolean checkIfFileIsImage(String fileName) {

        String ext = null;
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < (fileName.length() - 1)) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        if (ext == null)
            return false;
        else if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png") && !ext.equals("gif"))
            return false;
        else
            return true;
    }


}
