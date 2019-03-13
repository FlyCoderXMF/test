package com.example.fly.socketnew;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class SocketClient
{
    private Socket client,clientChat;
    private Context context,contextChat;
    private int port,portChat;
    private String site,siteChat;
    private Thread thread,threadChat;
    public static Handler mHandler;
    private boolean isClient=false,isClientChat=false;
    private PrintWriter out;
    private InputStream in,inChat;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String str;


    /**
     * @effect 开启线程建立连接开启客户端
     * */
    public void openClientThread(){
        thread=new Thread ( new Runnable ( )
        {
            @Override
            public void run()
            {

                try {
                    client=new Socket ( site,port );

                    Message messageSuc=new Message();
                    messageSuc.what=2;
                    mHandler.sendMessage(messageSuc);
//                    client.setSoTimeout ( 5000 );//设置超时时间
                    if (client!=null)
                    {
                        isClient=true;
                        forOut();
                        forIn ();
                    }else {
                        isClient=false;
                        Toast.makeText ( context,"网络连接失败", Toast.LENGTH_LONG ).show ();
                    }
                    Log.i ( "hahah","site="+site+" ,port="+port );
                }catch (UnknownHostException e) {
                    e.printStackTrace ();
                    Log.i ( "socket","6" );
                }catch (IOException e) {
                    e.printStackTrace ();
                    Log.i ( "socket","7" );
                }

            }
        } );
        thread.start ();
    }

    public void openClientThreadChat(){
        threadChat=new Thread ( new Runnable ( )
        {
            @Override
            public void run()
            {

                try {
                    /**
                     *  connect()步骤
                     * */
                    clientChat=new Socket ( siteChat,portChat );

//                    client.setSoTimeout ( 5000 );//设置超时时间
                    if (clientChat!=null)
                    {
                        isClientChat=true;
                        forOutChat();
                        forInChat ();
                    }else {
                        isClientChat=false;
                        Toast.makeText ( contextChat,"网络连接失败", Toast.LENGTH_LONG ).show ();
                    }
                    Log.i ( "hahah","site="+site+" ,port="+port );
                }catch (UnknownHostException e) {
                    e.printStackTrace ();
                    Log.i ( "socket","6" );
                }catch (IOException e) {
                    e.printStackTrace ();
                    Log.i ( "socket","7" );
                }

            }
        } );
        threadChat.start ();
    }
    /**
     * 调用时向类里传值
     * */
    public void clintValue(Context context, String site, int port)
    {
        this.context=context;
        this.site=site;
        this.port=port;
    }

    public void clintValueChat(Context context, String site, int port)
    {
        this.contextChat=context;
        this.siteChat=site;
        this.portChat=port;
    }
    /**
     * @effect 得到输出字符串
     * */
    public void forOut()
    {
        try {
            dataOutputStream=new DataOutputStream(client.getOutputStream ());
        }catch (IOException e){
            e.printStackTrace ();
            Log.i ( "socket","8" );
        }
    }

    public void forOutChat()
    {
        try {
            out=new PrintWriter ( clientChat.getOutputStream () );
        }catch (IOException e){
            e.printStackTrace ();
            Log.i ( "socket","8" );
        }
    }

    /**
     * @steps read();
     * @effect 得到输入字符串
     * */
    public void forIn(){

        while (isClient) {
            try {
                in=client.getInputStream ();
                dataInputStream=new DataInputStream(in);
                int temp=dataInputStream.readInt();
                Message message=new Message();
                message.arg1=temp;
                message.what=0;
                mHandler.sendMessage(message);

            } catch (IOException e) {}
        }
    }

    public void forInChat(){

        while (isClientChat) {
            try {
                /**得到的是16进制数，需要进行解析*/
                inChat=clientChat.getInputStream();
                byte[] bt = new byte[50];
                inChat.read ( bt );
                str=new String ( bt,"UTF-8" );
            } catch (IOException e) {}
            if (str!=null) {
                Message msg = new Message ( );
                msg.obj =str ;
                msg.what=1;
                mHandler.sendMessage ( msg );
            }
        }
    }
    /**
     * @steps write();
     * @effect 发送消息
     * */
    public void sendMsg(final String str)
    {
        Thread thread= new Thread ( new Runnable ( )
        {
            @Override
            public void run()
            {
                if (clientChat!=null)
                {
                    out.print ( str );
                    out.flush ();
                    Log.i ( "outtt",out+"" );
                }else
                {
                    isClientChat=false;
                    Toast.makeText ( context,"网络连接失败", Toast.LENGTH_LONG ).show ();
                }
            }
        } );
        thread.start();
    }



//向服务端发送坐标x与y
    public void sendMsgInt(final int xy)
    {
        Thread thread= new Thread ( new Runnable ( )
        {
            @Override
            public void run()
            {
                if (client!=null)
                {
                    try {
                        dataOutputStream.writeInt(xy);
                        dataOutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i ( "outtt",out+"" );
                }else
                {
                    isClient=false;
                    Toast.makeText ( context,"网络连接失败", Toast.LENGTH_LONG ).show ();
                }
            }
        } );
        thread.start();
        //避免位置x和y的发送出现乱序，使x先发先到，y后发后到
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
