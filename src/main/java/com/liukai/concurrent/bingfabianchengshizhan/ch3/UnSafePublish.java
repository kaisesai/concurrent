package com.liukai.concurrent.bingfabianchengshizhan.ch3;

import java.util.concurrent.TimeUnit;

/**
 * 不安全的发布对象
 */
public class UnSafePublish {

  private int state;

  public UnSafePublish(EventSource eventSource) {
    eventSource.registerListener(this::doSomething);

    // 这里模拟创建构造器时，由于多线程的竞争，导致该构造器方法未执行完毕，this就被逸出引发的安全性问题
    try {
      TimeUnit.SECONDS.sleep(2);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // 初始化为1
    this.state = 1;
    System.out
      .println(Thread.currentThread().getName() + "线程\tUnSafePublish 初始化完毕，state=" + this.state);
  }

  public static void main(String[] args) {

    EventSource eventSource = new EventSource();

    new Thread(() -> {
      new UnSafePublish(eventSource);
    }, "t1").start();

    try {
      TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // 执行更新事件
    eventSource.updateState();

    try {
      TimeUnit.SECONDS.sleep(5);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void doSomething() {
    this.state = 2;
    System.out
      .println(Thread.currentThread().getName() + "线程\t执行doSomething方法，state=" + this.state);
  }

}
