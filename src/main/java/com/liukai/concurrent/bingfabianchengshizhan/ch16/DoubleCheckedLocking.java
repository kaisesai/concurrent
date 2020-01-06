package com.liukai.concurrent.bingfabianchengshizhan.ch16;

/**
 * 16-7 双重检查加锁
 *
 * @desc 不建议使用此模式，推荐延长初始化占位类模式
 */
public class DoubleCheckedLocking {

  private static volatile DoubleCheckedLocking resource;

  public static DoubleCheckedLocking getInstance() {
    if (resource == null) {
      synchronized (DoubleCheckedLocking.class) {
        if (resource == null) {
          resource = new DoubleCheckedLocking();
        }
      }
    }
    return resource;
  }

}
