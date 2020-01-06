package com.liukai.concurrent.bingfabianchengshizhan.ch16;

/**
 * 16-5 提前初始化
 */
public class EagerInitialization {

  private static EagerInitialization resoure = new EagerInitialization();

  public static EagerInitialization getInstance() {
    return resoure;
  }

}
