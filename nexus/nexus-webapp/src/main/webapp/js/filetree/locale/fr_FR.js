/**
 * FileTree Translation : french fr_FR
 *
 * @author  Tennaxia
 * @translator   christophe blin
 * @license FileTree Translation file is licensed under the terms of
 * the Open Source LGPL 3.0 license.  Commercial use is permitted to the extent
 * that the code/component(s) do NOT become part of another Open Source or Commercially
 * licensed development library or toolkit without explicit permission.
 * 
 * License details: http://www.gnu.org/licenses/lgpl.html
 */
if(Ext.ux.FileUploader){
    Ext.apply(Ext.ux.FileUploader.prototype, {
        jsonErrorText:'Impossible de lire la réponse du serveur',
        unknownErrorText:'Erreur inconnue'
    });
}

if(Ext.ux.UploadPanel){
    Ext.apply(Ext.ux.UploadPanel.prototype, {
        addText:'Ajouter',
        clickRemoveText:'Cliquer pour supprimer',
        clickStopText:'Cliquer pour arrêter l\'envoi',
        emptyText:'Pas de fichiers',
        errorText:'Erreur',
        fileQueuedText:'Le fichier <b>{0}</b> est prêt pour l\'envoi' ,
        fileDoneText:'Le fichier <b>{0}</b> a été envoyé',
        fileFailedText:'Le fichier <b>{0}</b> n\'a pu être envoyé',
        fileStoppedText:'Le fichier <b>{0}</b> a été annulé par l\'utilisateur',
        fileUploadingText:'Envoi en cours <b>{0}</b>',
        removeAllText:'Tout supprimer',
        removeText:'Supprimer',
        stopAllText:'Arrêter tous les envois',
        uploadText:'Envoyer'
    });
}

if(Ext.ux.FileTreeMenu){
    Ext.apply(Ext.ux.FileTreeMenu.prototype, {
    collapseText: 'Tout réduire',
    deleteKeyName:'Touche supprimer',
    deleteText:'Supprimer',
    expandText: 'Tout étendre',
    newdirText:'Nouveau dossier',
    openBlankText:'Ouvrir dans une nouvelle fenêtre',
    openDwnldText:'Télécharger',
    openPopupText:'Ouvrir dans une fenêtre',
    openSelfText:'Ouvrir dans c ette fenêtre',
    openText:'Ouvrir',
    reloadText:'Actualiser',
    renameText: 'Renommer',
    uploadFileText:'Envoyer un fichier',
    uploadText:'Envoyer'
    });
}

if(Ext.ux.FileTreePanel){
    Ext.apply(Ext.ux.FileTreePanel.prototype, {
        confirmText:'Confirmer',
        deleteText:'Supprimer',
        errorText:'Erreur',
        existsText:'Le fichier <b>{0}</b> existe déjà',
        fileText:'Fichier',
        newdirText:'Nouveau dossier',
        overwriteText:'Etes-vous sûr de vouloir remplacer le fichier ?',
        reallyWantText:'Est-ce que vous voulez vraiment ',
        rootText:'Dossier racine'
    });
}
