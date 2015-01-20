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
import com.google.common.io.Files;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class LdapServerConfiguration
{
  private final File workingDirectory;

  private final List<Partition> partitions;

  private final boolean deleteOnStart;

  private final int port;

  private final boolean enableSsl;

  private final List<String> additionalSchemas;

  private final String saslHost;

  private final String saslPrincipal;

  private final String saslSearchBaseDn;

  private final List<String> saslRealms;

  public LdapServerConfiguration(final File workingDirectory,
                                 final List<Partition> partitions,
                                 final boolean deleteOnStart,
                                 final int port,
                                 final boolean enableSsl,
                                 final List<String> additionalSchemas,
                                 final String saslHost,
                                 final String saslPrincipal,
                                 final String saslSearchBaseDn,
                                 final List<String> saslRealms)
  {
    this.workingDirectory = checkNotNull(workingDirectory);
    this.partitions = checkNotNull(partitions);
    this.deleteOnStart = deleteOnStart;
    this.port = port;
    this.enableSsl = enableSsl;
    this.additionalSchemas = additionalSchemas;
    this.saslHost = saslHost;
    this.saslPrincipal = saslPrincipal;
    this.saslSearchBaseDn = saslSearchBaseDn;
    this.saslRealms = saslRealms;
  }

  public File getWorkingDirectory() {
    return workingDirectory;
  }

  public List<Partition> getPartitions() {
    return partitions;
  }

  public boolean isDeleteOnStart() {
    return deleteOnStart;
  }

  public int getPort() {
    return port;
  }

  public boolean isEnableSsl() {
    return enableSsl;
  }

  public List<String> getAdditionalSchemas() {
    return additionalSchemas;
  }

  public String getSaslHost() {
    return saslHost;
  }

  public String getSaslPrincipal() {
    return saslPrincipal;
  }

  public String getSaslSearchBaseDn() {
    return saslSearchBaseDn;
  }

  public List<String> getSaslRealms() {
    return saslRealms;
  }

  public static Builder builder() {
    return new Builder();
  }

  // ==

  public static class Builder
  {
    private File workingDirectory;

    private List<Partition> partitions = Lists.newArrayList();

    private boolean deleteOnStart = true;

    private int port = -1;

    private boolean enableSsl = false;

    private List<String> additionalSchemas = Lists.newArrayList();

    private String saslHost;

    private String saslPrincipal;

    private String saslSearchBaseDn;

    private List<String> saslRealms;

    public LdapServerConfiguration build() {
      validate();
      return new LdapServerConfiguration(
          workingDirectory,
          partitions,
          deleteOnStart,
          port,
          enableSsl,
          additionalSchemas,
          saslHost,
          saslPrincipal,
          saslSearchBaseDn,
          saslRealms
      );
    }

    private void validate() {
      checkNotNull(workingDirectory);
      checkNotNull(partitions);
    }

    public Builder withTempWorkingDirectory() {
      this.workingDirectory = Files.createTempDir();
      return this;
    }

    public Builder withWorkingDirectory(final File workingDirectory) {
      checkNotNull(workingDirectory);
      this.workingDirectory = workingDirectory;
      return this;
    }

    public Builder withPartitions(final Partition... partitions) {
      this.partitions.addAll(Arrays.asList(partitions));
      return this;
    }

    public Builder withDeleteOnStart(final boolean deleteOnStart) {
      this.deleteOnStart = deleteOnStart;
      return this;
    }

    public Builder withPort(final int port) {
      checkArgument(port >= 1);
      this.port = port;
      return this;
    }

    public Builder withSSL() {
      this.enableSsl = true;
      return this;
    }

    public Builder withAdditionalSchemas(final String... schemas) {
      this.additionalSchemas = Lists.newArrayList(schemas);
      return this;
    }

    public Builder withSasl(final String saslHost,
                            final String saslPrincipal,
                            final String saslSearchBaseDn,
                            final String... saslRealms)
    {
      this.saslHost = checkNotNull(saslHost);
      this.saslPrincipal = checkNotNull(saslPrincipal);
      this.saslSearchBaseDn = checkNotNull(saslSearchBaseDn);
      this.saslRealms = Lists.newArrayList(saslRealms);
      return this;
    }
  }
}
