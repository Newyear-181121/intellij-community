// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package ny.log;

import java.util.*;

/**
 * 制作一个启动工具记录类类， 使用的时候，会记录在启动的时候声明的顺序，并记录调用到这里之前的堆栈， 可以选择打印相关的堆栈
 * @author New Year
 * @since 2022/7/6 15:30
 */
public class StartUtils {

  public static int key = 0;

  public static Map<Integer, Key> keys = new HashMap<>();
  public static Map<Key, Object[]> stackTraces = new HashMap<>();


  public static void log() {
    key++;
    stackTraces.put(new Key(key), Thread.currentThread().getStackTrace());
  }

  public static void log(String msg) {
    log(true, msg, null);
  }

  public static void log(String msg, Object ... str) {
     log(true, msg, str);
  }

  public static void log(boolean out, String msg, Object ... str) {
    key++;
    stackTraces.put(new Key(key, msg, str), Thread.currentThread().getStackTrace());
    if (out) {
      outEnd();
    }
  }

  public static Object[] get(int key) {
    if (keys.containsKey(key)) {
      return stackTraces.get(keys.get(key));
    }
    return null;
  }

  public static void out(int key) {
    if (keys.containsKey(key)) {
      System.out.println(keys.get(key));
      Arrays.stream(stackTraces.get(keys.get(key))).forEach(System.out::println);
      System.out.println();
    }
  }

  /**
   * 打印最后一个日志， key 从 1开始， 1-n
   */
  public static void outEnd() {
    out(stackTraces.size());
  }



  public static class Key{
    int key;
    String msg;
    /**
     * 参数, 记录一些想要记录的参数
     */
    List<Object> strs;

    Key(int key) {
      keys.put(key, this);
      this.key = key;
    }

    Key(int key, String msg) {
      this(key);
      this.msg = msg;
    }

    private Key(int key, String msg, Object ... str) {
      this(key, msg);
      this.strs = new ArrayList<>();
      strs.addAll(List.of(str));
    }

    @Override
    public String toString() {
      return "Key{" +
             "key=" + key +
             ", msg='" + msg + '\'' +
             ", strs=" + strs +
             '}';
    }
  }

  /**
   * 获取堆栈信息
   * @param current 是否打印当前行
   * @return
   */
  public static String getStackTrace(boolean current) {
    StackTraceElement[] elements = Thread.currentThread().getStackTrace();
    StringBuilder sb = new StringBuilder();
    // 从一开始，应该就是不打印这个方法本身了
    for (int i = current ? 0 : 2; i < elements.length; i++) {
      StackTraceElement s = elements[i];
      sb.append(s.getClassName()).append(".")
        .append(s.getMethodName()).append("(")
        .append(s.getFileName()).append(":")
        .append(s.getLineNumber()).append(")").append("\n");
    }
    return sb.toString();
  }
}
