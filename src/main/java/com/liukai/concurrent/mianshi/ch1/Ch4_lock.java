package com.liukai.concurrent.mianshi.ch1;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 锁的种类
 */
public class Ch4_lock {

  public static void main(String[] args) {

    MyCache myCache = new MyCache();

    int time = 5;
    // 同步写入数据
    for (int i = 0; i < time; i++) {
      int finalI = i;
      new Thread(() -> {
        myCache.put(finalI + "", finalI + "");
      }, i + "").start();
    }

    for (int i = 0; i < time; i++) {
      // 写入数据
      int finalI = i;
      new Thread(() -> {
        myCache.get(finalI + "");
      }, i + "").start();

    }
  }

  /**
   * 读写锁（独占/共享锁)）
   */
  static class MyCache {

    private volatile Map<String, Object> map = new HashMap<>();

    private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public void put(String key, Object value) {
      readWriteLock.writeLock().lock();
      try {

        System.out.println(Thread.currentThread().getName() + "\t开始执行put\tkey=" + key);
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        map.put(key, value);
        System.out.println(Thread.currentThread().getName() + "\t结束执行put\tkey=" + key);
      } finally {
        readWriteLock.writeLock().unlock();
      }

    }

    public void get(String key) {
      readWriteLock.readLock().lock();
      try {

        System.out.println(Thread.currentThread().getName() + "\t开始执行get\tkey=" + key);
        // try {
        //   TimeUnit.SECONDS.sleep(2);
        // } catch (InterruptedException e) {
        //   e.printStackTrace();
        // }
        Object value = map.get(key);
        System.out
          .println(Thread.currentThread().getName() + "\t结束执行get\tkey=" + key + "\tvalue=" + value);
      } finally {
        readWriteLock.readLock().unlock();
      }

    }

  }

}
