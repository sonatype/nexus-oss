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
   * <Plugin>_<Class>_<Name>_<COMPONENT>
   */

  keys: {

    /*
     * Global strings
     */

    // Header
    View_Header_Panel_Logo_Text: 'Sonatype Nexus',
    View_Header_BrowseMode_Title: 'Browse',
    View_Header_BrowseMode_Tooltip: 'Browse server contents',
    View_Header_AdminMode_Title: 'Administration',
    View_Header_AdminMode_Tooltip: 'Server administration and configuration',
    View_Header_QuickSearch_Empty: 'Search components',
    View_Header_QuickSearch_Tooltip: 'Quick component keyword search',
    View_Header_Refresh_Tooltip: 'Refresh current view and data',
    Controller_Refresh_Message: 'Refreshed',
    View_Header_UserMode_Title: 'User',
    Controller_User_Tooltip: 'Hi, {0}. Manage your user account.',
    View_Header_SignIn_Text: 'Sign in',
    View_Header_SignIn_Tooltip: 'Sign in',
    View_Header_SignOut_Text: 'Sign out',
    View_Header_SignOut_Tooltip: 'Sign out',
    View_Header_Help_Tooltip: 'Help',
    Controller_Help_Feature_Text: 'Help for: ',
    View_Header_Help_Feature_Tooltip: 'Help and documentation for the currently selected feature',
    View_Header_Help_About_Text: 'About',
    View_Header_Help_About_Tooltip: 'About Sonatype Nexus',
    View_Header_Help_Documentation_Text: 'Documentation',
    View_Header_Help_Documentation_Tooltip: 'Sonatype Nexus product documentation',
    View_Header_Help_KB_Text: 'Knowledge base',
    View_Header_Help_KB_Tooltip: 'Sonatype Nexus knowledge base',
    View_Header_Help_Community_Text: 'Community',
    View_Header_Help_Community_Tooltip: 'Sonatype Nexus community information',
    View_Header_Help_Issues_Text: 'Issue tracker',
    View_Header_Help_Issues_Tooltip: 'Sonatype Nexus issue and bug tracker',
    View_Header_Help_Support_Text: 'Support',
    View_Header_Help_Support_Tooltip: 'Sonatype Nexus product support',

    // Footer
    View_Footer_Panel_HTML: 'Copyright &copy; 2008-2015, Sonatype Inc. All rights reserved.',

    // Sign in
    View_SignIn_Title: 'Sign In',
    Controller_User_SignIn_Mask: 'Signing in&hellip;',
    View_SignIn_Username_Empty: 'Username',
    View_SignIn_Password_Empty: 'Password',
    View_SignIn_RememberMe_BoxLabel: 'Remember me',
    View_SignIn_Submit_Button: 'Sign in',
    View_SignIn_Cancel_Button: 'Cancel',

    // Filter box
    Ext_Grid_Plugin_FilterBox_Empty: 'Filter',

    // Dialogs
    Dialogs_Info_Title: 'Information',
    Dialogs_Error_Title: 'Error',
    Dialogs_Error_Message: 'Operation failed',
    View_Add_Submit_Button: 'Create',
    View_Add_Cancel_Button: 'Cancel',
    View_ChangeOrderWindow_Submit_Button: 'Save',
    View_ChangeOrderWindow_Cancel_Button: 'Cancel',

    // Messages
    View_Header_Messages_Tooltip: 'Toggle messages display',
    View_Message_Panel_Empty: 'No messages',

    // Server
    Controller_User_ConnectFailure_Message: 'Operation failed as server could not be contacted',
    Controller_State_Reconnected_Message: 'Server reconnected',
    Controller_State_Disconnected_Message: 'Server disconnected',
    Controller_UiSessionTimeout_Expire_Message: 'Session is about to expire',
    Controller_UiSessionTimeout_Expired_Message: 'Session expired after being inactive for {0} minutes',
    Controller_User_SignedIn_Message: 'User signed in: {0}',
    Controller_User_SignedOut_Message: 'User signed out',
    Controller_User_Credentials_Message: 'Incorrect username and/or password or no permission to use the Nexus User Interface.',
    Util_DownloadHelper_Download_Message: 'Download initiated',
    Windows_Popup_Message: 'Window pop-up was blocked!',

    // License
    Controller_State_Installed_Message: 'License installed',
    Controller_State_Uninstalled_Message: 'License uninstalled',

    // About modal
    View_AboutWindow_Title: 'About Sonatype Nexus',
    View_AboutWindow_Close_Button: 'Close',
    View_AboutWindow_About_Title: 'Copyright',
    View_AboutWindow_License_Tab: 'License',

    // Authentication modal
    View_Authenticate_Title: 'Authenticate',
    View_Authenticate_Help_Text: 'You have requested an operation which requires validation of your credentials.',
    User_Controller_Authenticate_Mask: 'Authenticate&hellip;',
    User_View_Authenticate_Submit_Button: 'Authenticate',
    Controller_User_Retrieving_Mask: 'Retrieving authentication token&hellip;',
    View_Authenticate_Cancel_Button: 'Cancel',

    // Expiry modal
    View_ExpireSession_Title: 'Session',
    View_Controller_UiSessionTimeout_Expire_Text: 'Session is about to expire',
    Controller_UiSessionTimeout_Expire_Text: 'Session will expire in {0} seconds',
    Controller_SignedOut_Text: 'Your session has expired. Please sign in.',
    View_ExpireSession_Cancel_Button: 'Cancel',
    View_ExpireSession_SignIn_Button: 'Sign in',
    View_ExpireSession_Close_Button: 'Close',

    // Unsaved changes modal
    View_UnsavedChanges_Title: 'Unsaved changes',
    View_UnsavedChanges_Help_HTML: '<p>Do you want to discard your changes?</p>',
    View_UnsavedChanges_Discard_Button: 'Discard changes',
    View_UnsavedChanges_Back_Button: 'Go back',
    Controller_Menu_Browser_Title: 'You will lose your unsaved changes',

    // Unsupported browser
    View_UnsupportedBrowser_Title: 'The browser you are using is not supported',
    View_UnsupportedBrowser_Alternatives_Text: 'Below is a list of alternatives that are supported by this web application',
    View_UnsupportedBrowser_Continue_Button: 'Ignore and continue',

    // 404
    View_Feature_NotFoundPath_Text: 'Path "{0}" not found',
    View_Feature_NotFound_Text: 'Path not found',

    // Buttons
    View_SettingsForm_Save_Button: 'Save',
    View_SettingsForm_Discard_Button: 'Discard',
    Coreui_View_Ldap_LdapServerConnectionAdd_Text: 'Next',

    // Item selector
    Ext_Form_Field_ItemSelector_Empty: 'Filter',

    // Settings form
    View_SettingsForm_Load_Message: 'Loading',
    View_SettingsForm_Submit_Message: 'Saving',

    // Browse -> Welcome
    Controller_Dashboard_Title: 'Welcome',
    Controller_Dashboard_Description: 'Welcome to Sonatype Nexus!',

    // Field validation messages
    Util_Validator_Text: 'Only letters, digits, underscores(_), hyphens(-), and dots(.) are allowed and may not start with underscore or dot.'
  }
}, function(obj) {
  NX.I18n.register(obj.keys);
});

