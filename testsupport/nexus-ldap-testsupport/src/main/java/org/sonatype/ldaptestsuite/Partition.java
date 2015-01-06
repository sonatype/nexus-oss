/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.ldaptestsuite;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Partition
{
  private final String name;

  private final String suffix;

  private final List<String> indexedAttributes;

  private final List<String> rootEntryClasses;

  private final File ldifFile;

  public Partition(final String name,
                   final String suffix,
                   final List<String> indexedAttributes,
                   final List<String> rootEntryClasses,
                   final File ldifFile)
  {
    this.name = checkNotNull(name);
    this.suffix = checkNotNull(suffix);
    this.indexedAttributes = indexedAttributes;
    this.rootEntryClasses = rootEntryClasses;
    this.ldifFile = checkNotNull(ldifFile);
    checkArgument(this.ldifFile.isFile(), "The LDIF file %s is missing", this.ldifFile);
  }

  public String getName() {
    return name;
  }

  public String getSuffix() {
    return suffix;
  }

  public List<String> getIndexedAttributes() {
    return indexedAttributes;
  }

  public List<String> getRootEntryClasses() {
    return rootEntryClasses;
  }

  public File getLdifFile() {
    return ldifFile;
  }

  public static Builder builder() {
    return new Builder();
  }

  // ==

  public static class Builder
  {
    private String name;

    private String suffix;

    private List<String> indexedAttributes = Lists.newArrayList();

    private List<String> rootEntryClasses = Lists.newArrayList();

    private File ldifFile;

    public Partition build() {
      return new Partition(name, suffix, indexedAttributes, rootEntryClasses, ldifFile);
    }

    public Builder withNameAndSuffix(final String name, final String suffix) {
      this.name = checkNotNull(name);
      this.suffix = checkNotNull(suffix);
      return this;
    }

    public Builder withIndexedAttributes(final String... attributes) {
      indexedAttributes.addAll(Arrays.asList(attributes));
      return this;
    }

    public Builder withRootEntryClasses(final String... classes) {
      rootEntryClasses.addAll(Arrays.asList(classes));
      return this;
    }

    public Builder withLdifFile(final File file) {
      this.ldifFile = checkNotNull(file);
      checkArgument(ldifFile.isFile(), "LDIF file %s not found", ldifFile.getAbsoluteFile());
      return this;
    }
  }
}
