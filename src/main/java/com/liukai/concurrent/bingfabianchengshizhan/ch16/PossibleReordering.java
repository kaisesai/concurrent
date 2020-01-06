package com.liukai.concurrent.bingfabianchengshizhan.ch16;

/**
 * 16-1 指令重排序
 */
public class PossibleReordering {

  static int x = 0, y = 0;

  static int a = 0, b = 0;

  public static void main(String[] args) throws InterruptedException {

    Thread t1 = new Thread(() -> {

      // synchronized (PossibleReordering.class) {
      a = 1;
      x = b;
      // }

    }, "t1");

    Thread t2 = new Thread(() -> {

      // synchronized (PossibleReordering.class) {
      b = 1;
      y = a;
      // }

    }, "t2");

    t1.start();
    t2.start();
    t1.join();
    t2.join();

    System.out.println("元素x 与 y的组合：(" + x + "," + y + ")");

  }

}
