package com.liukai.concurrent.bingfabianchengshizhan.ch15;

import java.util.Random;

/**
 * 假冒的随机数
 */
public abstract class PseudoRandom {

  private Random random = new Random();

  public static void main(String[] args) {
    // 重入锁版本的伪随机数生成器
    PseudoRandom pseudoRandom = new ReentrantLockPseudoRandom(10);
    int nThread = 10;
    for (int i = 0; i < nThread; i++) {
      int finalI = i;
      new Thread(() -> {
        int next = pseudoRandom.calculateNext(finalI);

      }, "线程" + i).start();
    }

  }

  protected int calculateNext(int s) {
    return random.nextInt(s);
  }

  public abstract int nextInt(int n);

}
