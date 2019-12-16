package com.liukai.concurrent.bingfabianchengshizhan.ch14;

import java.util.concurrent.TimeUnit;

/**
 * 14.1.1 将前提条件的失败传递给调用者（坏脾气的缓存）
 *
 * @desc 一言不合就抛异常！所以它是坏脾气的！
 */
public class GrumpyBoundedBuffer<V> extends BaseBoundedBuffer<V> {

  public GrumpyBoundedBuffer(int capacity) {
    super(capacity);
  }

  public static void main(String[] args) throws InterruptedException {

    // 客户端调用需要做好捕获异常
    GrumpyBoundedBuffer<Integer> boundedBuffer = new GrumpyBoundedBuffer<>(10);
    while (true) {
      try {
        Integer value = boundedBuffer.take();
        // 对 value 执行一些操作
        break;
      } catch (BufferEmptyException e) {
        // 遇到空队列异常进行睡眠，然后重新获取元素
        TimeUnit.SECONDS.sleep(1);
      }
    }

  }

  public synchronized void put(V v) {
    if (isFull()) {
      throw new BufferFullException();
    }
    doPut(v);
  }

  public synchronized V take() {
    if (isEmpty()) {
      throw new BufferEmptyException();
    }
    return doTake();
  }

}
