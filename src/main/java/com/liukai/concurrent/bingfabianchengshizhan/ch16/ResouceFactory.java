package com.liukai.concurrent.bingfabianchengshizhan.ch16;

/**
 * 16-6 延长初始化占位类模式
 */
public class ResouceFactory {

  public static ResourceHolder getResource() {
    return ResourceHolder.resourceHolder;
  }

  public static class ResourceHolder {

    private static ResourceHolder resourceHolder = new ResourceHolder();

    private ResourceHolder() {
    }

  }

}
