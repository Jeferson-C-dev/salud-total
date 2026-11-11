package com.example.saludtotal;

import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onLongItemClick(View view, int position);
    }

    private OnItemClickListener listener;
    private GestureDetector gestureDetector;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView,
                                     OnItemClickListener listener) {
        this.listener = listener;
        gestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }
                    @Override
                    public void onLongPress(MotionEvent e) {
                        View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (child != null && listener != null) {
                            listener.onLongItemClick(child,
                                    recyclerView.getChildAdapterPosition(child));
                        }
                    }
                });
    }

    // Devuelve true si el toque cayó sobre un botón visible dentro de la tarjeta
    private boolean tocaUnBoton(View view, float x, float y) {
        if (view instanceof Button && view.getVisibility() == View.VISIBLE) {
            Rect r = new Rect();
            view.getGlobalVisibleRect(r);
            return r.contains((int) x, (int) y);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                if (tocaUnBoton(group.getChildAt(i), x, y)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());
        if (child != null && listener != null && gestureDetector.onTouchEvent(e)) {
            // Si tocó un botón, dejar que el botón haga su trabajo
            if (tocaUnBoton(child, e.getRawX(), e.getRawY())) {
                return false;
            }
            listener.onItemClick(child, rv.getChildAdapterPosition(child));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
}