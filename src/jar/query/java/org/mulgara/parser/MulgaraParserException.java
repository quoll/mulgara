package org.mulgara.parser;

public class MulgaraParserException extends Exception {

  private static final long serialVersionUID = -7724312286750583118L;

  public MulgaraParserException(String message) {
    super(message);
  }

  public MulgaraParserException(Throwable cause) {
    super(cause.getMessage(), cause);
  }

  public MulgaraParserException(String message, Throwable cause) {
    super(message, cause);
  }

}
