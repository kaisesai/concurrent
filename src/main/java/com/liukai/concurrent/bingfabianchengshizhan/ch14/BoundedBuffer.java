package com.liukai.concurrent.bingfabianchengshizhan.ch14;

import java.util.concurrent.TimeUnit;

/**
 * 14.1.3 使用条件队列来实现有界队列缓存
 * <p>
 * <p>
 * 阻塞队列版本2.0：object（wait、notifyAll）版本的条件通知
 * <p>
 * 使用条件通知：满足一定条件才会进行通知，而不是全部通知，该操作可以降低线程上下文切换，减小锁的竞争
 * </p>
 *
 * <p>
 * put 方法中，增加判断是否已空的条件谓词，为true则通知
 * </p>
 * <p>
 * take 方法中，增加判断是否已满的条件谓词，为true则通知
 * </p>
 *
 * @param <V>
 */
public class BoundedBuffer<V> extends BaseBoundedBuffer<V> {

  public BoundedBuffer(int capacity) {
    super(capacity);
  }

  public static void main(String[] args) {

    BoundedBuffer<Integer> buffer = new BoundedBuffer<>(10);

    // 生产者
    int prodNum = 10;
    for (int i = 0; i < prodNum; i++) {

      new Thread(() -> {

        int count = 0;
        while (true) {
          try {
            buffer.put(++count);
            System.out.println(Thread.currentThread().getName() + "\tput: " + count);
            TimeUnit.SECONDS.sleep(2);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }, "prud" + i).start();
    }

    // 消费者
    int cusNum = 1;
    for (int i = 0; i < cusNum; i++) {

      new Thread(() -> {

        while (true) {
          try {
            Integer take = buffer.take();
            System.out.println(Thread.currentThread().getName() + "\ttake: " + take);
            // TimeUnit.SECONDS.sleep(1);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }, "cust" + i).start();
    }

  }

  public synchronized void put(V v) throws InterruptedException {
    while (isFull()) {
      wait();
    }
    boolean empty = isEmpty();
    doPut(v);
    if (empty) {
      System.out.println(Thread.currentThread().getName() + "\tnotifyAll custThread");
      notifyAll();
    }
  }

  public synchronized V take() throws InterruptedException {
    while (isEmpty()) {
      wait();
    }

    boolean full = isFull();
    V e = doTake();
    if (full) {
      System.out.println(Thread.currentThread().getName() + "\tnotifyAll prodThread");
      notifyAll();
    }

    return e;
  }

}
