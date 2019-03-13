package com.example.fly.socketnew;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WuziqiPanel extends View {

    private int mPanelWidth;          //棋盘宽度
    private float mLineHight;         //棋盘格子的行高(声明为int会造成由于不能整除而造成的误差较大)
    private int MAX_LINE=10;           //棋盘最大行列数(其实就是棋盘横竖线的个数)
    private int MAX_COUNT_IN_LINE=5;  //最大连棋，即五子棋中的“五”

    private Paint mPaint=new Paint();   //定义画笔绘制棋盘格子
    private Bitmap mWhitePiece;         //定义黑白棋子Bitmap
    private Bitmap mBlackPiece;

    private float ratioPieceOfLineHight=3*1.0f/4;      //棋子的缩放比例(行高的3/4)

    private boolean mIsWhite;          //区别服务端与客户端
    private ArrayList<Point> mWhiteArray=new ArrayList<>();    //存储黑白棋子的坐标
    private ArrayList<Point> mBlackArray=new ArrayList<>();

    private boolean mIsGameOver;       //游戏是否结束
    private boolean mIsWhiteWinner;    //确定赢家
    private boolean mIsRivalDone;      //确定对方是否下了
    public boolean mIsConnect=false;   //确定是否连接成功

    private MediaPlayer mediaPlayer;     //落子音效

    public WuziqiPanel(Context context, AttributeSet attrs){
        super(context,attrs);
        //setBackgroundColor(0x44ff0000);
        init(context);
    }

    private void init(Context context) {
        mPaint.setColor(0x88000000);    //初始化画笔颜色
        mPaint.setAntiAlias(true);     //设置抗锯齿
        mPaint.setDither(true);         //设置防抖动
        mPaint.setStyle(Paint.Style.STROKE);      //设置为空心(画线)

        mWhitePiece=BitmapFactory.decodeResource(getResources(),R.drawable.stone_w2);    //初始化棋子
        mBlackPiece=BitmapFactory.decodeResource(getResources(),R.drawable.stone_b1);

        mediaPlayer=MediaPlayer.create(context,R.raw.chess);     //初始化音效
    }

    public void setmIsWhite(boolean isWhite){
        mIsWhite=isWhite;
    }
    public void setmIsRivalDone(boolean isRivalDone){
        mIsRivalDone=isRivalDone;
    }

    protected void finalize() throws Throwable {
        mediaPlayer.release();
        super.finalize();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize=MeasureSpec.getSize(widthMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int hightSize=MeasureSpec.getSize(heightMeasureSpec);
        int hightMode=MeasureSpec.getMode(heightMeasureSpec);

        int width=Math.min(widthSize,hightSize);
        if(widthMode==MeasureSpec.UNSPECIFIED)
            width=hightSize;
        else if(hightMode==MeasureSpec.UNSPECIFIED)
            width=widthSize;

        setMeasuredDimension(width,width);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mPanelWidth=w;
        mLineHight=mPanelWidth*1.0f/MAX_LINE;

        int pieceWidth=(int)(mLineHight*ratioPieceOfLineHight);
        mWhitePiece=Bitmap.createScaledBitmap(mWhitePiece,pieceWidth,pieceWidth,false);
        mBlackPiece=Bitmap.createScaledBitmap(mBlackPiece,pieceWidth,pieceWidth,false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!mIsConnect){
            Message message=new Message();
            message.what=1;
            if(mIsWhite)
                MainSocketServer.handler.sendMessage(message);
            else MainSocketClient.handler.sendMessage(message);
            return false;
        }
        if(mIsGameOver) return false;
        if(!mIsRivalDone) return false;

        int action = event.getAction();
        if(action==MotionEvent.ACTION_UP)        //手指抬起后处理
        {   //拦截事件自己来处理
            int x=(int)event.getX();
            int y=(int)event.getY();
            Point p=getValidPoint(x,y);
            if(mWhiteArray.contains(p)||mBlackArray.contains(p))         //首先判断所点击的位置是不是已经有棋子
                return false;

            //将位置信息处理后，即落子位置包装在Message实例中利用handler机制发送给MainSocketServer或MainSocketClient
            Message msg=new Message();
            msg.what=0;
            msg.arg1=(int)(x/mLineHight);
            msg.arg2=(int)(y/mLineHight);
            if(mIsWhite)
                MainSocketServer.handler.sendMessage(msg);
            else MainSocketClient.handler.sendMessage(msg);

            if(mIsWhite)
                mWhiteArray.add(p);
            else mBlackArray.add(p);
            invalidate();      //调用重绘
            mIsRivalDone=!mIsRivalDone;
            mediaPlayer.start();
        }
        return true;
    }

    private Point getValidPoint(int x, int y) {
        return new Point((int)(x/mLineHight),(int)(y/mLineHight));
    }



    //绘制对手的棋子
    public void drawRival(Point q){
        if(mIsWhite)
            mBlackArray.add(q);
        else mWhiteArray.add(q);
        mIsRivalDone=!mIsRivalDone;
        invalidate();
    }

    //悔棋
    public void withdrawRival() {
        if (mIsWhite)
            mBlackArray.remove(mBlackArray.size() - 1);
        else
            mWhiteArray.remove(mWhiteArray.size() - 1);
        mIsRivalDone=!mIsRivalDone;
        invalidate();
    }

    public void withdrawOwn(){
        if(mIsWhite)
            mWhiteArray.remove(mWhiteArray.size() - 1);
        else mBlackArray.remove(mBlackArray.size() - 1);
        mIsRivalDone=!mIsRivalDone;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPieces(canvas);
        checkGameOver();
    }

    private void checkGameOver() {
        boolean whiteWin=checkFiveInLine(mWhiteArray);
        boolean blackWin=checkFiveInLine(mBlackArray);
        if(whiteWin||blackWin) {
            mIsGameOver = true;
            mIsWhiteWinner=whiteWin;
            String text=mIsWhiteWinner ? "白棋胜利":"黑棋胜利";
            Toast.makeText(getContext(),text,Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkFiveInLine(List<Point> points) {
        for(Point p : points)
        {
            int x=p.x;
            int y=p.y;
            boolean win=checkHorizontal(x,y,points);
            if(win) return true;
            win=checkVertical(x,y,points);
            if(win) return true;
            win=checkLeftDiagonal(x,y,points);
            if(win) return true;
            win=checkRightDiagonal(x,y,points);
            if(win) return true;
        }
        return false;
    }

    private boolean checkHorizontal(int x, int y, List<Point> points) {
        int count=1;
        for(int i=1;i<MAX_COUNT_IN_LINE;i++)
        {
            if(points.contains(new Point(x-i,y)))
                count++;
            else break;
        }
        if(count==MAX_COUNT_IN_LINE)
            return true;
        for(int i=1;i<MAX_COUNT_IN_LINE;i++)
        {
            if(points.contains(new Point(x+i,y)))
                count++;
            else break;
        }
        if(count==MAX_COUNT_IN_LINE)
            return true;

        return false;
    }

    private boolean checkVertical(int x, int y, List<Point> points) {
        int count=1;
        for(int i=1;i<MAX_COUNT_IN_LINE;i++)
        {
            if(points.contains(new Point(x,y-i)))
                count++;
            else break;
        }
        if(count==MAX_COUNT_IN_LINE)
            return true;

        for(int i=1;i<MAX_COUNT_IN_LINE;i++)
        {
            if(points.contains(new Point(x,y+i)))
                count++;
            else break;
        }
        if(count==MAX_COUNT_IN_LINE)
            return true;

        return false;
    }

    private boolean checkLeftDiagonal(int x, int y, List<Point> points) {
        int count=1;
        for(int i=1;i<MAX_COUNT_IN_LINE;i++)
        {
            if(points.contains(new Point(x-i,y+i)))
                count++;
            else break;
        }
        if(count==MAX_COUNT_IN_LINE)
            return true;
        for(int i=1;i<MAX_COUNT_IN_LINE;i++)
        {
            if(points.contains(new Point(x+i,y-i)))
                count++;
            else break;
        }
        if(count==MAX_COUNT_IN_LINE)
            return true;
        return false;
    }

    private boolean checkRightDiagonal(int x, int y, List<Point> points) {
        int count=1;
        for(int i=1;i<MAX_COUNT_IN_LINE;i++)
        {
            if(points.contains(new Point(x-i,y-i)))
                count++;
            else break;
        }
        if(count==MAX_COUNT_IN_LINE)
            return true;
        for(int i=1;i<MAX_COUNT_IN_LINE;i++)
        {
            if(points.contains(new Point(x+i,y+i)))
                count++;
            else break;
        }
        if(count==MAX_COUNT_IN_LINE)
            return true;
        return false;
    }

    private void drawPieces(Canvas canvas) {
        for(int i=0,n=mWhiteArray.size();i<n;i++)     //绘制白棋子
        {
            Point whitePoint=mWhiteArray.get(i);
            //棋子之间的间隔为1/4行高
            canvas.drawBitmap(mWhitePiece,
                    (whitePoint.x+(1-ratioPieceOfLineHight)/2)*mLineHight,
                    (whitePoint.y+(1-ratioPieceOfLineHight)/2)*mLineHight,null);
        }
        for(int i=0,n=mBlackArray.size();i<n;i++)    //绘制黑棋子
        {
            Point blackPoint=mBlackArray.get(i);
            //棋子之间的间隔为1/4行高,棋子距离左右边框的距离为1/8行高
            canvas.drawBitmap(mBlackPiece,
                    (blackPoint.x+(1-ratioPieceOfLineHight)/2)*mLineHight,
                    (blackPoint.y+(1-ratioPieceOfLineHight)/2)*mLineHight,null);
        }
    }

    private void drawBoard(Canvas canvas) {
        int w=mPanelWidth;
        float lineHight=mLineHight;

        for(int i=0;i<MAX_LINE;i++){
            int startX=(int)lineHight/2;
            int endX=(int)(w-lineHight/2);
            int y=(int)((0.5+i)*lineHight);
            canvas.drawLine(startX,y,endX,y,mPaint);    //画横线
            canvas.drawLine(y,startX,y,endX,mPaint);    //画竖线
        }
    }

    public void start()
    {
        mWhiteArray.clear();
        mBlackArray.clear();
        mIsGameOver=false;
        mIsWhiteWinner=false;
        invalidate();
    }

    private static final String INSTANCE="instance";
    private static final String INSTANCE_GAME_OVER="instance_game_over";
    private static final String INSTANCE_WHITE_ARRAY="instance_white_array";
    private static final String INSTANCE_BLACK_ARRAY="instance_black_array";

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle=new Bundle();
        bundle.putParcelable(INSTANCE,super.onSaveInstanceState());
        bundle.putBoolean(INSTANCE_GAME_OVER,mIsGameOver);
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY,mWhiteArray);
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY,mBlackArray);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(state instanceof Bundle)
        {
            Bundle bundle=(Bundle)state;
            mIsGameOver=bundle.getBoolean(INSTANCE_GAME_OVER);
            mWhiteArray=bundle.getParcelableArrayList(INSTANCE_WHITE_ARRAY);
            mBlackArray=bundle.getParcelableArrayList(INSTANCE_BLACK_ARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}

