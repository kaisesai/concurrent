package com.liukai.concurrent.bingfabianchengshizhan.ch15;

/**
 * 15-2 基于 CAS 实现的非阻塞计数器
 */
public class CasCounter {

  private SimulateCAS simulateCAS = new SimulateCAS();

  public static void main(String[] args) {

    CasCounter casCounter = new CasCounter();

    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        int increment = casCounter.increment();
        System.out.println(Thread.currentThread().getName() + "\t自增值为：" + increment);
      }, "线程" + i).start();
    }

  }

  public int getValue() {
    return simulateCAS.getValue();
  }

  /**
   * 非阻塞式自增操作
   *
   * @return 返回自增之后的值
   */
  public int increment() {
    int v;
    do {
      v = getValue();
      System.out.println(Thread.currentThread().getName() + "\t循环获取值\tvalue=" + v);
    }
    while (v != simulateCAS.compareAndSwap(v, v + 1));
    // while (!simulateCAS.compareAndSet(v, v + 1));
    return v + 1;
  }

}
