package com.liukai.concurrent.bingfabianchengshizhan.ch12;

import java.util.concurrent.Semaphore;

/**
 * 有界的缓存
 * <p>
 * 使用信号量技术来实现缓存的有界属性和阻塞操作
 * </p>
 * <p>
 * 注意实际上有界队列应该使用 ArrayBlockingQueue 或者 LinkedBlockingQueue
 *
 * @param <E> 元素
 */
public class BoundedBuffer<E> {

  // 可用的元素信号量
  private final Semaphore availableItems;

  // 可用的空间信号量
  private final Semaphore availableSpaces;

  // 元素数组
  private final E[] items;

  // put操作的索引值
  private int putPostion = 0;

  // take操作的索引值
  private int takePostion = 0;

  public BoundedBuffer(int capacity) {
    // 初始值为0
    this.availableItems = new Semaphore(0);
    // 初始值为容量大小
    this.availableSpaces = new Semaphore(capacity);
    this.items = (E[]) new Object[capacity];
  }

  public boolean isEmpty() {
    // 可用元素信号量的许可是否为0
    return availableItems.availablePermits() == 0;
  }

  public boolean isFull() {
    // 可用空间信号量的许可是否为0
    return availableSpaces.availablePermits() == 0;
  }

  public void put(E e) throws InterruptedException {
    // 可用空间信号量许可减一
    availableSpaces.acquire();
    // 执行新增数据操作
    doInsert(e);
    // 可用元素信号量的许可加一
    availableItems.release();
  }

  public E take() throws InterruptedException {
    // 可用元素的信号量许可减一
    availableItems.acquire();
    // 执行删除元素操作
    E e = doExtract();
    // 可用空间信号量许可加一
    availableSpaces.release();
    return e;
  }

  private synchronized E doExtract() {
    int i = takePostion;
    // 取出当前索引值的元素
    E tmp = items[i];
    // 将取出的元素置为null
    items[i] = null;
    takePostion = (++i == items.length ? 0 : i);
    // 注意个方法是先进先出方法，记录take的索引值，用于获取下一个数组中的元素值
    return tmp;
  }

  private synchronized void doInsert(E e) {
    int i = putPostion;
    items[i] = e;
    // 如果当前索引值等于数组的长度，则将putPosition值设置为0，循环添加，
    // 注意这里可能存在对象泄露出去。其他地方有可能使用已经被覆盖的元素对象
    putPostion = (++i == items.length ? 0 : i);
  }

}
