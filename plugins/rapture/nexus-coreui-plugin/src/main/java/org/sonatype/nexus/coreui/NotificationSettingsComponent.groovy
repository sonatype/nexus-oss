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

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.nexus.configuration.application.NexusConfiguration
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.guice.Validate
import org.sonatype.nexus.notification.NotificationManager
import org.sonatype.nexus.notification.NotificationTarget

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull

import static org.sonatype.nexus.notification.NotificationCheat.AUTO_BLOCK_NOTIFICATION_GROUP_ID

/**
 * Notifications System Settings {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_NotificationSettings')
class NotificationSettingsComponent
extends DirectComponentSupport
{

  @Inject
  NotificationManager notificationManager

  @Inject
  NexusConfiguration nexusConfiguration

  /**
   * Retrieves notification system settings
   * @return notification system settings
   */
  @DirectMethod
  @RequiresPermissions('nexus:settings:read')
  NotificationSettingsXO read() {
    NotificationTarget notificationTarget = notificationManager.readNotificationTarget(AUTO_BLOCK_NOTIFICATION_GROUP_ID)
    return new NotificationSettingsXO(
        enabled: notificationManager.enabled,
        notifyEmails: notificationTarget?.externalTargets,
        notifyRoles: notificationTarget?.targetRoles
    )
  }

  /**
   * Updates notification system settings
   * @return updated notification system settings
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:settings:update')
  @Validate
  NotificationSettingsXO update(final @NotNull(message = '[notificationSettingsXO] may not be null') @Valid NotificationSettingsXO notificationSettingsXO) {
    notificationManager.enabled = notificationSettingsXO.enabled

    NotificationTarget notificationTarget = new NotificationTarget()
    if (notificationSettingsXO.notifyEmails) notificationTarget.externalTargets.addAll(notificationSettingsXO.notifyEmails)
    if (notificationSettingsXO.notifyRoles) notificationTarget.targetRoles.addAll(notificationSettingsXO.notifyRoles)
    notificationManager.updateNotificationTarget(notificationTarget)

    nexusConfiguration.saveConfiguration()

    return read()
  }

}
