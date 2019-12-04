package com.liukai.concurrent.bingfabianchengshizhan.ch12;

import org.junit.Assert;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 并发程序的测试
 * <p>
 * 1. 正确性测试
 * 1.1 基本的单元测试
 * 1.2 对阻塞操作的测试
 * 1.3 安全性测试
 * 1.4 资源管理的测试
 * 1.5 使用回调
 *
 * </p>
 * 12-1 正确性测试
 */
public class Ch12_1 {

  //====================================== 12.1.5 使用回调 =====================================

  static void testPoolExpansion() throws InterruptedException {

    // 自定义线程工厂
    TestingThreadFactory threadFactory = new TestingThreadFactory();

    int MAX_SIZE = 10;
    ExecutorService pool = Executors.newFixedThreadPool(MAX_SIZE, threadFactory);

    // 创建 10*MAX_SIZE 个线程
    for (int i = 0; i < 10 * MAX_SIZE; i++) {
      pool.execute(() -> {
        try {
          Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });

    }

    for (int i = 0; i < 20 && threadFactory.numCreated.get() < MAX_SIZE; i++) {
      Thread.sleep(100);
    }
    System.out.println("线程池已经创建的线程数量：" + threadFactory.numCreated.get());

    Assert.assertEquals(MAX_SIZE, threadFactory.numCreated.get());
    // 关闭线程池中的线程
    pool.shutdownNow();

  }

  static void testLeak() throws InterruptedException {
    int capacity = 10000;
    BoundedBuffer<Big> bb = new BoundedBuffer<>(capacity);
    for (int i = 0; i < capacity; i++) {
      bb.put(new Big());
    }
    Thread.sleep(10000);
    System.out.println("方法结束了！");
  }

  static void testTakeBlocksWhenEmpty() {
    BoundedBuffer<Integer> bb = new BoundedBuffer<>(10);
    Thread t1 = new Thread(() -> {

      // 从队列中获取元素
      try {
        bb.take();
        fai();
      } catch (InterruptedException e) {
        System.out.println(Thread.currentThread().getName() + "\t被中断, 操作正确");
      }
    }, "t1");

    try {
      t1.start();
      // 中断t1线程
      t1.interrupt();
      // 注意不要使用 Thread.interrupted() 的方法，它会中断当前线程
      // Thread.interrupted()
      // 睡眠
      Thread.sleep(2000);
      // 让t1线程加入进来
      t1.join(1000);
      // 判断线程t1是否还活着
      Assert.assertFalse("线程t1已经死去", t1.isAlive());

    } catch (InterruptedException e) {
      e.printStackTrace();
    }

  }

  static void fai() {
    System.out.println(Thread.currentThread().getName() + "\t失败的调用");
  }

  //====================================== 12.1.3 安全性的测试 =====================================

  /**
   * 测试队列是否为空与是否已满
   */
  static void testIsEmptyWhenConstrunctd() {
    BoundedBuffer<Integer> bb = new BoundedBuffer<>(10);
    Assert.assertTrue("队列为空", bb.isEmpty());
    Assert.assertTrue("队列为空，不满", bb.isFull());
  }

  //====================================== 12.1.2 对阻塞操作的测试 =====================================

  /**
   * 测试队列在填充之后是否已满
   */
  static void testFullAfterPuts() throws InterruptedException {
    BoundedBuffer<Integer> bb = new BoundedBuffer<>(10);
    for (int i = 0; i < 10; i++) {
      bb.put(i);
    }
    Assert.assertTrue("队列为空，不满", bb.isFull());
  }

  public static void main(String[] args) throws InterruptedException {
    // testIsEmptyWhenConstrunctd();
    // testFullAfterPuts();
    // testTakeBlocksWhenEmpty();

    // 多个线程put与take的交互操作
    // new PutTakeTest(10, 10, 100000).test();
    // PutTakeTest.pool.shutdown();

    // 资源操作的测试
    // testLeak();

    // 使用回调
    testPoolExpansion();
  }

  //====================================== 12.1.1 基本单元的测试 =====================================

  /**
   * 记录线程创建数量的线程工厂
   */
  static class TestingThreadFactory implements ThreadFactory {

    private final AtomicInteger numCreated = new AtomicInteger();

    private final ThreadFactory threadFactory = Executors.defaultThreadFactory();

    @Override
    public Thread newThread(Runnable r) {
      numCreated.incrementAndGet();
      return threadFactory.newThread(r);
    }

  }

  //====================================== 12.1.4 资源管理的测试 =====================================
  static class Big {

    // 直接导致堆内存溢出 java.lang.OutOfMemoryError: Java heap space
    double[] data = new double[100000];

  }

  /**
   * BoundedBuffer 生产者与消费者测试类
   */
  static class PutTakeTest {

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

    public PutTakeTest(int capacity, int nPairs, int nTrials) {
      this.barrier = new CyclicBarrier(nPairs * 2 + 1);
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

        // 创建 nPairs 个生产者与消费者任务
        for (int i = 0; i < nPairs; i++) {
          pool.execute(new Producter());
          pool.execute(new Customer());
        }

        /*
        1. 即创建 barrier 时，指定了21个线程数量，for循环10次，即生产者与消费者在run方法中首次调用 await方法，一共20次，
        2. 主线程调用 barrier.await() 方法后，正好达到21次调用（每调用一次await方法，内部的count都会-1，减到0时，就会唤醒线程），此时barrier就会唤醒所有等待的线程，并且 barrier的count重新设置为21。
        3. 在生产者与消费者线程都执行完毕run方法后，调用了 await方法，主线线程也调用await方法,此时也调用了21次，barrier的count为0，唤醒所有线程继续执行。
         */
        barrier.await();// 等待所有线程就绪,
        barrier.await();// 等待所有线程执行完毕
        Assert.assertTrue("putSum 与 takeSum 不一致！", putSum.get() == takeSum.get());
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (BrokenBarrierException e) {
        e.printStackTrace();
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
          // TODO 这了又执行了一次??? 不太懂啊
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
          // TODO 执行两次？
          barrier.await();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

      }

    }

  }

}
