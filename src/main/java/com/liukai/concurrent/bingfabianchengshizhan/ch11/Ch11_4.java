package com.liukai.concurrent.bingfabianchengshizhan.ch11;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 缩小锁的范围（快进快出）
 */
public class Ch11_4 {

  /*
    11.4.1 缩小锁的范围（快进快出）
   */

  /**
   * 缩小锁的范围优化。
   * <p>
   * 注意：这里我们只是演示了下缩小锁范围的示例，还可以线程安全性（锁相关的操作）交给其他类来进一步提升它的性能。比如使用 ConcurrentHashMap
   */
  public static class BetterAttributeStore {

    private final Map<String, String> attributes = new HashMap<>();

    /**
     * 用户地址匹配方法
     *
     * @param name
     * @param regexp
     * @return
     */
    public boolean userLocationMatches(String name, String regexp) {
      String key = "users." + name + ".location";
      String location;
      synchronized (this) {
        location = attributes.get(key);
      }
      if (location == null) {
        return false;
      } else {
        return Pattern.matches(regexp, location);
      }

    }

  }

  /**
   * 这是一个需要优化的线程安全类。方法中很多操作是不需要同步的。
   */
  public static class AttributeStore {

    private final Map<String, String> attributes = new HashMap<>();

    /**
     * 用户地址匹配方法
     *
     * @param name
     * @param regexp
     * @return
     */
    public synchronized boolean userLocationMatches(String name, String regexp) {
      String key = "users." + name + ".location";
      String location = attributes.get(key);
      if (location == null) {
        return false;
      } else {
        return Pattern.matches(regexp, location);
      }

    }

  }

}
