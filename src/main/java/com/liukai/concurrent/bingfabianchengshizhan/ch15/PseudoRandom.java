package com.liukai.concurrent.bingfabianchengshizhan.ch15;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 假冒的随机数
 */
public abstract class PseudoRandom {

  private Random random = new Random();

  public static void main(String[] args) {
    // 重入锁版本的伪随机数生成器
    PseudoRandom pseudoRandom = new ReentrantLockPseudoRandom(10);
    // 原子变量获取伪随机数生成器
    // PseudoRandom pseudoRandom = new AtomicPseudoRandom(10);
    // 使用线程本地化伪随机数生成器
    // PseudoRandom pseudoRandom = new ThreadLocalPseudoRandom();
    long start = System.currentTimeMillis();
    int nThread = 10;

    for (int i = 1; i < nThread; i++) {
      int finalI = i;
      new Thread(() -> {
        int next = pseudoRandom.calculateNext(finalI);
        System.out.println(Thread.currentThread().getName() + "\t获取随机数：" + next);
      }, "线程" + i).start();
    }

    // 检测上述线程生命周期已经结束
    while (Thread.activeCount() > 1) {
      // do nothing
    }
    System.out.println();
    System.out.println(
      nThread + "个线程使用\t" + pseudoRandom.getClass().getSimpleName() + "\t随机数生成器一共耗时：" + (
        System.currentTimeMillis() - start) + "ms");
  }

  protected int calculateNext(int s) {
    try {
      TimeUnit.NANOSECONDS.sleep(10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return random.nextInt(s);
  }

  public abstract int nextInt(int n);

}
