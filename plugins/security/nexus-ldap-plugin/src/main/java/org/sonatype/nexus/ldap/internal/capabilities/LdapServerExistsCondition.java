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
package org.sonatype.nexus.ldap.internal.capabilities;

import org.sonatype.nexus.capability.Condition;
import org.sonatype.nexus.capability.Evaluable;
import org.sonatype.nexus.capability.condition.EvaluableCondition;
import org.sonatype.nexus.ldap.internal.events.LdapClearCacheEvent;
import org.sonatype.nexus.ldap.internal.persist.LdapConfigurationManager;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A condition that is satisfied when an LDAP server exists.
 *
 * @since 2.4.1
 */
public class LdapServerExistsCondition
    extends EvaluableCondition
    implements Condition
{

  private final LdapConfigurationManager ldapConfigurationManager;

  private final LdapConditions.LdapServerId ldapServerId;

  private static interface Messages
      extends MessageBundle
  {

    @DefaultMessage("LDAP server exists")
    String satisfied();

    @DefaultMessage("LDAP server does not exist")
    String unsatisfied();
  }

  private static final Messages messages = I18N.create(Messages.class);

  public LdapServerExistsCondition(final EventBus eventBus,
                                   final LdapConfigurationManager ldapConfigurationManager,
                                   final LdapConditions.LdapServerId ldapServerId)
  {
    super(
        eventBus,
        new Evaluable()
        {
          @Override
          public boolean isSatisfied() {
            return ldapServerExists(ldapServerId, ldapConfigurationManager);
          }

          @Override
          public String explainSatisfied() {
            return messages.satisfied();
          }

          @Override
          public String explainUnsatisfied() {
            return messages.unsatisfied();
          }

        });
    this.ldapConfigurationManager = checkNotNull(ldapConfigurationManager);
    this.ldapServerId = checkNotNull(ldapServerId);
  }

  @AllowConcurrentEvents
  @Subscribe
  public void handle(final LdapClearCacheEvent event) {
    setSatisfied(ldapServerExists(ldapServerId, ldapConfigurationManager));
  }

  private static boolean ldapServerExists(final LdapConditions.LdapServerId ldapServerId,
                                          final LdapConfigurationManager ldapConfigurationManager)
  {
    final String id = ldapServerId.get();
    if (id == null) {
      return false;
    }
    try {
      return ldapConfigurationManager.getLdapServerConfiguration(id) != null;
    }
    catch (Exception e) {
      return false;
    }
  }

}
