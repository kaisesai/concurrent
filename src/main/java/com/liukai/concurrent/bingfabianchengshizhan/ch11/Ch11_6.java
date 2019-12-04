package com.liukai.concurrent.bingfabianchengshizhan.ch11;

import java.util.HashSet;
import java.util.Set;

/**
 * 锁分解
 */
public class Ch11_6 {

  public static void main(String[] args) {
    String str = "abc";
    // String str= new String("abc");
    changeValueStr(str);
    System.out.println(str);
    System.out.println(str.hashCode());
  }

  private static void changeValueStr(String str) {
    str = "xxx";
  }

  /**
   * 采用锁分解技术，多两个独立的状态变量分别使用各自的锁
   */
  public static class BetterServerStatus {

    public final Set<String> users = new HashSet<>();

    public final Set<String> queries = new HashSet<>();

    public void addUser(String u) {
      synchronized (users) {
        users.add(u);
      }
    }

    public void addQuery(String q) {
      synchronized (queries) {
        queries.add(q);
      }
    }

    /**
     * 类中有两个状态变量，并且相互独立，不存在依赖性，使用同一把锁
     */
    public static class ServerStatus {

      public final Set<String> users = new HashSet<>();

      public final Set<String> queries = new HashSet<>();

      public synchronized void addUser(String u) {
        users.add(u);
      }

      public synchronized void addQuery(String q) {
        queries.add(q);
      }

    }

  }

}
