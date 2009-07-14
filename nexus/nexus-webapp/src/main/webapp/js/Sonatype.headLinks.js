Sonatype.headLinks = Ext.emptyFn;

Ext.apply(Sonatype.headLinks.prototype, {
	
	/**
	 * Update the head links based on the current status of Nexus
	 * 
	 * @param {Ext.Element}
	 *            linksEl parent of all the links' parent
	 */
	updateLinks : function() {
		var left = Ext.get('head-link-l');
		var middle = Ext.get('head-link-m');
		var right = Ext.get('head-link-r');
		
		var loggedIn = Sonatype.user.curr.isLoggedIn;
		if (loggedIn) {
			this.updateLeftWhenLoggedIn(left);
			this.updateMiddleWhenLoggedIn(middle);
			this.updateRightWhenLoggedIn(right);
		} else {
			this.updateLeftWhenLoggedOut(left);
			this.updateMiddleWhenLoggedOut(middle);
			this.updateRightWhenLoggedOut(right);
		}
	},
	
	updateLeftWhenLoggedIn : function( linkEl ){
		linkEl.update(Sonatype.user.curr.username);
	},
	
	updateMiddleWhenLoggedIn : function(linkEl){
		linkEl.update(' | ');
	},
	
	updateRightWhenLoggedIn : function(linkEl){
		linkEl.update('Log Out');
		linkEl.on('click', Sonatype.repoServer.RepoServer.loginHandler, Sonatype.repoServer.RepoServer);
		linkEl.setStyle({
			'color': '#15428B',
			'cursor': 'pointer',
			'text-align': 'right'
		});		
	},
	updateLeftWhenLoggedOut : function(linkEl){
		linkEl.update('');
	},
	
	updateMiddleWhenLoggedOut : function(linkEl){
		linkEl.update('');
	},
	
	updateRightWhenLoggedOut : function(linkEl){
		linkEl.update('Log In');
		linkEl.on('click', Sonatype.repoServer.RepoServer.loginHandler, Sonatype.repoServer.RepoServer);
		linkEl.setStyle({
			'color': '#15428B',
			'cursor': 'pointer',
			'text-align': 'right'
		});		
	}	
});
