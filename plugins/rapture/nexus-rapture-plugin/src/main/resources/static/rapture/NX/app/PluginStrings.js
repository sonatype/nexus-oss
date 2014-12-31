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
 * Application strings
 *
 * @since 3.0
 */
Ext.define('NX.app.PluginStrings', {
  '@aggregate_priority': 1,

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
    GLOBAL_HEADER_BROWSE_TOOLTIP: 'Browse Server Contents',
    GLOBAL_HEADER_ADMIN_TOOLTIP: 'Server administration and configuration',
    GLOBAL_HEADER_SEARCH_PLACEHOLDER: 'Search…',
    GLOBAL_HEADER_REFRESH_TOOLTIP: 'Refresh current view and data',
    GLOBAL_HEADER_USER_TOOLTIP: 'User profile and options',
    GLOBAL_HEADER_SIGN_IN: 'Sign In',
    GLOBAL_HEADER_SIGN_IN_TOOLTIP: 'Have an account?',
    GLOBAL_HEADER_SIGN_OUT_TOOLTIP: 'Sign out',
    GLOBAL_HEADER_HELP_TOOLTIP: 'Help',
    GLOBAL_HEADER_HELP_FEATURE: 'Help for: ',
    GLOBAL_HEADER_HELP_ABOUT: 'About',
    GLOBAL_HEADER_HELP_DOCUMENTATION: 'Documentation',
    GLOBAL_HEADER_HELP_KB: 'Knowledge Base',
    GLOBAL_HEADER_HELP_COMMUNITY: 'Community',
    GLOBAL_HEADER_HELP_ISSUES: 'Issue Tracker',
    GLOBAL_HEADER_HELP_SUPPORT: 'Support',

    // Footer
    GLOBAL_FOOTER_COPYRIGHT: 'Sonatype Nexus™ © Copyright Sonatype, Inc.',

    // Sign in
    GLOBAL_SIGN_IN_TITLE: 'Sign In',
    GLOBAL_SIGN_IN_USERNAME: 'Username',
    GLOBAL_SIGN_IN_USERNAME_PLACEHOLDER: 'enter your username',
    GLOBAL_SIGN_IN_PASSWORD: 'Password',
    GLOBAL_SIGN_IN_PASSWORD_PLACEHOLDER: 'enter your password',
    GLOBAL_SIGN_IN_REMEMBER_ME: 'Remember me',
    GLOBAL_SIGN_IN_SUBMIT: 'Sign In',
    GLOBAL_SIGN_IN_CANCEL: 'Cancel',

    // Filter box
    GLOBAL_FILTER_PLACEHOLDER: 'filter',

    // Pagination
    //GLOBAL_PAGINATION_CONTROL: 'Page {0} of {1}',
    //GLOBAL_PAGINATION_DISPLAYED: 'Displaying {0} of {1}',

    // Browse -> Welcome
    BROWSE_WELCOME_TITLE: 'Welcome',
    BROWSE_WELCOME_SUBTITLE: 'Welcome to Sonatype Nexus!'
  }
}, function(obj) {
  NX.I18n.register(obj.keys);
});

