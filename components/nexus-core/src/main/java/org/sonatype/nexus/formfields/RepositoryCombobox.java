/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.formfields;

import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import com.google.common.base.Preconditions;
import org.codehaus.plexus.util.StringUtils;

/**
 * A repository combo box {@link FormField}.
 *
 * @since 2.7
 */
public class RepositoryCombobox
    extends ComboboxFormField<String>
{

  public static final String REGARDLESS_VIEW_PERMISSIONS = "regardlessViewPermissions";

  public static final String FACET = "facet";

  public static final String CONTENT_CLASS = "contentClass";

  private Class<?>[] facets;

  private boolean regardlessViewPermissions;

  private String[] contentClasses;

  private static interface Messages
      extends MessageBundle
  {

    @DefaultMessage("Staging Profile")
    String label();

    @DefaultMessage("Select a staging profile")
    String helpText();

  }

  private static final Messages messages = I18N.create(Messages.class);

  public RepositoryCombobox(String id, String label, String helpText, boolean required, String regexValidation) {
    super(id, label, helpText, required, regexValidation);
  }

  public RepositoryCombobox(String id, String label, String helpText, boolean required) {
    super(id, label, helpText, required);
  }

  public RepositoryCombobox(String id, boolean required) {
    super(id, messages.label(), messages.helpText(), required);
  }

  public RepositoryCombobox(String id) {
    super(id, messages.label(), messages.helpText(), false);
  }

  @Override
  protected String defaultStorePath() {
    return siestaStore("/capabilities/stores/repositories");
  }

  /**
   * Repository will be present if implements any of specified facets.
   */
  public RepositoryCombobox withAnyOfFacets(final Class<?>... facets) {
    this.facets = facets;
    return this;
  }

  /**
   * Repository will be present if has any of specified content classes.
   */
  public RepositoryCombobox withAnyOfContentClasses(final String... contentClasses) {
    this.contentClasses = contentClasses;
    return this;
  }

  /**
   * Repository will be present regardless if current user has rights to view the repository.
   */
  public RepositoryCombobox regardlessViewPermissions() {
    this.regardlessViewPermissions = true;
    return this;
  }

  @Override
  public String getStorePath() {
    Preconditions.checkState(StringUtils.isNotEmpty(super.getStorePath()), "Store path cannot be empty");

    StringBuilder sb = new StringBuilder();
    if (regardlessViewPermissions) {
      sb.append(REGARDLESS_VIEW_PERMISSIONS).append("=true");
    }
    if (facets != null) {
      for (Class<?> facet : facets) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        sb.append(FACET).append("=").append(facet.getName());
      }
    }
    if (contentClasses != null) {
      for (String contentClass : contentClasses) {
        if (sb.length() > 0) {
          sb.append("&");
        }
        sb.append(CONTENT_CLASS).append("=").append(contentClass);
      }
    }
    if (sb.length() > 0) {
      sb.insert(0, "?");
    }
    sb.insert(0, super.getStorePath());

    return sb.toString();
  }
}
