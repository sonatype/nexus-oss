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
/*global Ext, NX*/

/**
 * Application strings
 *
 * @since 3.0
 */
Ext.define('NX.app.PluginStrings', {
  '@aggregate_priority': 90,

  singleton: true,

  requires: [
    'NX.I18n'
  ],

  /*
   * Note: Symbols follow the following naming convention:
   * <MODE>_<FEATURE>_<VIEW>_<NAME>_<COMPONENT>
   */

  keys: {

    /*
     * Global strings
     */

    // Header
    GLOBAL_HEADER_TITLE: 'Sonatype Nexus',
    GLOBAL_HEADER_BROWSE_TOOLTIP: 'Browse server contents',
    GLOBAL_HEADER_ADMIN_TOOLTIP: 'Server administration and configuration',
    GLOBAL_HEADER_SEARCH_PLACEHOLDER: 'Search components',
    GLOBAL_HEADER_REFRESH_TOOLTIP: 'Refresh current view and data',
    GLOBAL_HEADER_USER_TOOLTIP: 'User profile and options',
    GLOBAL_HEADER_SIGN_IN: 'Sign in',
    GLOBAL_HEADER_SIGN_IN_TOOLTIP: 'Administer the server',
    GLOBAL_HEADER_SIGN_OUT_TOOLTIP: 'Sign out',
    GLOBAL_HEADER_HELP_TOOLTIP: 'Help',
    GLOBAL_HEADER_HELP_FEATURE: 'Help for: ',
    GLOBAL_HEADER_HELP_ABOUT: 'About',
    GLOBAL_HEADER_HELP_DOCUMENTATION: 'Documentation',
    GLOBAL_HEADER_HELP_KB: 'Knowledge base',
    GLOBAL_HEADER_HELP_COMMUNITY: 'Community',
    GLOBAL_HEADER_HELP_ISSUES: 'Issue tracker',
    GLOBAL_HEADER_HELP_SUPPORT: 'Support',

    // Footer
    GLOBAL_FOOTER_COPYRIGHT: 'Copyright © 2008-2015, Sonatype Inc. All rights reserved.',

    // Sign in
    GLOBAL_SIGN_IN_TITLE: 'Sign In',
    GLOBAL_SIGN_IN_USERNAME_PLACEHOLDER: 'Username',
    GLOBAL_SIGN_IN_PASSWORD_PLACEHOLDER: 'Password',
    GLOBAL_SIGN_IN_REMEMBER_ME: 'Remember me',
    GLOBAL_SIGN_IN_SUBMIT: 'Sign in',
    GLOBAL_SIGN_IN_CANCEL: 'Cancel',

    // Filter box
    GLOBAL_FILTER_PLACEHOLDER: 'Filter',

    // Dialogs
    GLOBAL_DIALOG_INFO_TITLE: 'Information',
    GLOBAL_DIALOG_ERROR_TITLE: 'Error',
    GLOBAL_DIALOG_ERROR_FAILED: 'Operation failed',
    GLOBAL_DIALOG_ADD_SUBMIT_BUTTON: 'Add',
    GLOBAL_DIALOG_ADD_CANCEL_BUTTON: 'Cancel',
    GLOBAL_DIALOG_ORDER_SUBMIT_BUTTON: 'Save',
    GLOBAL_DIALOG_ORDER_CANCEL_BUTTON: 'Cancel',

    // Messages
    GLOBAL_MESSAGES_EMPTY_STATE: 'No messages',

    // Buttons
    GLOBAL_BUTTON_SAVE: 'Save',
    GLOBAL_BUTTON_DISCARD: 'Discard',

    // Item selector
    GLOBAL_ITEM_SELECTOR_FILTER: 'Filter',

    // Settings form
    GLOBAL_SETTINGS_LOADING: 'Loading',
    GLOBAL_SETTINGS_SAVING: 'Saving',

    // Drilldown
    GLOBAL_DRILLDOWN_GO_BACK: 'Go back to {0}',

    // Browse -> Welcome
    BROWSE_WELCOME_TITLE: 'Welcome',
    BROWSE_WELCOME_SUBTITLE: 'Welcome to Sonatype Nexus!'
  }
}, function(obj) {
  NX.I18n.register(obj.keys);
});

