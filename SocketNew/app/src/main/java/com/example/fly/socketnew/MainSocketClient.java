package com.example.fly.socketnew;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainSocketClient extends AppCompatActivity
{
    private static SocketClient client,clientChat;
    private TextView txt;
    private EditText edit;
    private Button btn;
    private Button server_OK;
    private EditText edit_ip;
    private LinearLayout mLinearLayout;

    private String ip="";

    private WuziqiPanel wuziqiPanel;
    private MediaPlayer mediaPlayer;
    private boolean openMusic=true;
    private Point p;
    private int x,y;
    private int n=0;
    private static boolean withdrawValid=false;
    private static Context sContext;
    private boolean connectValid=false;

    //从WuziqiPanel类的onTouchEvent方法中接受落点位置（处理后的触电位置），再调用SocketClient实例发送方法，将位置信息发送给服务端
    public static Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    client.sendMsgInt(msg.arg1);
                    client.sendMsgInt(msg.arg2);
                    withdrawValid=true;
                    break;
                case 1:
                    Toast.makeText(MainSocketClient.sContext,"请先连接",Toast.LENGTH_LONG).show();
                    break;
                    default:
                        break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );
        sContext=getApplicationContext();

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        wuziqiPanel=(WuziqiPanel)findViewById(R.id.id_wuziqi);
        wuziqiPanel.setmIsWhite(false);
        wuziqiPanel.setmIsRivalDone(false);
        mediaPlayer=MediaPlayer.create(this,R.raw.pdd);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });

        txt=(TextView)findViewById ( R.id.textView );
        edit=(EditText)findViewById ( R.id.edit );
        btn=(Button)findViewById ( R.id.btn );

        edit_ip=(EditText)findViewById ( R.id.client_ip );
        server_OK=(Button)findViewById ( R.id.server_OK );
        mLinearLayout=(LinearLayout)findViewById ( R.id.lin_1 ) ;

        client=new SocketClient ();
        clientChat=new SocketClient();

        server_OK.setOnClickListener ( new View.OnClickListener ( )
        {
            @Override
            public void onClick(View v)
            {
                mLinearLayout.setVisibility ( View.GONE );
                try {
                    ip=edit_ip.getText ().toString ();

                    //服务端的IP地址和端口号
                    client.clintValue (MainSocketClient.this,ip ,8000);
                    clientChat.clintValueChat(MainSocketClient.this,ip,8100);

                    //开启客户端接收消息线程
                    client.openClientThread ();
                    clientChat.openClientThreadChat();


                }catch (Exception e){
                    Toast.makeText ( MainSocketClient.this,"请检查ip及地址", Toast.LENGTH_SHORT ).show ();
                    mLinearLayout.setVisibility ( View.VISIBLE );
                    e.printStackTrace ();
                }

            }
        } );


        /**
         * 发送消息
         * */
        btn.setOnClickListener ( new View.OnClickListener ( )
        {
            @Override
            public void onClick(View v)
            {
                if(connectValid)
                    clientChat.sendMsg ( edit.getText ().toString () );
                else Toast.makeText(MainSocketClient.this,"请先连接",Toast.LENGTH_LONG).show();
            }
        } );
        /**
         *  接受消息
         *
         **/

        SocketClient.mHandler=new Handler (  ){
            @Override
            public void handleMessage(Message msg)
            {
                switch(msg.what){
                    case 0:
                        if(msg.arg1==11)              //再来一局
                            wuziqiPanel.start();
                        else if(msg.arg1==12){       //处理对方悔棋请求
                            Intent intent=new Intent(MainSocketClient.this,Withdraw.class);
                            startActivityForResult(intent,1);
                        }else if(msg.arg1==13){      //对方同意悔棋
                            wuziqiPanel.withdrawOwn();
                            withdrawValid=false;
                        }else if(msg.arg1==14){      //对方拒绝悔棋
                            Toast.makeText(MainSocketClient.this,"对方拒绝悔棋",Toast.LENGTH_LONG).show();
                            Log.d("MainSocketClient","14");
                        }
                        else {                        //显示对方落子
                            if(n==0){
                                x=msg.arg1;
                                n+=1;
                                String strX=String.valueOf(x);
                                Log.d("n1",strX);
                            }else if(n==1){
                                y=msg.arg1;
                                String strY=String.valueOf(y);
                                Log.d("n2",strY);
                                p=new Point(x,y);
                                wuziqiPanel.drawRival(p);
                                n=0;
                                withdrawValid=false;
                            }
                        }
                        break;
                    case 1:                          //聊天
                        txt.setText ( msg.obj.toString ());
                        break;
                    case 2:
                        Toast.makeText(MainSocketClient.this,"网络连接成功，请开始游戏",Toast.LENGTH_LONG).show();
                        connectValid=true;
                        wuziqiPanel.mIsConnect=true;
                        break;
                        default:
                            break;
                }
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    wuziqiPanel.withdrawRival();
                    client.sendMsgInt(13);
                }else
                    client.sendMsgInt(14);
                break;
                default:
                    break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.again:
                wuziqiPanel.start();
                client.sendMsgInt(11);
                break;
            case R.id.music:
                if(openMusic){
                    mediaPlayer.pause();
                    openMusic=!openMusic;
                }
                else {
                    mediaPlayer.start();
                    openMusic=!openMusic;
                }
                break;
            case R.id.withdraw:
                if(withdrawValid)
                    client.sendMsgInt(12);
                else Toast.makeText(MainSocketClient.this,"您还没有落子，轮到您下了",Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
        return true;
    }
}
