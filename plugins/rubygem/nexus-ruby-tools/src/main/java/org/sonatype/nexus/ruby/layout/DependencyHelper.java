package org.sonatype.nexus.ruby.layout;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * helper to collect or merge dependency data from <code>DependencyFile</code>s
 * or extract the dependency data from a given <code>GemspecFile</code>.
 * the remote data from <code>BundlerApiFile</code> is collection of the same
 * dependency data format which can added as well.
 * 
 * after adding all the data, you can retrieve the list of gemnames for which
 * there are dependency data and retrieve them as marshalled stream (same format as
 * <code>DependencyFile</code> or <code>BundlerApiFile</code>).
 * 
 * @author christian
 *
 */
public interface DependencyHelper {
  
  /**
   * add dependency data to instance
   * @param marshalledDependencyData stream of the marshalled "ruby" data
   */
  void add(InputStream marshalledDependencyData);
  
  /**
   * add dependency data to instance from a rzipped gemspec object.
   * @param marshalledDependencyData rzipped stream of the marshalled gemspec object
   */
  void addGemspec(InputStream gemspec);

  /**
   * freezes the instance - no more added of data is allowed - and returns
   * the list of gemnames for which dependency data was added.
   * @return String[] of gemnames
   */
  String[] getGemnames();
  
  /**
   * marshal ruby object with dependency data for the given gemname.
   * @param gemname
   * @return ByteArrayInputStream of binary data
   */
  ByteArrayInputStream getInputStreamOf(String gemname);
  
  /**
   * marshal ruby object with dependency data for all the dependency data,
   * either with or without duplicates.
   * @return ByteArrayInputStream of binary data
   */
  ByteArrayInputStream getInputStream(boolean unique);
}