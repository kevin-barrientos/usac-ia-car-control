package gt.edu.usac.ingenieria.ia.smartcarcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class MazeCanvas extends Canvas {

    public static final int UP = 1;
    public static final int LEFT = 2;
    public static final int DOWN = 3;
    public static final int RIGHT = 4;

    private Paint mLinePaint;
    private List<Coordanate> mPoints;
    private int x0; //punto inicial en x
    private int y0; //punto inicial en y
    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int mLineDpis;

    private Bitmap mBitmap;

    public MazeCanvas() {
        super();
        init();
    }

    private void init() {
        mPoints = new ArrayList<>();
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.BLACK);
        mPoints.add(new Coordanate(0, 0));
    }

    public Bitmap addStep(int direction) {
        Coordanate lastPoint = mPoints.get(mPoints.size() - 1);
        int newX = lastPoint.getX(), newY = lastPoint.getY();
        switch (direction) {
            case UP:
                if(--newY < minY){ minY = newY; y0 += mLineDpis / 2; }
                break;
            case LEFT:
                if(--newX < minX){ minX = newX; x0 += mLineDpis / 2; }
                break;
            case DOWN:
                if(++newY > maxY){ maxY = newY; y0 -= mLineDpis / 2; }
                break;
            case RIGHT:
                if(++newX > maxX){ maxX = newX; x0 -= mLineDpis / 2; }
                break;
        }

        Coordanate newPoint = new Coordanate(newX, newY);
        mPoints.add(newPoint);

//        if ((stepsRight * mLineDpis) > getWidth() || (Math.max(stepsUp, Math.abs(stepsDown)) * mLineDpis) > getHeight()) {
//            mLineDpis /= 2;
//        }

        setBitmap(Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888));
        redraw();
        return mBitmap;

    }

    private void redraw(){
        int size = mPoints.size();
        for (int i = 1; i < size; i++) {
            drawLine(x0 + mPoints.get(i - 1).getX() * mLineDpis, y0 + mPoints.get(i - 1).getY() * mLineDpis,
                    x0 + mPoints.get(i).getX() * mLineDpis, y0 + mPoints.get(i).getY() * mLineDpis, mLinePaint);
        }
    }

    public Bitmap getmBitmap(){
        return mBitmap;
    }

    @Override
    public void setBitmap(Bitmap bitmap) {
        super.setBitmap(bitmap);
        mBitmap = bitmap;
        mLineDpis = Math.min(bitmap.getWidth(), bitmap.getHeight()) / 8;
        if(x0 == 0 || y0 == 0){
            x0 = bitmap.getWidth()/2;
            y0 = bitmap.getHeight()/2;
        }
    }

    public void erase(){
        init();
        drawColor(Color.WHITE);
    }
}
