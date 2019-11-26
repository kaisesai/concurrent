package com.liukai.concurrent.ch1;

/**
 * 单例模式
 */
public class SingletonDemon {

  /*
    volatile 语义是禁止指令重排序
    getinstance中的代码 singletonDemon = new SingletonDemon(); 可以分解为三行伪代码
    memory = allocate();  // 1:分配对象的内存空间
    ctorInstance(memory);  // 2:初始化对象
    instance = memory;   // 3:设置instance指向刚分配的内存地址

    伪代码中的 2和3是不存在数据依赖关系的，因此有些JIT编译器会将这些指令重排序，导致变成以下伪代码
    memory = allocate();  // 1:分配对象的内存空间
    instance = memory;   // 3:设置instance指向刚分配的内存地址
    ctorInstance(memory);  // 2:初始化对象

    这个在单线程模式下是没有问题的，但是在多线程中就会出现问题，因为此时多线程中获取到的  singletonDemon 这个对象虽然已经分配了内存空间，但是并没有初始化完成！
   */
  private static volatile SingletonDemon singletonDemon = null;

  private SingletonDemon() {
    System.out.println("这是一个单例模式\t" + Thread.currentThread().getName());
  }

  public static SingletonDemon getInstance() {
    if (singletonDemon == null) {
      synchronized (SingletonDemon.class) {
        if (singletonDemon == null) {
          singletonDemon = new SingletonDemon();
        }
      }
    }
    return singletonDemon;
  }

  public static void main(String[] args) {

    // 运行1000个线程获取单例
    for (int i = 0; i < 10; i++) {
      new Thread(() -> {
        SingletonDemon.getInstance();
      }, String.valueOf(i)).start();
    }
  }

}
