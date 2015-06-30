/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.capability;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.sonatype.nexus.validation.ConstraintViolations;
import org.sonatype.nexus.validation.group.Create;
import org.sonatype.nexus.validation.group.Update;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Support for {@link CapabilityDescriptor} implementations.
 *
 * @since 2.7
 */
public abstract class CapabilityDescriptorSupport<ConfigT>
    extends ComponentSupport
    implements CapabilityDescriptor
{
  private boolean exposed = true;

  private boolean hidden = false;

  @Override
  public String about() {
    try {
      return renderAbout();
    }
    catch (Exception e) {
      log.warn("Failed to render about", e);
    }
    return null;
  }

  protected String renderAbout() throws Exception {
    return null;
  }

  @Override
  public boolean isExposed() {
    return exposed;
  }

  protected void setExposed(final boolean exposed) {
    this.exposed = exposed;
  }

  @Override
  public boolean isHidden() {
    return hidden;
  }

  protected void setHidden(final boolean hidden) {
    this.hidden = hidden;
  }

  //
  // Version support
  //

  @Override
  public int version() {
    return 1;
  }

  @Override
  public Map<String, String> convert(final Map<String, String> properties, final int fromVersion) {
    return properties;
  }

  private Provider<Validator> validatorProvider;

  @Inject
  public void installValidationComponents(final Provider<Validator> validatorProvider) {
    checkState(this.validatorProvider == null);
    this.validatorProvider = checkNotNull(validatorProvider);
  }

  @Override
  public void validate(final Map<String, String> properties, final ValidationMode validationMode) {
    ConfigT config = createConfig(properties);
    if (config != null) {
      if (validationMode == ValidationMode.CREATE) {
        validate(config, Create.class, Default.class);
      }
      else {
        validate(config, Update.class, Default.class);
      }
    }
  }

  protected void validate(final Object value, final Class<?>... groups) {
    checkNotNull(value);
    checkNotNull(groups);

    if (log.isTraceEnabled()) {
      log.trace("Validating: {} in groups: {}", value, Arrays.asList(groups));
    }

    Validator validator = validatorProvider.get();
    Set<ConstraintViolation<Object>> violations = validator.validate(value, groups);
    ConstraintViolations.maybePropagate(violations, log);
  }

  protected ConfigT createConfig(final Map<String, String> properties) { return null; }

  //
  // Template support
  //

  private TemplateEngine templateEngine;

  protected TemplateEngine getTemplateEngine() {
    checkState(templateEngine != null);
    return templateEngine;
  }

  @Inject
  public void installTemplateEngine(final @Named("shared-velocity") TemplateEngine templateEngine) {
    checkState(this.templateEngine == null);
    this.templateEngine = checkNotNull(templateEngine);
  }

  protected String render(final String template, @Nullable final Map<String, Object> params) {
    return getTemplateEngine().render(this, template, params);
  }

  protected String render(final String template, final TemplateParameters params) {
    return getTemplateEngine().render(this, template, params);
  }

  protected String render(final String template) {
    return getTemplateEngine().render(this, template, (Map<String, Object>) null);
  }
}
