package org.sonatype.nexus.blobstore.api;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * since @3.0
 */
public interface Facet<T>
{

  /**
   * 
   */
  void init(T target) throws Exception;

  /**
   * 
   */
  void start() throws Exception;

  /**
   *
   */
  void stop() throws Exception;

  /**
   *
   */
  void delete() throws Exception;

  /**
   *
   */
  @Documented
  @Target(TYPE)
  @Retention(RUNTIME)
  @interface Exposed
  {
    // marker
  }
}
