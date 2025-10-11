package org.github.bootcamp.tooltik.template.chain.responsibility;

/**
 * @author zhuling
 */
public class RoleChainOfResponsibility extends ChainOfResponsibilityTemplate {
  public boolean check(String email, String password) {
    if (email.equals("admin@example.com")) {
      System.out.println("Hello, admin!");
      return true;
    }
    System.out.println("Hello, user!");
    return checkNext(email, password);
  }
}
