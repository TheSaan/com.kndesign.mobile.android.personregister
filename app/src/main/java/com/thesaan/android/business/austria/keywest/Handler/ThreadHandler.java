package com.thesaan.android.business.austria.keywest.Handler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Michael Knöfler on 24.02.2015.
 */
public class ThreadHandler{

    //maximum cores
    private static final int NUMBER_OF_PROCESSORE_CORES = Runtime.getRuntime().availableProcessors();

    //set the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;

    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    static {
        ThreadHandler threadHandler = new ThreadHandler();
       /* Handler handler = new Handler(Looper.getMainLooper()){

            @Override
            public void close() {

            }

            @Override
            public void flush() {

            }

            @Override
            public void publish(LogRecord record) {

            }
        };*/
    }

    private ThreadHandler(){

        // A queue of Runnables
        final BlockingQueue<Runnable> mDecodeWorkQueue;

        // Instantiates the queue of Runnables as a LinkedBlockingQueue
        mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();

        ThreadPoolExecutor mDecodeThreadPool = new ThreadPoolExecutor(NUMBER_OF_PROCESSORE_CORES,NUMBER_OF_PROCESSORE_CORES,KEEP_ALIVE_TIME,KEEP_ALIVE_TIME_UNIT,mDecodeWorkQueue);


    }
}
