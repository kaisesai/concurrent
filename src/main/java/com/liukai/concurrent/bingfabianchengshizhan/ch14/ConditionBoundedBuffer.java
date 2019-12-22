package com.liukai.concurrent.bingfabianchengshizhan.ch14;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 14.3 显示条件变量的有界缓存
 *
 * @param <T>
 */
public class ConditionBoundedBuffer<T> extends BaseBoundedBuffer<T> {

  protected final Lock lock = new ReentrantLock();

  // 条件谓词：notFull （count < items.length）
  private final Condition notFull = lock.newCondition();

  // 条件谓词：notEmpty（count > 0）
  private final Condition notEmpty = lock.newCondition();

  public ConditionBoundedBuffer(int capacity) {
    super(capacity);
  }

  public static void main(String[] args) throws InterruptedException {
    ConditionBoundedBuffer<Integer> buffer = new ConditionBoundedBuffer<>(10);

    int prodNum = 10;
    for (int i = 0; i < prodNum; i++) {
      int finalI = i;
      new Thread(() -> {

        while (true) {
          try {
            buffer.put(finalI);
            System.out.println(Thread.currentThread().getName() + "\t生产：" + finalI);
            TimeUnit.SECONDS.sleep(1);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }, "生产者线程" + i).start();
    }

    int custNum = 1;
    for (int i = 0; i < custNum; i++) {
      new Thread(() -> {
        while (true) {
          try {
            Integer take = buffer.take();
            System.out.println(Thread.currentThread().getName() + "\t消费：" + take);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }, "消费者线程" + i).start();
    }

    TimeUnit.SECONDS.sleep(10);
    System.exit(0);

  }

  public void put(T t) throws InterruptedException {
    lock.lock();
    try {
      while (isFull()) {
        notFull.await();
      }
      doPut(t);
      notEmpty.signal();
    } finally {
      lock.unlock();
    }
  }

  public T take() throws InterruptedException {
    lock.lock();
    try {
      while (isEmpty()) {
        notEmpty.await();
      }
      T t = doTake();
      notFull.signal();
      return t;
    } finally {
      lock.unlock();
    }
  }

}
