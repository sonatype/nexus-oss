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
/*global Ext, NX*/

/**
 * Analytics settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.analytics.AnalyticsSettings', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-analytics-settings',
  requires: [
    'NX.Conditions'
  ],

  /**
   * @override
   */
  initComponent: function() {
    var me = this;

    me.items = [
      {
        xtype: 'nx-settingsform',
        settingsFormSuccessMessage: 'Analytics Settings $action',
        api: {
          load: 'NX.direct.analytics_Settings.read',
          submit: 'NX.direct.analytics_Settings.update'
        },
        editableCondition: NX.Conditions.isPermitted('nexus:analytics', 'update'),
        editableMarker: 'You do not have permission to configure analytics',

        items: [
          {
            xtype: 'container',
            html: '<p>The analytics feature collects non-sensitive information about how your organization is using Nexus. ' +
                'It is useful to you from a compatibility perspective, since it gathers answers to questions such as what features are most important, ' +
                'where are users having difficulty and what integrations/APIs are actively in use. This data is available to you and allows you to understand your usage of Nexus better. ' +
                'Provided to Sonatype it enables us to tailor the ongoing development of the product.</p>' +
                '<b>Event Collection</b>' +
                '<p>The collected information is limited to the use of the Nexus user interface and the Nexus REST API -- i.e. the primary interaction points between your environment and Nexus. ' +
                'Only the user interface navigation flows and REST endpoints being called are recorded. None of the request specific data ' +
                '(e.g. credentials or otherwise sensitive information) is ever captured.</p>' +
                '<p>Event collection and submission are controlled separately.  When collection is enabled, a summary of the data collected is shown on the <code>Events</code> tab.</p>' +
                '<b>Event Submission</b>' +
                '<p>Analytics event data can be submitted either automatically or manually.' +
                '<br/><code>Export</code> generates a ZIP file that can be inspected prior to any information being sent to the Nexus analytics service. ' +
                '<br/><code>Submit</code> generates a ZIP file and then immediately uploads it to the Nexus analytics service.</p>'
          },
          {
            xtype: 'checkbox',
            name: 'collection',
            boxLabel: 'Enable analytics event collection'
          },
          {
            xtype: 'checkbox',
            name: 'autosubmit',
            boxLabel: 'Enable automatic analytics event submission'
          }
        ]
      }
    ];

    me.callParent();
  }
});