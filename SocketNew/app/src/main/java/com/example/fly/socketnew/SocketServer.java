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
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by kys-29 on 2016/9/21.
 */

/*
MainActivity代码：
    SocketServer server=new SocketServer ( port );
    server.beginListen ();

* */
public class SocketServer
{
    private ServerSocket server,serverChat;
    private Socket socket,socketChat;
    private InputStream in,inChat;
    private DataInputStream dataInputStream;
    private String str=null;
    public static Handler ServerHandler;
    private Context context;

    /**
     * @steps bind();绑定端口号
     * @effect 初始化服务端
     * @param port 端口号
     * */
    public SocketServer(int port,int mark){
        if(mark==0){
            try {
                server= new ServerSocket ( port );
            }catch (IOException e){
                e.printStackTrace ();
            }
        }else if(mark==1){
            try {
                serverChat= new ServerSocket ( port );
            }catch (IOException e){
                e.printStackTrace ();
            }
        }
    }

    /**
     * @steps listen();
     * @effect socket监听数据
     * */

    public void beginListen()
    {
        new Thread ( new Runnable ( )
        {
            @Override
            public void run()
            {
                try {
                    /**
                     * accept();
                     * 接受请求
                     * */
                    socket=server.accept ();

                    Message messageSuc=new Message();
                    messageSuc.what=2;
                    ServerHandler.sendMessage(messageSuc);
                    try {
                        /**得到输入流*/
                        in =socket.getInputStream();
                        dataInputStream=new DataInputStream(in);
                        /**
                         * 实现数据循环接收
                         * */
                        while (!socket.isClosed())
                        {
                            int temp=dataInputStream.readInt();
                            String str=String.valueOf(temp);
                            Log.d("dataInputStream",str);
                            Message message=new Message();
                            message.arg1=temp;
                            message.what=0;
                            ServerHandler.sendMessage(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace ( );
                        socket.isClosed ();
                    }
                } catch (IOException e) {
                    e.printStackTrace ( );
                    socket.isClosed ();
                }
            }
        } ).start ();
    }

    public void beginListenChat()
    {
        new Thread ( new Runnable ( )
        {
            @Override
            public void run()
            {
                try {
                    /**
                     * accept();
                     * 接受请求
                     * */
                    socketChat=serverChat.accept ();
                    try {
                        /**得到输入流*/
                        inChat =socketChat.getInputStream();
                        /**
                         * 实现数据循环接收
                         * */
                        while (!socketChat.isClosed())
                        {
                            byte[] bt=new byte[50];
                            inChat.read ( bt );
                            str=new String ( bt,"UTF-8" );                  //编码方式  解决收到数据乱码
                            if (str!=null&&str!="exit")
                            {
                                returnMessage ( str );
                            }else if (str==null||str=="exit"){
                                break;                                     //跳出循环结束socket数据接收
                            }
                            System.out.println(str);
                        }
                    } catch (IOException e) {
                        e.printStackTrace ( );
                        socketChat.isClosed ();
                    }
                } catch (IOException e) {
                    e.printStackTrace ( );
                    socketChat.isClosed ();
                }
            }
        } ).start ();
    }
    /**
     * @steps write();
     * @effect socket服务端发送信息
     * */
    public void sendMessage(final String chat)
    {
        Thread thread=new Thread ( new Runnable ( )
        {
            @Override
            public void run()
            {
                try {
                    PrintWriter out=new PrintWriter ( socketChat.getOutputStream () );
                    out.print ( chat );
                    out.flush ();
                } catch (IOException e) {
                    e.printStackTrace ( );
                }
            }
        } );
        thread.start ();
    }

    public void sendMessageInt(final int xy)
    {
        Thread thread=new Thread ( new Runnable ( )
        {
            @Override
            public void run()
            {
                try {
                    DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());
                    dataOutputStream.writeInt(xy);
                    dataOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace ( );
                }
            }
        } );
        thread.start ();
        //避免位置x和y的发送出现乱序，使x先发先到，y后发后到
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d("error","join");
        }
    }
    /**
     * @steps read();
     * @effect socket服务端得到返回数据并发送到主界面
     * */
    public void returnMessage(String chat){
        Message msg=new Message ();
        msg.obj=chat;
        msg.what=1;
        ServerHandler.sendMessage ( msg );
    }

}

