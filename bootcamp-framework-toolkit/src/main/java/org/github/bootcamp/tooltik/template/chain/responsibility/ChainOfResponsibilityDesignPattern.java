package org.github.bootcamp.tooltik.template.chain.responsibility;

/**
 * @author zhuling
 */
public class ChainOfResponsibilityDesignPattern {
  public static void main(String[] args) {
    ChainOfResponsibilityTemplate chainOfResponsibilityTemplate =
        ChainOfResponsibilityTemplate.link(
            new RoleChainOfResponsibility(), new ThrottlingChainOfResponsibility(200));
    boolean isChecked = chainOfResponsibilityTemplate.check("royse", "123456");
    System.out.println("ChainOfResponsibilityTemplate check result " + isChecked);
  }
}
