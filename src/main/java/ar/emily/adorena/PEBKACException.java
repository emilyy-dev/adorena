package ar.emily.adorena;

import java.io.Serial;

public class PEBKACException extends RuntimeException {

  @Serial private static final long serialVersionUID = 0L;

  public PEBKACException(final String message) {
    super(message, null, true, false);
  }
}
