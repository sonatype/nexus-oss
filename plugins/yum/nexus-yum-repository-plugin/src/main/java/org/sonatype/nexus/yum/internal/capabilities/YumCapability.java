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

package org.sonatype.nexus.yum.internal.capabilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.capability.support.CapabilitySupport;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.plugins.capabilities.Evaluable;
import org.sonatype.nexus.yum.YumRegistry;
import org.sonatype.nexus.yum.internal.task.CommandLineExecutor;

import org.jetbrains.annotations.NonNls;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * YUM capability.
 *
 * @since yum 3.0
 */
@Named(YumCapabilityDescriptor.TYPE_ID)
public class YumCapability
    extends CapabilitySupport<YumCapabilityConfiguration>
{
  @NonNls
  public static final String NL = System.getProperty("line.separator");

  private final YumRegistry yumRegistry;

  private final CommandLineExecutor commandLineExecutor;

  private String binariesLog;

  private String binariesNotAvailableMessage;

  @Inject
  public YumCapability(final YumRegistry yumRegistry,
                       final CommandLineExecutor commandLineExecutor)
  {
    this.yumRegistry = checkNotNull(yumRegistry);
    this.commandLineExecutor = checkNotNull(commandLineExecutor);
  }

  @Override
  protected YumCapabilityConfiguration createConfig(final Map<String, String> properties) throws Exception {
    return new YumCapabilityConfiguration(properties);
  }

  @Override
  protected void onActivate(final YumCapabilityConfiguration config) throws Exception {
    yumRegistry.setMaxNumberOfParallelThreads(config.maxNumberParallelThreads());
    yumRegistry.setCreaterepoPath(config.getCreaterepoPath());
    yumRegistry.setMergerepoPath(config.getMergerepoPath());
  }

  /**
   * Only activate if create/mergerepo are available (running with --version executes successfully).
   *
   * @since 2.11
   */
  @Override
  public Condition activationCondition() {
    return conditions().capabilities().evaluable(
        new Evaluable()
        {
          @Override
          public boolean isSatisfied() {
            StringBuilder sb = new StringBuilder();
            binariesNotAvailableMessage = null;

            sb.append("\"createrepo\" version:").append(NL);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
              if (commandLineExecutor.exec(getConfig().getCreaterepoPath() + " --version", baos, baos) != 0) {
                binariesNotAvailableMessage = "\"createrepo\"";
              }
              sb.append(new String(baos.toByteArray()));
            }
            catch (IOException e) {
              binariesNotAvailableMessage = "\"createrepo\"";
              sb.append(new String(baos.toByteArray()));
              sb.append(e.getMessage()).append(NL);
            }

            sb.append(NL).append("\"mergerepo\" version:").append(NL);
            baos = new ByteArrayOutputStream();
            try {
              if (commandLineExecutor.exec(getConfig().getMergerepoPath() + " --version", baos, baos) != 0) {
                if (binariesNotAvailableMessage == null) {
                  binariesNotAvailableMessage = "\"mergerepo\"";
                }
                else {
                  binariesNotAvailableMessage += " and \"mergerepo\"";
                }
              }
              sb.append(new String(baos.toByteArray()));
            }
            catch (IOException e) {
              if (binariesNotAvailableMessage == null) {
                binariesNotAvailableMessage = "\"mergerepo\"";
              }
              else {
                binariesNotAvailableMessage += " and \"mergerepo\"";
              }
              sb.append(new String(baos.toByteArray()));
              sb.append(e.getMessage()).append(NL);
            }
            binariesLog = sb.toString().replace(NL, "<br/>");
            if (binariesNotAvailableMessage != null) {
              binariesNotAvailableMessage += " not available (see status for details)";
            }
            return binariesNotAvailableMessage == null;
          }

          @Override
          public String explainSatisfied() {
            return "\"createrepo\" and \"mergerepo\" are available";
          }

          @Override
          public String explainUnsatisfied() {
            return binariesNotAvailableMessage;
          }
        }
    );
  }

  /**
   * Show output of create/mergerepo --version.
   *
   * @since 2.11
   */
  @Override
  protected String renderStatus() {
    return binariesLog;
  }
}
