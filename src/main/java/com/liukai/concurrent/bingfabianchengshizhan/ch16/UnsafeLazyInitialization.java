package com.liukai.concurrent.bingfabianchengshizhan.ch16;

/**
 * 16-3 不安全的延迟初始化
 */
public class UnsafeLazyInitialization {

  private static UnsafeLazyInitialization resouce;

  public static UnsafeLazyInitialization getInstance() {
    if (resouce == null) {
      resouce = new UnsafeLazyInitialization();
    }
    return resouce;
  }

}
