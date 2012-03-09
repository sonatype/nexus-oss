package de.is24.nexus.yum.alias;

@SuppressWarnings("serial")
public class AliasNotFoundException extends Exception {
  public AliasNotFoundException(String message) {
    super(message);
  }

  public AliasNotFoundException(Throwable cause) {
    super(cause);
  }

  public AliasNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
