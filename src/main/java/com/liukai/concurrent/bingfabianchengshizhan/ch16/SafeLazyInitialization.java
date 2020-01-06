package com.liukai.concurrent.bingfabianchengshizhan.ch16;

/**
 * 16-4 安全的延迟初始化
 */
public class SafeLazyInitialization {

  private static SafeLazyInitialization resouce;

  public static synchronized SafeLazyInitialization getInstance() {
    if (resouce == null) {
      resouce = new SafeLazyInitialization();
    }
    return resouce;
  }

}
