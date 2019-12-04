package com.liukai.concurrent.bingfabianchengshizhan.ch12;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 12.2 性能测试
 */
public class Ch12_2 {

  public static void main(String[] args) throws InterruptedException {

    int tpt = 10000;// 每个线程中测试次数

    for (int capacity = 1; capacity <= 10000; capacity *= 10) {// 控制队列的容量
      System.out.println("Capacity: " + capacity);
      for (int paris = 1; paris <= 128; paris *= 2) {// 生产者与消费者对数
        TimedPutTakeTest putTakeTest = new TimedPutTakeTest(capacity, paris, tpt);
        System.out.print("Paris: " + paris + "\t");
        putTakeTest.test();
        System.out.print("\t");
        Thread.sleep(100);
        System.out.println();
        Thread.sleep(100);
      }
    }
    TimedPutTakeTest.pool.shutdown();
  }

  /**
   * 扩展的统计时间的测试类
   */
  static class TimedPutTakeTest {

    // 创建一个无界缓存线程池
    private static ExecutorService pool = Executors.newCachedThreadPool();

    // 循环栅栏，用于协调生产者与消费者线程以及主线程的交互操作
    protected final CyclicBarrier barrier;

    // 统计put与take的总数之和
    private final AtomicInteger putSum = new AtomicInteger(0);

    private final AtomicInteger takeSum = new AtomicInteger(0);

    // 有界队列
    private final BoundedBuffer<Integer> bb;

    // 生产者于消费者的数量
    private final int nPairs;

    // 测试的数量
    private final int nTrials;

    private final BarrierTimer timer;

    public TimedPutTakeTest(int capacity, int nPairs, int nTrials) {
      this.timer = new BarrierTimer();
      this.barrier = new CyclicBarrier(nPairs * 2 + 1, timer);
      this.bb = new BoundedBuffer<>(capacity);
      this.nPairs = nPairs;
      this.nTrials = nTrials;
    }

    /**
     * 简单的随机数生成器
     *
     * @param y
     * @return
     */
    static int xorShift(int y) {
      y ^= (y << 6);
      y ^= (y >>> 21);
      y ^= (y << 7);
      return y;
    }

    void test() {
      try {

        timer.clear();
        // 创建 nPairs 个生产者与消费者任务
        for (int i = 0; i < nPairs; i++) {
          pool.execute(new TimedPutTakeTest.Producter());
          pool.execute(new TimedPutTakeTest.Customer());
        }

        /*
        1. 即创建 barrier 时，指定了21个线程数量，for循环10次，即生产者与消费者在run方法中首次调用 await方法，一共20次，
        2. 主线程调用 barrier.await() 方法后，正好达到21次调用（每调用一次await方法，内部的count都会-1，减到0时，就会唤醒线程），此时barrier就会唤醒所有等待的线程，并且 barrier的count重新设置为21。
        3. 在生产者与消费者线程都执行完毕run方法后，调用了 await方法，主线线程也调用await方法,此时也调用了21次，barrier的count为0，唤醒所有线程继续执行。
         */
        barrier.await();// 等待所有线程就绪,
        barrier.await();// 等待所有线程执行完毕

        // 任务执行的总时间
        long totalTime = timer.getTime();
        // 平均任务耗时
        long time = totalTime / (nPairs * nTrials);
        double totalTimeForSec = totalTime / 10_000_000_000D;
        double pearThreadTaskTimeForSec = totalTimeForSec / nPairs;
        double tunTuLiang = 1 / pearThreadTaskTimeForSec;

        System.out.println(
          "每个线程的每个任务平均耗时：" + time + "ns\t总耗时" + totalTime + "ns\t" + totalTimeForSec
            + "s\t每个线程执行时间：" + pearThreadTaskTimeForSec + "s\t吞吐量（每秒钟可以执行的线程数量taks）：" + tunTuLiang);
      } catch (InterruptedException | BrokenBarrierException e) {
        e.printStackTrace();
      }
    }

    /**
     * 统计任务时间的任务类
     */
    class BarrierTimer implements Runnable {

      // 是否开始
      private boolean started;

      // 任务开始时间
      private long startTime;

      // 任务结束时间
      private long endTime;

      @Override
      public synchronized void run() {
        long t = System.nanoTime();
        if (!this.started) {
          this.started = true;
          this.startTime = t;
        } else {
          this.endTime = t;
        }
      }

      private synchronized void clear() {
        this.started = false;
      }

      private synchronized long getTime() {
        return endTime - startTime;
      }

    }

    private class Producter implements Runnable {

      @Override
      public void run() {
        // 使用 hashcode 与 时间毫秒的值
        int seed = this.hashCode() ^ (int) System.nanoTime();
        int sum = 0;
        try {
          // 栅栏等待
          barrier.await();
          // 执行 nTrials 次put操作
          for (int i = 0; i < nTrials; i++) {
            bb.put(seed);
            sum += seed;
            // 随机生成数
            seed = xorShift(seed);
          }
          // putSum 加上数据
          putSum.getAndAdd(sum);
          barrier.await();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

      }

    }

    private class Customer implements Runnable {

      @Override
      public void run() {
        try {
          barrier.await();
          int sum = 0;
          for (int i = 0; i < nTrials; i++) {
            sum += bb.take();
          }
          takeSum.getAndAdd(sum);
          barrier.await();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }

    }

  }

}
