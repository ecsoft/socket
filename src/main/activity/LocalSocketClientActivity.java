package main.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * @author pengyiming
 * @note start localSocketClient,send heart beat to server
 * 
 */

public class LocalSocketClientActivity extends Activity
{
    private final static String TAG = "clientLocalSocketClientActivity";

    private HeartBeatThread mHeartBeatThread;

    public native int startHeartBeat();

    static
    {
        System.loadLibrary("pcmclient");
    }

    LocalSocketClientActivity()
    {
        Log.e(TAG, "-----new-----");
        mHeartBeatThread = new HeartBeatThread();
        mHeartBeatThread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        mHeartBeatThread.stopRun();
    }

    private class HeartBeatThread extends Thread
    {
        int ret;

        boolean keepRunning = true;

        public void stopRun()
        {
            keepRunning = false;
        }

        @Override
        public void run()
        {
            Log.e(TAG, "start heart beat!");

            while (keepRunning)
            {// last one
                ret = startHeartBeat();

                Log.e(TAG, "ret = " + ret);

                if (ret != 0)
                {
                    break;
                }

                try
                {
                    sleep(3000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            Log.e(TAG, "stop heart beat!");
        }
    }
}
