package org.mulgara.parser;

public class MulgaraLexerException extends Exception {

  private static final long serialVersionUID = -5853073510057952109L;

  public MulgaraLexerException(String message) {
    super(message);
  }

  public MulgaraLexerException(Throwable cause) {
    super(cause.getMessage(), cause);
  }

  public MulgaraLexerException(String message, Throwable cause) {
    super(message, cause);
  }

}
