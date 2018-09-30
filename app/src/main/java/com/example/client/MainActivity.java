package com.example.client;


import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;

import static android.os.SystemClock.sleep;


public class MainActivity extends AppCompatActivity {

//    byte Identifier = 0x7e;
//    short LocationMsgId = 0x0200;//位置信息ID
//    byte Phone[] = {0x01,(byte)0x86,0x55,0x03,0x72,0x59};//java里byte是有符号数，大于0x80的是负数
//    short FlowNum = 0x0000;//消息流水号
//    int AlarmSign = 0x00000000;//报警标志
//    int Status = 0x00000000;//状态
//    int Latitude = 0x00000000;//纬度
//    int Longitude = 0x00000000;//经度
//    short Hight = 0x0000;//高度
//    short Speed = 0x0000;//速度
//    short Direction = 0x0000;//方向
//    byte time[] = {0x00,0x00,0x00,0x00,0x00,0x00};//时间
//    byte ExLocationMsgId = 0x20;//保留的附加信息ID
//    byte ExLocationMsgLength = 0x00;//附加信息长度
//    byte ExMsg[] = {0x00};//附加信息
//    byte Check = 0x00;//校验码
//
//    String sIdentifier = Identifier + "";
//    String sLocationMsgId = LocationMsgId + "";
//    String sPhone = Phone + "";
//    String sFlowNum = FlowNum + "";
//    String sAlarmSign = AlarmSign + "";
//    String sStatus = Status + "";
//    String sLatitude = Latitude + "";
//    String sLongitude = Longitude + "";
//    String sHight = Hight + "";
//    String sSpeed = Speed + "";
//    String sDirection = Direction + "";
//    String stime = time + "";
//    String sExLocationMsgId = ExLocationMsgId + "";
//    String sExLocationMsgLength = ExLocationMsgLength + "";
//    String sExMsg = ExMsg + "";
//
//    //消息头和消息体
//    String sMsg = sLocationMsgId+sPhone+sFlowNum+sAlarmSign+sStatus+sLatitude+sLongitude+sHight
//            +sSpeed+sDirection+stime+sExLocationMsgId+sExLocationMsgLength+sExMsg;
//    byte[] Msg = sMsg.getBytes();


    byte[] LocationMsg = {0x7e,0x02,0x00,0x00,0x1f,0x01,(byte)0x86,0x55,0x03,0x72,0x59,0x00,0x00,
            //消息体------------------------------------------
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,//纬度
            0x00,0x00,0x00,0x00,//经度
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x01,
            0x7e,//附加信息
            //消息体-------------------------------------------
            0x00,0x7e,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00//预留的多余长度，为了放多出来的转义字符
    };

    byte[] RegisterMsg = {0x7e,0x01,0x00,0x00,0x1f,0x01,(byte)0x86,0x55,0x03,0x72,0x59,
            0x00,0x00,//流水号
            //消息体------------------------------------------
            0x00,0x22,//省域ID
            0x00,0x68,//市域ID
            0x00,0x00,0x00,0x00,0x00,//制造商ID
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,//终端型号
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,//终端ID
            0x00,//车牌颜色
            0x00,//车牌标识，string类型的
            //消息体-------------------------------------------
            0x00,0x7e,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00//预留的多余长度，为了放多出来的转义字符
    };

    byte[] HeartMsg = {0x7e,0x00,0x02,0x00,0x00,0x01,(byte)0x86,0x55,0x03,0x72,0x59,0x00,0x00,
            //消息体-为空-----------------------------------------

            //消息体-------------------------------------------
            0x00,0x7e,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00//预留的多余长度，为了放多出来的转义字符
    };
    // 主线程Handler
    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;

    // Socket变量
    private Socket socket;

    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;

    /**
     * 接收服务器消息 变量
     */
    // 输入流对象
    InputStream is;

    // 输入流读取器对象
    InputStreamReader isr;
    BufferedReader br;

    // 接收服务器发送过来的消息
    String response;


    /**
     * 发送消息到服务器 变量
     */
    // 输出流对象
    OutputStream outputStream;

    /**
     * 按钮 变量
     */

    // 连接 断开连接 发送数据到服务器 的按钮变量
    private Button btnConnect, btnDisconnect, btnSend, Receive;

    // 显示接收服务器消息 按钮
    private TextView receive_message;

    // 输入需要发送的消息 输入框
    private EditText mEdit;

    public MainActivity() throws UnsupportedEncodingException {
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    // 步骤1：创建输入流对象InputStream
                    is = socket.getInputStream();

                    // 步骤2：创建输入流读取器对象 并传入输入流对象
                    // 该对象作用：获取服务器返回的数据
                    isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);

                    // 步骤3：通过输入流读取器对象 接收服务器发送过来的数据
                    response = br.readLine();

