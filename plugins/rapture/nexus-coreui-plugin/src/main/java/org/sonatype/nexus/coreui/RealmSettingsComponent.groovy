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

package org.sonatype.nexus.coreui

import com.google.inject.Key
import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.bval.guice.Validate
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.apache.shiro.realm.Realm
import org.eclipse.sisu.inject.BeanLocator
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.security.SecuritySystem

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * Realm Security Settings {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_RealmSettings')
class RealmSettingsComponent
extends DirectComponentSupport
{

  @Inject
  SecuritySystem securitySystem

  @Inject
  BeanLocator beanLocator

  /**
   * Retrieves security realm settings.
   * @return security realm settings
   */
  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  RealmSettingsXO read() {
    return new RealmSettingsXO(
        realms: securitySystem.realms
    )
  }

  /**
   * Retrieves realm types.
   * @return a list of realm types
   */
  @DirectMethod
  @RequiresPermissions('nexus:componentrealmtypes:read')
  List<ReferenceXO> readRealmTypes() {
    beanLocator.locate(Key.get(Realm.class, Named.class)).collect { entry ->
      new ReferenceXO(
          id: entry.key.value,
          name: entry.description
      )
    }
  }

  /**
   * Updates security realm settings.
   * @return updated security realm settings
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  @Validate
  RealmSettingsXO update(final @NotNull(message = '[realmSettings] may not be null') @Valid RealmSettingsXO realmSettingsXO) {
    securitySystem.realms = realmSettingsXO.realms
    return read()
  }

}
