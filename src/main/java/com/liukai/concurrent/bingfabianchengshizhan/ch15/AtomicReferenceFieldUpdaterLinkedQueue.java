package com.liukai.concurrent.bingfabianchengshizhan.ch15;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 15-8 基于 AtomicReferenceFieldUpdater 原子的域更新器队列
 */
public class AtomicReferenceFieldUpdaterLinkedQueue<E> {

  private static final AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterLinkedQueue, Node>
    headUpdater = AtomicReferenceFieldUpdater
    .newUpdater(AtomicReferenceFieldUpdaterLinkedQueue.class, Node.class, "head");

  private static final AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterLinkedQueue, Node>
    tailUpdater = AtomicReferenceFieldUpdater
    .newUpdater(AtomicReferenceFieldUpdaterLinkedQueue.class, Node.class, "tail");

  private static final AtomicReferenceFieldUpdater<Node, Node> nextUpdater
    = AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "next");

  // 稳定状态的执行次数
  private AtomicInteger stabilityExecCount = new AtomicInteger();

  // 中间状态的执行次数
  private AtomicInteger intermediateExecCount = new AtomicInteger();

  // 哨兵节点
  private volatile Node<E> dummy = new Node<>(null, null);

  // 头指针
  private volatile Node<E> head = dummy;

  // 尾指针
  private volatile Node<E> tail = dummy;

  public static void main(String[] args) throws InterruptedException {

    AtomicReferenceFieldUpdaterLinkedQueue<Integer> linkedQueue
      = new AtomicReferenceFieldUpdaterLinkedQueue<>();

    final int tNum = 10;
    final int peerThreadPutNum = 5;
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
      Node<E> currentTail = tail;
      // 尾结点下一个节点
      Node<E> tailNext = currentTail.next;
      // 确保是当前节点
      if (currentTail == tail) {

        if (tailNext != null) {
          // 处于中间状态——即已经有线程执行了一部分操作
          // 此时的线程继续执行接下来的操作，如果是当前线程则继续执行，如果是其他线程则可以代替未完成操作的线程完成操作
          // 不用考虑是否成功，允许失败，因为无论如何其他线程都会代替执行，直到成功为止。
          tailUpdater.compareAndSet(this, currentTail, tailNext);
          intermediateExecCount.incrementAndGet();
          System.out.println(
            Thread.currentThread().getName() + "\t执行中间状态, currentTail.item=" + currentTail.item
              + "\ttailNext.item=" + tailNext.item);

        } else {

          // 处于稳定状态——即未发现其他线程执行操作
          if (nextUpdater.compareAndSet(currentTail, null, newNode)) {
            // 尾部节点指向新元素,不用考虑是否成功，允许失败，因为无论如何其他线程都会代替执行，直到成功为止。
            tailUpdater.compareAndSet(this, currentTail, newNode);
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

  static class Node<E> {

    volatile Node<E> next;

    private E item;

    public Node(E item, Node<E> next) {
      this.item = item;
      this.next = next;
    }

  }

}
