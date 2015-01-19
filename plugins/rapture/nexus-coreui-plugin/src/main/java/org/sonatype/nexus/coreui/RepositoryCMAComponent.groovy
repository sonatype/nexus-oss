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
package org.sonatype.nexus.coreui

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.configuration.validation.InvalidConfigurationException
import org.sonatype.configuration.validation.ValidationMessage
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.repository.Recipe
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.config.Configuration
import org.sonatype.nexus.repository.manager.RepositoryManager
import org.sonatype.nexus.validation.Create
import org.sonatype.nexus.validation.Update
import org.sonatype.nexus.validation.Validate

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

/**
 * Repository CMA {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_RepositoryCMA')
class RepositoryCMAComponent
extends DirectComponentSupport
{
  @Inject
  RepositoryManager repositoryManager

  @Inject
  Map<String, Recipe> recipes

  @DirectMethod
  List<RepositoryCMAXO> read() {
    repositoryManager.browse().collect { asRepository(it) }
  }

  @DirectMethod
  List<ReferenceXO> readRecipes() {
    recipes.collect { key, value ->
      new ReferenceXO(
          id: key,
          name: "${value.format} (${value.type})"
      )
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @Validate(groups = [Create.class, Default.class])
  RepositoryCMAXO create(final @NotNull(message = '[repository] may not be null') @Valid RepositoryCMAXO repository) {
    return asRepository(repositoryManager.create(new Configuration(
        repositoryName: repository.name,
        recipeName: repository.recipe,
        attributes: asAttributes(repository.attributes)
    )))
  }

  @DirectMethod
  @RequiresAuthentication
  @Validate(groups = [Update.class, Default.class])
  RepositoryCMAXO update(final @NotNull(message = '[repository] may not be null') @Valid RepositoryCMAXO repository) {
    return asRepository(repositoryManager.update(repositoryManager.get(repository.name).configuration.with {
      attributes = asAttributes(repository.attributes)
      return it
    }))
  }

  @DirectMethod
  @RequiresAuthentication
  @Validate
  void remove(final @NotEmpty(message = '[name] may not be empty') String name) {
    repositoryManager.delete(name)
  }

  RepositoryCMAXO asRepository(Repository input) {
    return new RepositoryCMAXO(
        name: input.name,
        type: input.type,
        format: input.format,
        attributes: asAttributes(input.configuration.attributes)
    )
  }

  Map<String, Map<String, Object>> asAttributes(final String attributes) {
    if (!attributes) {
      return null;
    }
    TypeReference<Map<String, Map<String, Object>>> typeRef = new TypeReference<Map<String, Map<String, Object>>>() {}
    try {
      return new ObjectMapper().readValue(attributes, typeRef)
    }
    catch (Exception e) {
      def validations = new ValidationResponse()
      validations.addValidationError(new ValidationMessage('attributes', e.message))
      throw new InvalidConfigurationException(validations)
    }
  }

  String asAttributes(final Map<String, Map<String, Object>> attributes) {
    if (!attributes) {
      return null;
    }
    return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(attributes)
  }

}
