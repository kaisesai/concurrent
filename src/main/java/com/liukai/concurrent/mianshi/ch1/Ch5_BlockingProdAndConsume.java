package com.liukai.concurrent.mianshi.ch1;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 阻塞队列——生产者/消费者
 */
public class Ch5_BlockingProdAndConsume {

  /*
    内网段范围：
    10.0.0.0~10.255.255.255
    172.16.0.0~172.31.255.255
    192.168.0.0~192.168.255.255
    127.0.0.1
   */
  public static final String INTERNAL_IP_REG = "((192\\.168|172\\.([1][6-9]|[2]\\d|3[01]))"
    + "(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){2}|"
    + "^(\\D)*10(\\.([2][0-4]\\d|[2][5][0-5]|[01]?\\d?\\d)){3}|127\\.0\\.0\\.1)";

  public static final Pattern INTERNAL_IP_PATTERN = Pattern.compile(INTERNAL_IP_REG);

  public static void main(String[] args) throws InterruptedException {

    Matcher m = INTERNAL_IP_PATTERN.matcher("172.16.0.1");
    // System.out.println(m.find());

    if (m.find()) {
      System.out.println("Found group size: " + m.groupCount());
      System.out.println("Found value: " + m.group(0));
      System.out.println("Found value: " + m.group(1));
      System.out.println("Found value: " + m.group(2));
      System.out.println("Found value: " + m.group(3));
      System.out.println("Found value: " + m.group(4));
      System.out.println("Found value: " + m.group(5));
      System.out.println("Found value: " + m.group(6));
      System.out.println("Found value: " + m.group(7));
    } else {
      System.out.println("NO MATCH");
    }
    // testMyBlockProdAndConsume();

    // testBlockingQueue();

  }

  /**
   * 测试阻塞队列
   */
  private static void testBlockingQueue() {
    MyBlockingQueue<String> myBlockingQueue = new MyBlockingQueue<>(1);

    new Thread(() -> {

      for (int i = 0; i < 100; i++) {
        try {
          myBlockingQueue.put(i + 1 + "");
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    }, "生产者线程").start();

    new Thread(() -> {

      for (int i = 0; i < 100; i++) {
        try {
          myBlockingQueue.take();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    }, "消费者线程").start();
  }

  /**
   * 测试
   *
   * @throws InterruptedException
   */
  private static void testMyBlockProdAndConsume() throws InterruptedException {
    MyResource myResource = new MyResource(new ArrayBlockingQueue<>(10));

    new Thread(() -> {
      System.out.println(Thread.currentThread().getName() + "线程启动");
      try {
        myResource.product();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }, "生产者").start();

    new Thread(() -> {
      System.out.println(Thread.currentThread().getName() + "线程启动");
      System.out.println();
      System.out.println();
      try {

        myResource.consume();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }, "消费者").start();

    System.out.println();
    System.out.println();

    // 大老板叫停了！
    TimeUnit.SECONDS.sleep(5);
    System.out.println(Thread.currentThread().getName() + "\t大BOSS叫停了!");
    myResource.stop();
  }

  /**
   * 新版 3.0阻塞队列之生产者消费者
   */
  public static class MyResource {

    private volatile boolean flag = true;

    private AtomicInteger ac = new AtomicInteger();

    private BlockingQueue<String> blockingQueue;

    public MyResource(BlockingQueue<String> blockingQueue) {
      this.blockingQueue = blockingQueue;
    }

    /**
     * 无限消费
     */
    public void product() throws InterruptedException {
      // 允许声场
      String data = null;
      while (flag) {
        data = ac.incrementAndGet() + "";
        // 阻塞添加数据
        boolean reValue = blockingQueue.offer(data, 2, TimeUnit.SECONDS);
        if (reValue) {
          System.out.println(Thread.currentThread().getName() + "\t生产了一个蛋糕：" + data + "\t成功");
        } else {
          System.out.println(Thread.currentThread().getName() + "\t生产了一个蛋糕：" + data + "\t失败");
        }
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      // 再次判断是否允许继续
      System.out.println(Thread.currentThread().getName() + "\t大老板叫停了！停止生蛋糕");
    }

    /**
     * 持续消费
     */
    public void consume() throws InterruptedException {
      while (flag) {
        // 消费
        String data = blockingQueue.poll(2, TimeUnit.SECONDS);
        if (data == null || "".equalsIgnoreCase(data)) {
          flag = false;
          System.out.println(Thread.currentThread().getName() + "没有蛋糕了！退出！");
          return;
        }
        System.out.println(Thread.currentThread().getName() + "\t消费了一个蛋糕：" + data + "\t成功");

      }
    }

    public void stop() {
      flag = false;
    }

  }

  // 真——阻塞队列（仿照ArrayBlockingQueue）
  public static class MyBlockingQueue<E> {

    private ReentrantLock lock = new ReentrantLock();

    private Condition notEmpty = lock.newCondition();

    private Condition notFull = lock.newCondition();

    private AtomicInteger currentCount = new AtomicInteger();

    private volatile int takeIndex;

    private volatile int putIndex;

    private Object[] data;

    public MyBlockingQueue(int count) {
      data = new Object[count];
    }

    public void put(E e) throws InterruptedException {
      lock.lock();
      try {
        while (data.length == currentCount.get()) {
          // 判断队列是否已满
          notFull.await();
        }
        enqueue(e);
      } finally {
        lock.unlock();
      }
    }

    private void enqueue(E e) throws InterruptedException {
      // 添加元素
      final Object[] items = data;
      items[putIndex] = e;
      if (++putIndex == data.length) {
        putIndex = 0;
      }
      // 移动尾部指针，如果指针查过了数组的索引值，则循环到0
      System.out.println(Thread.currentThread().getName() + "\t生产元素：" + e);
      currentCount.incrementAndGet();
      notEmpty.signal();
      // TimeUnit.SECONDS.sleep(1);
    }

    public E take() throws InterruptedException {
      lock.lock();
      try {
        // 判断队列是否已满
        while (currentCount.get() == 0) {
          notEmpty.await();
        }
        return dequeue();

      } finally {
        lock.unlock();
      }

    }

    private E dequeue() throws InterruptedException {
      // 去头部元素
      final Object[] items = data;
      E e = (E) items[takeIndex];
      if (++takeIndex == items.length) {
        takeIndex = 0;
      }
      currentCount.decrementAndGet();
      System.out.println(Thread.currentThread().getName() + "\t消费元素：" + e);
      notFull.signal();
      // TimeUnit.SECONDS.sleep(1);
      return e;
    }

  }

}
