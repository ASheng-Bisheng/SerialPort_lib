package cn.whzwl.xbs.serialport.utils;

public abstract class ThreadUtil extends Thread {

    private final Object lock = new Object();
    public boolean pause = false;

    /**
     * 调用这个方法实现暂停线程
     */
   public void pauseThread() {
        pause = true;
    }

    /**
     * 调用这个方法实现恢复线程的运行
     */
    public void resumeThread() {
        pause = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * 注意：这个方法只能在run方法里调用，不然会阻塞主线程，导致页面无响应
     */
    protected void onThreadPause() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        super.run();
        try {
            runMain();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public abstract void runMain() throws InterruptedException;


}
