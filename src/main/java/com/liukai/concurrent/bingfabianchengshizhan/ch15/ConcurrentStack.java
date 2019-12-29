package com.liukai.concurrent.bingfabianchengshizhan.ch15;

import org.junit.runner.notification.RunListener;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 15-6 使用 Treiber 算法（Treiber,1986）构造的非阻塞栈
 *
 * @param <E>
 */
@RunListener.ThreadSafe
public class ConcurrentStack<E> {

  // 栈顶元素
  private AtomicReference<Node<E>> top = new AtomicReference<>();

  public static void main(String[] args) throws InterruptedException {
    ConcurrentStack<Integer> concurrentStack = new ConcurrentStack<>();

    for (int i = 0; i < 10; i++) {
      int finalI = i;
      new Thread(() -> {
        concurrentStack.push(finalI);
      }, "push线程" + i).start();
    }

    TimeUnit.SECONDS.sleep(1);
    System.out.println(Thread.currentThread().getName() + "\tconcurrentStack: " + concurrentStack);

    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        Integer pop = concurrentStack.pop();
        System.out.println(Thread.currentThread().getName() + "\tpop: " + pop);
      }, "pop线程" + i).start();
    }

    TimeUnit.SECONDS.sleep(1);

    System.out.println(Thread.currentThread().getName() + "\tconcurrentStack: " + concurrentStack);
  }

  public void push(E e) {
    Node<E> node = new Node<>(e);
    Node<E> oldValue;
    while (true) {
      // 获取当前栈顶元素
      oldValue = top.get();
      node.next = oldValue;
      // 比较并设值,成功则退出，不成功则继续尝试
      if (top.compareAndSet(oldValue, node)) {
        return;
      }
    }
  }

  public E pop() {
    Node<E> oldValue;
    while (true) {
      // 获取当前栈顶元素
      oldValue = top.get();
      if (oldValue == null) {
        return null;
      }
      // 比较并设值，移出栈顶元素
      if (top.compareAndSet(oldValue, oldValue.next)) {
        return oldValue.e;
      }
    }
  }

  @Override
  public String toString() {
    Node<E> eNode = top.get();
    if (eNode == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    while (eNode != null) {
      sb.append(eNode.e);
      eNode = eNode.next;

      if (eNode != null) {
        sb.append(",");
      }
    }

    return sb.toString();
  }

  private static class Node<E> {

    private final E e;

    private Node<E> next;

    public Node(E e) {
      this.e = e;
    }

  }

}
