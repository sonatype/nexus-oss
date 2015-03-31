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
    GLOBAL_HEADER_BROWSE_TITLE: 'Browse',
    GLOBAL_HEADER_BROWSE_TOOLTIP: 'Browse server contents',
    GLOBAL_HEADER_ADMIN_TITLE: 'Administration',
    GLOBAL_HEADER_ADMIN_TOOLTIP: 'Server administration and configuration',
    GLOBAL_HEADER_SEARCH_PLACEHOLDER: 'Search components',
    GLOBAL_HEADER_SEARCH_TOOLTIP: 'Quick component keyword search',
    GLOBAL_HEADER_REFRESH_TOOLTIP: 'Refresh current view and data',
    GLOBAL_HEADER_REFRESH_DEFAULT: 'Refreshed',
    GLOBAL_HEADER_USER: 'User',
    GLOBAL_HEADER_USER_TOOLTIP: 'Hi, {0}. Manage your user account.',
    GLOBAL_HEADER_SIGN_IN: 'Sign in',
    GLOBAL_HEADER_SIGN_IN_TOOLTIP: 'Sign in',
    GLOBAL_HEADER_SIGN_OUT: 'Sign out',
    GLOBAL_HEADER_SIGN_OUT_TOOLTIP: 'Sign out',
    GLOBAL_HEADER_HELP_TOOLTIP: 'Help',
    GLOBAL_HEADER_HELP_FEATURE: 'Help for: ',
    GLOBAL_HEADER_HELP_FEATURE_TOOLTIP: 'Help and documentation for the currently selected feature',
    GLOBAL_HEADER_HELP_ABOUT: 'About',
    GLOBAL_HEADER_HELP_ABOUT_TOOLTIP: 'About Sonatype Nexus',
    GLOBAL_HEADER_HELP_DOCUMENTATION: 'Documentation',
    GLOBAL_HEADER_HELP_DOCUMENTATION_TOOLTIP: 'Sonatype Nexus product documentation',
    GLOBAL_HEADER_HELP_KB: 'Knowledge base',
    GLOBAL_HEADER_HELP_KB_TOOLTIP: 'Sonatype Nexus knowledge base',
    GLOBAL_HEADER_HELP_COMMUNITY: 'Community',
    GLOBAL_HEADER_HELP_COMMUNITY_TOOLTIP: 'Sonatype Nexus community information',
    GLOBAL_HEADER_HELP_ISSUES: 'Issue tracker',
    GLOBAL_HEADER_HELP_ISSUES_TOOLTIP: 'Sonatype Nexus issue and bug tracker',
    GLOBAL_HEADER_HELP_SUPPORT: 'Support',
    GLOBAL_HEADER_HELP_SUPPORT_TOOLTIP: 'Sonatype Nexus product support',

    // Footer
    GLOBAL_FOOTER_COPYRIGHT: 'Copyright &copy; 2008-2015, Sonatype Inc. All rights reserved.',

    // Sign in
    GLOBAL_SIGN_IN_TITLE: 'Sign In',
    GLOBAL_SIGN_IN_MASK: 'Signing in&hellip;',
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
    GLOBAL_DIALOG_ADD_SUBMIT_BUTTON: 'Create',
    GLOBAL_DIALOG_ADD_CANCEL_BUTTON: 'Cancel',
    GLOBAL_DIALOG_ORDER_SUBMIT_BUTTON: 'Save',
    GLOBAL_DIALOG_ORDER_CANCEL_BUTTON: 'Cancel',

    // Messages
    GLOBAL_MESSAGES_TOOLTIP: 'Toggle messages display',
    GLOBAL_MESSAGES_EMPTY_STATE: 'No messages',

    // Server
    GLOBAL_SERVER_EXTDIRECT_WARNING: 'Operation failed as server could not be contacted',
    GLOBAL_SERVER_RECONNECTED_SUCCESS: 'Server reconnected',
    GLOBAL_SERVER_DISCONNECTED: 'Server disconnected',
    GLOBAL_SERVER_EXPIRE_WARNING: 'Session is about to expire',
    GLOBAL_SERVER_EXPIRED_WARNING: 'Session expired after being inactive for {0} minutes',
    GLOBAL_SERVER_SIGNED_IN: 'User signed in: {0}',
    GLOBAL_SERVER_SIGNED_OUT: 'User signed out',
    GLOBAL_SERVER_INCORRECT_CREDENTIALS_WARNING: 'Incorrect username and/or password or no permission to use the Nexus User Interface.',
    GLOBAL_SERVER_DOWNLOAD_SUCCESS: 'Download initiated',
    GLOBAL_SERVER_BLOCKED_POPUP_DANGER: 'Window pop-up was blocked!',

    // License
    GLOBAL_LICENSE_INSTALLED_SUCCESS: 'License installed',
    GLOBAL_LICENSE_UNINSTALLED_WARNING: 'License uninstalled',

    // About modal
    GLOBAL_ABOUT_TITLE: 'About Sonatype Nexus',
    GLOBAL_ABOUT_CLOSE_BUTTON: 'Close',
    GLOBAL_ABOUT_COPYRIGHT_TAB: 'Copyright',
    GLOBAL_ABOUT_LICENSE_TAB: 'License',

    // Authentication modal
    GLOBAL_AUTHENTICATE_TITLE: 'Authenticate',
    GLOBAL_AUTHENTICATE_HELP: 'You have requested an operation which requires validation of your credentials.',
    GLOBAL_AUTHENTICATE_MASK: 'Authenticate&hellip;',
    GLOBAL_AUTHENTICATE_SUBMIT_BUTTON: 'Authenticate',
    GLOBAL_AUTHENTICATE_RETRIEVING_MASK: 'Retrieving authentication token&hellip;',
    GLOBAL_AUTHENTICATE_CANCEL_BUTTON: 'Cancel',

    // Expiry modal
    GLOBAL_EXPIRE_TITLE: 'Session',
    GLOBAL_EXPIRE_HELP: 'Session is about to expire',
    GLOBAL_EXPIRE_SECONDS: 'Session will expire in {0} seconds',
    GLOBAL_EXPIRE_SIGNED_OUT: 'Your session has expired. Please sign in.',
    GLOBAL_EXPIRE_CANCEL_BUTTON: 'Cancel',
    GLOBAL_EXPIRE_SIGN_IN_BUTTON: 'Sign in',
    GLOBAL_EXPIRE_CLOSE_BUTTON: 'Close',

    // Unsaved changes modal
    GLOBAL_UNSAVED_TITLE: 'Unsaved changes',
    GLOBAL_UNSAVED_MESSAGE: '<p>Do you want to discard your changes?</p>',
    GLOBAL_UNSAVED_DISCARD_BUTTON: 'Discard changes',
    GLOBAL_UNSAVED_BACK_BUTTON: 'Go back',
    GLOBAL_UNSAVED_BROWSER_TITLE: 'You will lose your unsaved changes',

    // Unsupported browser
    GLOBAL_UNSUPPORTED_TITLE: 'The browser you are using is not supported',
    GLOBAL_UNSUPPORTED_ALTERNATIVES: 'Below is a list of alternatives that are supported by this web application',
    GLOBAL_UNSUPPORTED_CONTINUE_BUTTON: 'Ignore and continue',

    // 404
    GLOBAL_404_PATH: 'Path "{0}" not found',
    GLOBAL_404_NO_PATH: 'Path not found',

    // Buttons
    GLOBAL_BUTTON_SAVE: 'Save',
    GLOBAL_BUTTON_DISCARD: 'Discard',
    GLOBAL_BUTTON_NEXT: 'Next',

    // Item selector
    GLOBAL_ITEM_SELECTOR_FILTER: 'Filter',

    // Settings form
    GLOBAL_SETTINGS_LOADING: 'Loading',
    GLOBAL_SETTINGS_SAVING: 'Saving',

    // Browse -> Welcome
    BROWSE_WELCOME_TITLE: 'Welcome',
    BROWSE_WELCOME_SUBTITLE: 'Welcome to Sonatype Nexus!'
  }
}, function(obj) {
  NX.I18n.register(obj.keys);
});

