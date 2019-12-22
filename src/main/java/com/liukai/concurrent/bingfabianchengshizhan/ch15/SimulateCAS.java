package com.liukai.concurrent.bingfabianchengshizhan.ch15;

/**
 * 15-1 模拟 CAS 操作
 */
public class SimulateCAS {

  private int value;

  public synchronized int getValue() {
    return value;
  }

  /**
   * 比较并交换
   *
   * @param expectedValue 期望值
   * @param newValue      新值
   * @return value
   */
  public synchronized int compareAndSwap(int expectedValue, int newValue) {
    // 这里为什么要复制将当前值作为一个单独的本地变量呢？为了记录此时的值？
    // 这是比较好的做法，在方法执行前，获取当前值并赋值到一个变量上。记录此时此刻的值
    int oldValue = this.value;
    if (expectedValue == oldValue) {
      this.value = newValue;
    }
    return oldValue;
  }

  /**
   * 比较并设值
   *
   * @param expectedValue 期望值
   * @param newValue      新值
   * @return true 为设置成功，false 为设置失败
   */
  public synchronized boolean compareAndSet(int expectedValue, int newValue) {
    return (expectedValue == compareAndSwap(expectedValue, newValue));
  }

}
