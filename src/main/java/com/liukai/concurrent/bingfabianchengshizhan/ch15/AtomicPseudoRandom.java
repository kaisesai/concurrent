package com.liukai.concurrent.bingfabianchengshizhan.ch15;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 15-5 基于原子 AtomicInteger 实现的随机数生成器
 */
public class AtomicPseudoRandom extends PseudoRandom {

  private AtomicInteger seed;

  public AtomicPseudoRandom(int seed) {
    this.seed = new AtomicInteger(seed);
  }

  @Override
  public int nextInt(int n) {

    while (true) {
      int s = seed.get();
      int nextSeed = calculateNext(s);
      if (seed.compareAndSet(s, nextSeed)) {
        int remainder = s % n;
        return remainder > 0 ? remainder : remainder + n;
      }
    }
  }

}
