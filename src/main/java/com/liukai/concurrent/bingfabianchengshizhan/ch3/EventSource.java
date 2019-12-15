package com.liukai.concurrent.bingfabianchengshizhan.ch3;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 事件源对象
 * <p>
 * 保存了一些监听器
 */
public class EventSource {

  private List<MyEventListener> listeners = new CopyOnWriteArrayList<>();

  public void registerListener(MyEventListener eventListener) {
    System.out.println(Thread.currentThread().getName() + "\t注册了监听器：" + eventListener);
    listeners.add(eventListener);
  }

  public void updateState() {
    for (MyEventListener listener : listeners) {
      System.out.println(Thread.currentThread().getName() + "线程\t执行更新操作");
      listener.onEvent();
    }
  }

}
