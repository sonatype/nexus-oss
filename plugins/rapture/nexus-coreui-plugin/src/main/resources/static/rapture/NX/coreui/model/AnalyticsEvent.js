/*
 * Copyright (c) 2008-2014 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Analytics Event model.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.model.AnalyticsEvent', {
  extend: 'Ext.data.Model',
  fields: [
    'type',
    'timestamp',
    'duration',
    'userId',
    'attributes'
  ]
});
