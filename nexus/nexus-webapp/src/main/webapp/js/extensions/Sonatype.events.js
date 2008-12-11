/*
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
Sonatype.utils.Observable = function(){
  this.addEvents({
    /* 
     * Fired when a repository is created, edited or deleted on the repository config tab.
     */
    'repositoryChanged': true,

    /* 
     * Fired when a group is created, edited or deleted on the group config tab.
     */
    'groupChanged': true,
    
    /*
     * Fired when the main Nexus navigation panel is being built.
     * Subscribers can use this event to add items to the navigation panel.
     * 
     * A Sonatype.navigation.NavigationPanel instance is passed as a parameter.
     * The subscriber can use the "add" method on it to append new sections or
     * individual items to existing sections (using a "sectionId" config property).
     * 
     * init: function() {
     *   Sonatype.Events.addListener( 'nexusNavigationInit', this.naviHandler, this );
     * },
     * 
     * naviHandler: function( navigationPanel ) {
     *   // add a new section with some items
     *   navigationPanel.add( {
     *     title: 'My Section',
     *     id: 'my-nexus-section',
     *     items: [
     *       {
     *         title: 'Open New Tab',
     *         tabId: 'my-unique-tab-id',
     *         tabCode: My.package.ClassName, // JavaScript class implementing Ext.Panel,
     *         tabTitle: 'My Tab' // optional tab title (if different from the link title)
     *       },
     *       {
     *         title: 'Open Another Tab',
     *         tabId: 'my-second-tab-id',
     *         tabCode: My.package.AnotherClass,
     *         enabled: this.isSecondTabEnabled() // condition to show the link or not
     *       },
     *       {
     *         title: 'Pop-up Dialog',
     *         handler: this.popupHandler, // click handler
     *         scope: this                 // handler execution scope
     *       }
     *     ]
     *   } );
     *   
     *   // add a link to an existing section
     *   navigationPanel.add( {
     *     sectionId: 'st-nexus-docs',
     *     title: 'Download Nexus',
     *     href: 'http://nexus.sonatype.org/using/download.html'
     *   } );
     * },
     * 
     * See Sonatype.repoServer.RepoServer.addNexusNavigationItems() for more examples.
     */
    'nexusNavigationInit': true,
    
    /*
     * Fired when a repository context action menu is initialized.
     * Subscribers can add action items to the menu.
     * 
     * A menu object and repository record are passed as parameters.
     * If clicked, the action handler receives a repository record as parameter.
     * 
     * init: function() {
     *   Sonatype.Events.addListener( 'repositoryMenuInit', this.onRepoMenu, this );
     * },
     * 
     * onRepoMenu: function( menu, repoRecord ) {
     *   if ( repoRecord.get( 'repoType' ) == 'proxy' ) {
     *     menu.add( this.actions.myProxyAction );
     *   }
     * },
     */
    'repositoryMenuInit': true,

    /*
     * Fired when an repository item (e.g. artifact or folder) context action menu
     * is initialized. Subscribers can add action items to the menu.
     * 
     * A menu object, a repository and item records are passed as parameters.
     * If clicked, the action handler receives an item record as parameter.
     * 
     * init: function() {
     *   Sonatype.Events.addListener( 'repositoryContentMenuInit', this.onArtifactMenu, this );
     * },
     * 
     * onRepoMenu: function( menu, repoRecord, contentRecord ) {
     *   if ( repoRecord.get( 'repoType' ) == 'proxy' ) {
     *     menu.add( this.actions.myProxyContentAction );
     *   }
     * },
     */
    'repositoryContentMenuInit': true,

    /*
     * Fired when a user action menu is initialized (most likely an admin function).
     * Subscribers can add action items to the menu.
     * 
     * A menu object and a user record are passed as parameters.
     * If clicked, the action handler receives an user record as parameter.
     * 
     * init: function() {
     *   Sonatype.Events.addListener( 'userMenuInit', this.onUserMenu, this );
     * },
     * 
     * onUserMenu: function( menu, userRecord ) {
     *   if ( userRecord.get( 'userId' ) != 'anonymous' ) {
     *     menu.add( this.actions.myUserAction );
     *   }
     * },
     */
    'userMenuInit': true,

    /*
     * Fired when a user list is loaded (e.g. on the user editor panel).
     * Subscribers can add more users to the list and assign editors.
     * 
     * A container object is passed as parameter. Some container implementations
     * may be read only, so they will ignore the editor supplied.
     * 
     * init: function() {
     *   Sonatype.Events.addListener( 'userListInit', this.populateUsers, this );
     * },
     * 
     * populateUsers: function( userPanel ) {
     *   userPanel.addRecords( myReadOnlyUsers, 'System Users' );
     *   userPanel.addRecords( myEditableUsers, 'Other Users', My.ext.UserFormPanel );
     * },
     */
    'userListInit': true
  });
};
Ext.extend( Sonatype.utils.Observable, Ext.util.Observable );

Sonatype.Events = new Sonatype.utils.Observable();
