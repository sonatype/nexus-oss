package org.sonatype.nexus.test.hamcrest;

import java.io.File;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;


public final class FileMatchers {
  private FileMatchers() {
  }

  public static Matcher<File> exists() {
    return new BaseMatcher<File>() {
      public boolean matches(Object item) {
        return ((File) item).exists();
      }

      public void describeTo(Description description) {
        description.appendText("file or directory exists");
      }

    };
  }

}