                    // 步骤4:通知主线程,将接收的消息显示到界面
                    Message msg = Message.obtain();
                    msg.what = 0;
                    mMainHandler.sendMessage(msg);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //将数组求校验码并转义
    public byte[] operate(byte[] M){

        //预留的用于放多出来的转义字符的大小
        int temp = 12;
        /*/
            求校验码
         */
        byte Check = 0x00;
        for (int i = 1; i < M.length - 1 - temp; i++) {
            Check ^= M[i];
            if (i == M.length - 2 - temp)
                M[i] = Check;
        }
        /*/
            转义
         */
        for (int i = 1; i < M.length - 1 - temp; ++i) {//在两个标志位中间找0x7e
            if (M[i] == 0x7e) {
                M[i] = 0x7d;
                for (int j = M.length - 1; j > i; j--) {
                    M[j] = M[j - 1]; //"7e"所在下标开始的元素后移一个位置
                }
                M[i + 1] = 0x02;
            }
        }
        /*/
            将预留位去除
         */
        int z, j = 0;
        for (z = 0; z < M.length - 1; ++z) {
            if (M[z] == 0x7e) {
                ++j;
                if (j == 2)
                    break;
            }
        }
        byte[] Msg = new byte[z + 1];//新建的用于存储截取后的数组的数组
        System.arraycopy(M, 0, Msg, 0, z + 1);
        return Msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * 初始化操作
         */

        // 初始化所有按钮
        btnConnect = (Button) findViewById(R.id.connect);
        btnDisconnect = (Button) findViewById(R.id.disconnect);
        btnSend = (Button) findViewById(R.id.send);
        mEdit = (EditText) findViewById(R.id.edit);
        receive_message = (TextView) findViewById(R.id.receive_message);
        Receive = (Button) findViewById(R.id.Receive);

        int heartThreadNum = 0;

        //将流水号写入Msg中
//        LocationMsg[11] = (byte) (FlowNum >> 8);
//        LocationMsg[12] = (byte) (FlowNum >> 0);

        // 初始化线程池
        mThreadPool = Executors.newFixedThreadPool(5);

        // 实例化主线程,用于更新接收过来的消息
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        receive_message.setText(response);
                        break;
                }
            }
        };


        /**
         * 创建客户端 & 服务器的连接
         */
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 利用线程池直接开启一个线程 & 执行该线程
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            // 创建Socket对象 & 指定服务端的IP 及 端口号
                            socket = new Socket("47.100.44.138", 8801);

                            // 判断客户端和服务器是否连接成功
                            System.out.println(socket.isConnected());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });


                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            while (true) {
                                sleep(100);

                                // 步骤1：创建输入流对象InputStream
                                is = socket.getInputStream();

                                // 步骤2：创建输入流读取器对象 并传入输入流对象
                                // 该对象作用：获取服务器返回的数据
                                isr = new InputStreamReader(is);
                                br = new BufferedReader(isr);

                                // 步骤3：通过输入流读取器对象 接收服务器发送过来的数据
                                response = br.readLine();

                                // 步骤4:通知主线程,将接收的消息显示到界面
                                Message msg = Message.obtain();
                                msg.what = 0;
                                mMainHandler.sendMessage(msg);

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });

            }
        });

        /**
         * 接收 服务器消息
         */
        Receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 利用线程池直接开启一个线程 & 执行该线程
//                mThreadPool.execute(new Runnable() {
                MyThread thread1 = new MyThread();
                thread1.start();
//                    @Override
//                    public void start() {
//
//                        try {
//
//                            // 步骤1：创建输入流对象InputStream
//                            is = socket.getInputStream();
//
//                            // 步骤2：创建输入流读取器对象 并传入输入流对象
//                            // 该对象作用：获取服务器返回的数据
//                            isr = new InputStreamReader(is);
//                            br = new BufferedReader(isr);
//
//                            // 步骤3：通过输入流读取器对象 接收服务器发送过来的数据
//                            response = br.readLine();
//
//                            // 步骤4:通知主线程,将接收的消息显示到界面
//                            Message msg = Message.obtain();
//                            msg.what = 0;
//                            mMainHandler.sendMessage(msg);
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                };

            }
        });


        /**
         * 发送消息 给 服务器
         */
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 利用线程池直接开启一个线程 & 执行该线程
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            byte[] LMsg = operate(LocationMsg);//位置消息
                            byte[] RMsg = operate(RegisterMsg);//注册消息

//                            String sCheck = Check+"";
//                            sMsg=sIdentifier+sMsg+sCheck+sIdentifier;
                                // 步骤1：从Socket 获得输出流对象OutputStream
                                // 该对象作用：发送数据
                                outputStream = socket.getOutputStream();

                                // 步骤2：写入需要发送的数据到输出流对象中
//                            outputStream.write((mEdit.getText().toString() + "\n").getBytes("utf-8"));
                                // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
                                outputStream.write(RMsg);

                                // 步骤3：发送数据到服务端
                                outputStream.flush();

                            } catch(IOException e){
                                e.printStackTrace();
                            }


                    }
                });

            }
        });


        /**
         * 断开客户端 & 服务器的连接
         */
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    // 断开 客户端发送到服务器 的连接，即关闭输出流对象OutputStream
                    outputStream.close();

                    // 断开 服务器发送到客户端 的连接，即关闭输入流读取器对象BufferedReader
                    br.close();

                    // 最终关闭整个Socket连接
                    socket.close();

                    // 判断客户端和服务器是否已经断开连接
                    System.out.println(socket.isConnected());

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


    }
}
