package io.eldermael.java.libs;

public enum ProcessResult {

  SUCCESS,
  ERROR;

  public static ProcessResult fromExitCode(int exitCode) {
    if (exitCode == 0) {
      return SUCCESS;
    }

    return ERROR;
  }
}
