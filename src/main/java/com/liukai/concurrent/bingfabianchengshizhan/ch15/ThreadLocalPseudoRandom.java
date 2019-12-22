package com.liukai.concurrent.bingfabianchengshizhan.ch15;

/**
 * 15-5-1 基于 ThreadLocal 线程本地化的随机数生成器
 * <p>
 * 这种做法改变了类的行为，每个线程有自己的随机数，避免了竞争
 * </p>
 */
public class ThreadLocalPseudoRandom extends PseudoRandom {

  private static final ThreadLocal<Integer> SEED_LOCAL = ThreadLocal.withInitial(() -> 0);

  @Override
  public int nextInt(int n) {
    int preSeedValue = SEED_LOCAL.get();
    int nextSeed = calculateNext(preSeedValue);
    SEED_LOCAL.set(nextSeed);
    int remainder = preSeedValue % n;
    return remainder > 0 ? remainder : remainder + n;
  }

}
