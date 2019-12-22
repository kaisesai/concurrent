package com.liukai.concurrent.bingfabianchengshizhan.ch15;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 15-4 基于 ReentrantLock 实现的随机数生成器
 */
public class ReentrantLockPseudoRandom extends PseudoRandom {

  private final Lock lock = new ReentrantLock();

  private int seed;

  public ReentrantLockPseudoRandom(int seed) {
    this.seed = seed;
  }

  @Override
  public int nextInt(int n) {
    lock.lock();
    try {
      int s = seed;
      seed = super.calculateNext(s);
      int remainder = s % n;
      return remainder > 0 ? remainder : remainder + n;
    } finally {
      lock.unlock();
    }

  }

}
