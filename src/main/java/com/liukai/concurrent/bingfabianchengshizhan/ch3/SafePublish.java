package com.liukai.concurrent.bingfabianchengshizhan.ch3;

/**
 * 安全的发布对象
 * <p>
 * 通过工厂方法发布对象
 * </p>
 */
public class SafePublish {

  private final MyEventListener eventListener;

  private int state;

  private SafePublish() {
    // 构造方法只初始化属性，不会发布this对象！所以一定是安全的
    eventListener = this::doSomething;
    // 初始化为1
    this.state = 1;
    System.out
      .println(Thread.currentThread().getName() + "线程\tSafePublish 初始化完毕，state=" + this.state);
  }

  public static SafePublish getInstance(EventSource eventSource) {
    SafePublish safePublish = new SafePublish();// 先创建对象
    eventSource.registerListener(safePublish.eventListener);// 再发布
    return safePublish;
  }

  public static void main(String[] args) {
    EventSource eventSource = new EventSource();

    new Thread(() -> {
      getInstance(eventSource);
    }, "t1").start();

    // 无法模拟不安全的逸出错误，因为对象初始化，与执行发布分开执行了，所以一定可以是安全的操作
    // try {
    //   TimeUnit.SECONDS.sleep(1);
    // } catch (InterruptedException e) {
    //   e.printStackTrace();
    // }
    // 执行更新事件
    eventSource.updateState();
  }

  private void doSomething() {
    this.state = 2;
    System.out
      .println(Thread.currentThread().getName() + "线程\t执行doSomething方法，state=" + this.state);
  }

}
