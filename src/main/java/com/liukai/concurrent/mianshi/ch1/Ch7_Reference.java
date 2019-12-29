package com.liukai.concurrent.mianshi.ch1;

import java.lang.ref.*;

public class Ch7_Reference {

  public static void main(String[] args) {

    // strongReference();

    // softReferenceEnough();

    // softReferenceNotEnough();

    // weakReference();

    // referenceQueue();

    phantomReference();
  }

  /**
   * 虚引用：任何情况下get方法都会返回null，需要配合引用队列使用
   */
  private static void phantomReference() {

    Object o1 = new Object();
    // WeakReference<Object> reference = new WeakReference<>(o1);
    ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
    Reference<Object> reference = new PhantomReference<>(o1, referenceQueue);

    System.out.println(o1);
    System.out.println(reference.get());
    System.out.println(referenceQueue.poll());
    o1 = null;

    System.gc();

    System.out.println("===============gc后================");
    System.out.println(o1);
    System.out.println(referenceQueue.poll());
    System.out.println(reference.get());

  }

  /**
   * 引用队列：配合引用使用
   */
  private static void referenceQueue() {

    Object o1 = new Object();
    // WeakReference<Object> reference = new WeakReference<>(o1);
    ReferenceQueue<Object> referenceQueue = new ReferenceQueue<>();
    Reference<Object> reference = new WeakReference<>(o1, referenceQueue);

    System.out.println(o1);
    System.out.println(reference.get());
    System.out.println(referenceQueue.poll());
    o1 = null;

    System.gc();

    System.out.println("===============gc后================");
    System.out.println(o1);
    System.out.println(referenceQueue.poll());
    System.out.println(reference.get());

  }

  /**
   * 弱引用：只要有gc就会被回收
   */
  private static void weakReference() {
    Object o1 = new Object();
    WeakReference<Object> reference = new WeakReference<>(o1);

    System.out.println(o1);
    System.out.println(reference.get());
    o1 = null;

    System.gc();

    System.out.println("===============gc后================");
    System.out.println(o1);
    System.out.println(reference.get());

  }

  /**
   * 软引用：内存不足时会对软引用进行垃圾回收
   */
  private static void softReferenceNotEnough() {
    Object o1 = new Object();
    SoftReference<Object> softReference = new SoftReference<>(o1);

    System.out.println(o1);
    System.out.println(softReference.get());
    o1 = null;

    try {
      System.out.println();
      byte[] bytes = new byte[30 * 1024 * 1024];
    } finally {
      System.out.println(softReference.get());
    }

  }

  private static void softReferenceEnough() {
    Object o1 = new Object();
    SoftReference<Object> softReference = new SoftReference<>(o1);

    System.out.println(o1);
    System.out.println(softReference.get());

    o1 = null;
    System.gc();
    System.out.println();

    System.out.println(softReference.get());
  }

  /**
   * 强引用：无论内存是否满，都不会回收对象引用（死了都不回收）
   */
  private static void strongReference() {
    Object o1 = new Object();
    Object o2 = o1;

    o1 = null;
    System.out.println(o2);
    // 垃圾回收
    System.gc();
    System.out.println(o2);
  }

}
