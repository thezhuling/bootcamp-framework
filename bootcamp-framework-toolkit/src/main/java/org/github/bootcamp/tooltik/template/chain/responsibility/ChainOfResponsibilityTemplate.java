package org.github.bootcamp.tooltik.template.chain.responsibility;

/**
 * @author zhuling
 */
public abstract class ChainOfResponsibilityTemplate {
  private ChainOfResponsibilityTemplate next;

  /** Builds chains of objects. */
  public static ChainOfResponsibilityTemplate link(
      ChainOfResponsibilityTemplate first, ChainOfResponsibilityTemplate... chain) {
    ChainOfResponsibilityTemplate head = first;
    for (ChainOfResponsibilityTemplate nextInChain : chain) {
      head.next = nextInChain;
      head = nextInChain;
    }
    return first;
  }

  /** Subclasses will implement this method with concrete checks. */
  public abstract boolean check(String email, String password);

  /** Runs check on the next object in chain or ends traversing if we're in last object in chain. */
  protected boolean checkNext(String email, String password) {
    if (next == null) {
      return true;
    }
    return next.check(email, password);
  }
}
