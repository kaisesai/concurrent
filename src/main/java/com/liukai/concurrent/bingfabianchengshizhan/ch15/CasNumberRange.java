package com.liukai.concurrent.bingfabianchengshizhan.ch15;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 15-3 通过 CAS 来维持包含多个变量的不变性条件
 */
public class CasNumberRange {

  private final AtomicReference<IntPair> atomicReference;

  public CasNumberRange(int lower, int upper) {
    atomicReference = new AtomicReference<>(new IntPair(lower, upper));
  }

  public static void main(String[] args) throws InterruptedException {
    CasNumberRange range = new CasNumberRange(0, 100);
    for (int i = 0; i < 5; i++) {
      int finalI = i + 1;
      new Thread(() -> {

        try {
          TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        range.setLower(finalI);
        // range.setUpper(finalI);

      }, "线程" + i).start();
    }

    while (Thread.activeCount() > 1) {
      TimeUnit.SECONDS.sleep(1);
      System.out.println("当前线程数量：" + Thread.activeCount());
    }

    System.out.println("数值范围 lower:" + range.getLower() + ", upper:" + range.getUpper());
  }

  public int getLower() {
    return atomicReference.get().lower;
  }

  /**
   * 设置下界值
   *
   * @param i 下界值
   */
  public void setLower(int i) {
    while (true) {
      System.out.println(Thread.currentThread().getName() + "\t开始设置下界元素：" + i);
      IntPair oldV = atomicReference.get();
      if (i > oldV.upper) {
        throw new IllegalArgumentException("Con't set lower to " + i + "> upper");
      }
      if (atomicReference.compareAndSet(oldV, new IntPair(i, oldV.upper))) {
        System.out.println(Thread.currentThread().getName() + "\t完成设置下界元素：" + i);
        return;
      }
    }
  }

  public int getUpper() {
    return atomicReference.get().upper;
  }

  /**
   * 设置上界值
   *
   * @param i 上界值
   */
  public void setUpper(int i) {
    while (true) {
      System.out.println(Thread.currentThread().getName() + "\t开始设置上界元素：" + i);
      IntPair oldV = atomicReference.get();
      if (i < oldV.lower) {
        throw new IllegalArgumentException("Con't set upper to " + i + "< lower");
      }
      IntPair newV = new IntPair(oldV.lower, i);
      if (atomicReference.compareAndSet(oldV, newV)) {
        System.out.println(Thread.currentThread().getName() + "\t完成设置下界元素：" + i);
        return;
      }

    }
  }

  private static class IntPair {

    final int lower;// 不变性条件是：lower <=upper

    final int upper;

    public IntPair(int lower, int upper) {
      this.lower = lower;
      this.upper = upper;
    }

    @Override
    public String toString() {
      return "IntPair{" + "lower=" + lower + ", upper=" + upper + '}';
    }

  }

}
