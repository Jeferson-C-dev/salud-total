package com.example.saludtotal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class FirmaView extends View {

    public interface OnFirmaListener {
        void onFirma();
    }

    private final Path path = new Path();
    private final Paint paint = new Paint();
    private boolean firmado = false;
    private OnFirmaListener listener;

    public FirmaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.parseColor("#0A1B2E"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setOnFirmaListener(OnFirmaListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        getParent().requestDisallowInterceptTouchEvent(true);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                firmado = true;
                break;
            case MotionEvent.ACTION_UP:
                if (firmado && listener != null) {
                    listener.onFirma();
                }
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void limpiar() {
        path.reset();
        firmado = false;
        invalidate();
        if (listener != null) {
            listener.onFirma();
        }
    }

    public boolean estaFirmado() {
        return firmado;
    }

    public Bitmap obtenerBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawPath(path, paint);
        return bitmap;
    }
}