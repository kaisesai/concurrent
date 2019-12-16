package com.liukai.concurrent.bingfabianchengshizhan.ch14;

import java.util.concurrent.TimeUnit;

/**
 * 14.1.2 通过轮询和与休眠来实现简单的阻塞
 *
 * @param <V>
 */
public class SleepyBoundedBuffer<V> extends BaseBoundedBuffer<V> {

  public SleepyBoundedBuffer(int capacity) {
    super(capacity);
  }

  public void put(V v) throws InterruptedException {
    while (true) {
      synchronized (this) {
        if (!isFull()) {
          doPut(v);
          return;
        }
      }
      TimeUnit.SECONDS.sleep(1);
    }
  }

  public V take() throws InterruptedException {
    while (true) {
      synchronized (this) {
        if (!isEmpty()) {
          return doTake();
        }
      }
      TimeUnit.SECONDS.sleep(1);
    }
  }

}
