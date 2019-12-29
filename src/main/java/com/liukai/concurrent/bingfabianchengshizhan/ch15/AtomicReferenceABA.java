package com.liukai.concurrent.bingfabianchengshizhan.ch15;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * 15-9 原子引用版本号器
 * <p>
 * 解决 ABA 问题
 * </p>
 */
public class AtomicReferenceABA {

  public static void main(String[] args) {

    // 创建时间错原子引用
    AtomicStampedReference<String> stampedReference = new AtomicStampedReference<>(null, 0);

    new Thread(() -> {
      stampedReference.compareAndSet(null, "A", 0, 1);
    }, "t1").start();

    new Thread(() -> {
      stampedReference.compareAndSet(null, "B", 0, 1);
    }, "t2").start();

    System.out.println(
      "stampedReference.reference=" + stampedReference.getReference() + "\tstampedReference.stamp="
        + stampedReference.getStamp());

  }

}
