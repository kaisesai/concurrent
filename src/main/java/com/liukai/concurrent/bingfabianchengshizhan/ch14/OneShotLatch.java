package com.liukai.concurrent.bingfabianchengshizhan.ch14;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 14.5 使用 AbstractQueuedSynchronizer 实现的二元闭锁
 */
public class OneShotLatch {

  private Sync sync = new Sync();

  public static void main(String[] args) throws InterruptedException {
    // 二元闭锁
    OneShotLatch latch = new OneShotLatch();
    new Thread(() -> {
      System.out.println(Thread.currentThread().getName() + "\t准备中...");
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println(Thread.currentThread().getName() + "\t准备好了！");
      latch.signal();
    }, "t1").start();
    latch.await();
    System.out.println(Thread.currentThread().getName() + "\t开始启动了！");

    System.out.println(1 >>> 16);

  }

  public void signal() {
    sync.releaseShared(0);
  }

  public void await() throws InterruptedException {
    sync.acquireSharedInterruptibly(0);
  }

  private static class Sync extends AbstractQueuedSynchronizer {

    @Override
    protected int tryAcquireShared(int arg) {
      // 如果闭锁是开的（state ==1 ），那么这个操作将成功，否则失败
      return getState() == 1 ? 1 : -1;
    }

    @Override
    protected boolean tryReleaseShared(int arg) {
      setState(1);
      return true;
    }

  }

}
