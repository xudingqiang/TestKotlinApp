package com.bella.testapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

public class NoMouseScrollRecyclerView extends RecyclerView {

    public NoMouseScrollRecyclerView(Context context) {
        super(context);
    }

    public NoMouseScrollRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {

        // 禁止鼠标滚轮
//        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
//
//            if (event.getAction() == MotionEvent.ACTION_SCROLL) {
//                return true;
//            }
//        }

        return super.dispatchGenericMotionEvent(event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {

        // 禁止触摸/鼠标拖动
        if (e.getToolType(0) == MotionEvent.TOOL_TYPE_MOUSE) {
            return false;
        }

        return super.onTouchEvent(e);
    }
}