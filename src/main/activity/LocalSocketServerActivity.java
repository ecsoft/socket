package main.activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import main.activity.R;

import android.app.Activity;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LocalSocketServerActivity extends Activity
{
    ProgressBar pb;

    TextView tv;

    final int BEGIN = 0;

    final int END = 1;

    private final String TAG = "serverLocalSocketServerActivity";

    private ServerSocketThread mServerSocketThread;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_socket_server);

        // MyLogcatCapture.captureLogToFile(this, getApplication()
        // .getPackageName());
        // Log.e("yang", "hello");
        // MyLogcatCapture.endLoopCapture();
        mServerSocketThread = new ServerSocketThread();
        mServerSocketThread.start();
        tv = (TextView) findViewById(R.id.textView1);
        pb = (ProgressBar) findViewById(R.id.progressBar1);
        pb.setIndeterminate(false);
        pb.setMax(100);
        pb.setProgress(0);

        new LocalSocketClientActivity();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg)
        {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            pb.setProgress(msg.arg1);
            if (msg.what == BEGIN)
                tv.setText("a new socket comes");
            if (msg.arg2 == '9')
                tv.setText("a socket die");
        }

    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        mServerSocketThread.stopRun();
    }

    private class ServerSocketThread extends Thread
    {
        private boolean keepRunning = true;

        private LocalServerSocket serverSocket;

        private void stopRun()
        {
            keepRunning = false;
        }

        @Override
        public void run()
        {
            try
            {
                serverSocket = new LocalServerSocket("pym_local_socket");
            }
            catch (IOException e)
            {
                e.printStackTrace();

                keepRunning = false;
            }

            while (keepRunning)
            {
                Log.e(TAG, "wait for new client coming !");

                try
                {
                    LocalSocket interactClientSocket = serverSocket.accept();

                    Message m = new Message();
                    m.what = BEGIN;
                    mHandler.sendMessage(m);

                    // when accept()is blocked,Activity maybe already finish,so
                    // checking keepRunning again
                    if (keepRunning)
                    {
                        Log.e(TAG, "new client coming !");

                        new InteractClientSocketThread(interactClientSocket)
                                .start();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();

                    keepRunning = false;
                }
            }

            if (serverSocket != null)
            {
                try
                {
                    serverSocket.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private class InteractClientSocketThread extends Thread
    {
        private LocalSocket interactClientSocket;

        private StringBuilder recvStrBuilder;

        public InteractClientSocketThread(LocalSocket interactClientSocket)
        {
            this.interactClientSocket = interactClientSocket;
        }

        @Override
        public void run()

        {
            byte[] buffer = "hello,ÄãºÃ¡£".getBytes();

            InputStream inputStream = null;
            OutputStream ouputStream = null;
            try
            {
                inputStream = interactClientSocket.getInputStream();
                ouputStream = interactClientSocket.getOutputStream();

                InputStreamReader inputStreamReader = new InputStreamReader(
                        inputStream);

                // no need to do so,default code with utf-8
                // BufferedWriter bw = new BufferedWriter(new
                // OutputStreamWriter(
                // ouputStream, "UTF-8"));
                // PrintWriter pw = new PrintWriter(bw, true);
                // bw.write("sf·ðµ²É±·ð");
                // pw.write("ÊÇ·ñ ");

                ouputStream.write(buffer);
                // try
                // {
                // sleep(3000);
                // }
                // catch (InterruptedException e)
                // {
                // // TODO Auto-generated catch block
                // e.printStackTrace();
                // }
                ouputStream.write(buffer);
                ouputStream.write(buffer);
                ouputStream.write(buffer);
                ouputStream.write(buffer);

                recvStrBuilder = new StringBuilder();
                char[] buf = new char[4096];
                int readBytes = -1;
                while ((readBytes = inputStreamReader.read(buf)) != -1)
                {
                    String tempStr = new String(buf, 0, readBytes);

                    recvStrBuilder.append(tempStr);
                    Message m = new Message();
                    m.arg1 = recvStrBuilder.length();
                    m.arg2 = tempStr.toCharArray()[0];
                    Log.e(TAG, "m.arg2 =" + m.arg2 + " tempStr=" + tempStr);
                    mHandler.sendMessage(m);

                }
            }
            catch (IOException e)
            {
                e.printStackTrace();

                Log.e(TAG, "resolve data error !");
            }
            finally
            {
                if (inputStream != null)
                {
                    try
                    {
                        inputStream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
