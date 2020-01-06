package com.liukai.concurrent.bingfabianchengshizhan.ch16;

import java.util.HashMap;
import java.util.Map;

/**
 * 16-8 不可变对象的初始化安全性
 */
public class SafeStates {

  private final Map<String, String> states;

  public SafeStates() {
    this.states = new HashMap<>();
    this.states.put("alaska", "AK");
    this.states.put("alabama", "AL");

  }

  public String getAbbreviation(String s) {
    return states.get(s);
  }

}
