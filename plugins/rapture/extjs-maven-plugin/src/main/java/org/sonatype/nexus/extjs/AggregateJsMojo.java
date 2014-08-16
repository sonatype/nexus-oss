/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.extjs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_RESOURCES;

/**
 * Aggregate javascript sources in order as required by ExtJS.
 *
 * @since 3.0
 */
@Mojo(name="aggregate-js", defaultPhase = PROCESS_RESOURCES)
public class AggregateJsMojo
    extends AbstractMojo
{
  public static final String[] DEFAULT_INCLUDES = { "**/*.js" };

  @Parameter(required = true)
  private File sourceDirectory;

  @Parameter
  private String[] includes;

  @Parameter
  private String[] excludes;

  @Parameter(required = true)
  private File outputFile;

  @Parameter
  private String namespace;

  @Parameter(property = "extjs.warnings", defaultValue = "false")
  private boolean warnings;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      doExecute();
    }
    catch (Exception e) {
      throw new MojoExecutionException(e.toString(), e);
    }
  }

  private void doExecute() throws Exception {
    DirectoryScanner files = new DirectoryScanner();
    files.setBasedir(sourceDirectory);
    if (includes == null || includes.length == 0) {
      files.setIncludes(DEFAULT_INCLUDES);
    }
    else {
      files.setIncludes(includes);
    }
    files.setExcludes(excludes);
    files.addDefaultExcludes();

    ClassDefScanner scanner = new ClassDefScanner(getLog());
    scanner.setWarnings(warnings);
    if (namespace == null) {
      getLog().warn("Namespace is not configured; will be unable to detect and resolve MVC references");
    }
    else {
      scanner.setNamespace(namespace);
    }
    List<ClassDef> classes = scanner.scan(files);

    Writer output = new BufferedWriter(new FileWriter(outputFile));
    try {
      for (ClassDef def : classes) {
        write(def.getSource(), output);
      }
    }
    finally {
      output.close();
    }
  }

  private void write(final File source, final Writer output) throws IOException {
    Reader input = new BufferedReader(new FileReader(source));
    try {
      IOUtil.copy(input, output);
    }
    finally {
      input.close();
    }
    output.write('\n');
  }
}
