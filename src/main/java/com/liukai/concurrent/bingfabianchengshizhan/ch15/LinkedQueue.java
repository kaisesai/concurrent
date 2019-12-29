package com.liukai.concurrent.bingfabianchengshizhan.ch15;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 15-7 Michael-Scott（Michael and Scott，1996）非阻塞算法中的插入算法
 */
public class LinkedQueue<E> {

  // 稳定状态的执行次数
  private AtomicInteger stabilityExecCount = new AtomicInteger();

  // 中间状态的执行次数
  private AtomicInteger intermediateExecCount = new AtomicInteger();

  // 哨兵节点
  private Node<E> dummy = new Node<>(null, null);

  // 头指针
  private AtomicReference<Node<E>> head = new AtomicReference<>(dummy);

  // 尾指针
  private AtomicReference<Node<E>> tail = new AtomicReference<>(dummy);

  public static void main(String[] args) throws InterruptedException {
    LinkedQueue<Integer> linkedQueue = new LinkedQueue<>();

    final int tNum = 10;
    final int peerThreadPutNum = 1;
    for (int i = 0; i < tNum; i++) {
      new Thread(() -> {
        for (int j = 0; j < peerThreadPutNum; j++) {
          linkedQueue.put(j);
        }
      }, "线程" + i).start();
    }

    TimeUnit.SECONDS.sleep(1);
    System.out.println(
      "所有执行put操作的次数：" + tNum * peerThreadPutNum + "\t队列执行的稳定态操作次数：" + linkedQueue.stabilityExecCount
        .get() + "\t中间态操作次数：" + linkedQueue.intermediateExecCount.get());
  }

  public void put(E e) {

    Node<E> newNode = new Node<>(e, null);
    while (true) {
      // 当前尾结点
      Node<E> currentTail = tail.get();
      // 尾结点下一个节点
      Node<E> tailNext = currentTail.next.get();
      // 确保是当前节点
      if (currentTail == tail.get()) {

        if (tailNext != null) {
          // 处于中间状态——即已经有线程执行了一部分操作
          // 此时的线程继续执行接下来的操作，如果是当前线程则继续执行，如果是其他线程则可以代替未完成操作的线程完成操作
          // 不用考虑是否成功，允许失败，因为无论如何其他线程都会代替执行，直到成功为止。
          tail.compareAndSet(currentTail, tailNext);
          intermediateExecCount.incrementAndGet();
          System.out.println(
            Thread.currentThread().getName() + "\t执行中间状态, currentTail.item=" + currentTail.item
              + "\ttailNext.item=" + tailNext.item);

        } else {
          // 处于稳定状态——即未发现其他线程执行操作
          if (currentTail.next.compareAndSet(null, newNode)) {
            // 尾部节点指向新元素,不用考虑是否成功，允许失败，因为无论如何其他线程都会代替执行，直到成功为止。
            tail.compareAndSet(currentTail, newNode);
            System.out.println(
              Thread.currentThread().getName() + "\t执行稳定状态, currentTail.item=" + currentTail.item
                + "\tnewNode.item=" + newNode.item);
            stabilityExecCount.incrementAndGet();
            return;
          }
        }
      }
    }

  }

  private static class Node<E> {

    private E item;

    private AtomicReference<Node<E>> next;

    public Node(E item, Node<E> next) {
      this.item = item;
      this.next = new AtomicReference<>(next);
    }

  }

}
