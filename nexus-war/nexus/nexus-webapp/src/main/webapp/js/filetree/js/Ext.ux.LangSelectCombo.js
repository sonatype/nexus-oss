// vim: ts=4:sw=4:nu:fdc=4:nospell
/**
 * Ext.ux.LangSelectCombo - Combo pre-configured for language selection
 * 
 * @author    Ing. Jozef Sakáloš <jsakalos@aariadne.com>
 * @copyright (c) 2008, by Ing. Jozef Sakáloš
 * @date      21. March 2008
 * @version   $Id: Ext.ux.LangSelectCombo.js 163 2008-04-10 23:30:42Z jozo $
 */

/*global Ext */

/**
 * @class Ext.ux.LangSelectCombo
 * @extends Ext.ux.IconCombo
 */

Ext.ux.LangSelectCombo = Ext.extend(Ext.ux.IconCombo, {
	selectLangText:'Select Language'
	,lazyRender:true
	,lazyInit:true
	,langVariable:'locale'
	,typeAhead:true
	,initComponent:function() {
		var langCode = Ext.state.Manager.getProvider() ? Ext.state.Manager.get(this.langVariable) : 'en_US'
		langCode = langCode ? langCode : 'en_US'
		Ext.apply(this, {
			store:new Ext.data.SimpleStore({
				id:0
				,fields:[
					 {name:'langCode', type:'string'}
					,{name:'langName', type:'string'}
					,{name:'langCls', type:'string'}
				]
				,data:[
					 ['cs_CZ', 'Český', 'ux-flag-cz']
					,['de_DE', 'Deutsch', 'ux-flag-de']
					,['fr_FR', 'French', 'ux-flag-fr']
					,['nl_NL', 'Dutch', 'ux-flag-nl']
					,['en_US', 'English', 'ux-flag-us']
					,['ru_RU', 'Russian', 'ux-flag-ru']
					,['sk_SK', 'Slovenský', 'ux-flag-sk']
					,['es_ES', 'Spanish', 'ux-flag-es']
					,['tr_TR', 'Turkish', 'ux-flag-tr']
				]
			})
			,valueField:'langCode'
			,displayField:'langName'
			,iconClsField:'langCls'
			,triggerAction:'all'
			,mode:'local'
			,forceSelection:true
			,value:langCode

		}) // eo apply

		// call parent
		Ext.ux.LangSelectCombo.superclass.initComponent.apply(this, arguments);

	} // eo function initComponent

	,onSelect:function(record) {
		// call parent
		Ext.ux.LangSelectCombo.superclass.onSelect.apply(this, arguments);

		var langCode = record.get('langCode');
		// save state to state manager
		if(Ext.state.Manager.getProvider()) {
			Ext.state.Manager.set(this.langVariable, langCode);
		}

		// reload page
		window.location.search = this.langVariable + '=' + langCode;

	} // eo function onSelect

}) // eo extend

// eof
