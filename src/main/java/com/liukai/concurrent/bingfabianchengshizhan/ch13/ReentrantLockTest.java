package com.liukai.concurrent.bingfabianchengshizhan.ch13;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockTest {

  // 通过trylock来避免死锁
  public boolean transferMoney(Account fromAccount, Account toAccount, int amount, long timeout,
                               TimeUnit timeUnit) throws InterruptedException {
    Random random = new Random();
    long fixedDelay = random.nextLong();
    long randMod = random.nextInt(1000);// 随机
    long stopTime = System.nanoTime() + timeUnit.toNanos(timeout);

    // 轮询获取锁
    while (true) {
      if (fromAccount.lock.tryLock()) {
        try {
          if (toAccount.lock.tryLock()) {
            try {
              int amount1 = fromAccount.amount;
              int amount2 = toAccount.amount;
              if (amount1 > amount2) {
                fromAccount.amount = 0;
                toAccount.amount += amount1;
                return true;
              } else {
                throw new RuntimeException("fromAccount 账户资金不足，无法转账！\tamount=" + amount1);
              }
            } finally {
              toAccount.lock.unlock();
            }
          }
        } finally {
          fromAccount.lock.unlock();
        }
      }
      if (System.nanoTime() > stopTime) {
        // 超时则平缓的退出
        return false;
      }
      // 确保每次定时的时间不一样
      TimeUnit.NANOSECONDS.sleep(fixedDelay + random.nextLong() % randMod);
    }
  }

  static class Account {

    private ReentrantLock lock = new ReentrantLock();

    private String name;

    private int amount;

    public Account(String name, int amount) {
      this.name = name;
      this.amount = amount;
    }

  }

}
