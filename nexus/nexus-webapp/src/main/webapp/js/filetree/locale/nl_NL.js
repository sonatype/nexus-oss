/**
 * FileTree Translation : Dutch nl_NL
 *
 * @author  Ing. Jozef Sakáloš
 *
 * @license FileTree Translation file is licensed under the terms of
 * the Open Source LGPL 3.0 license.  Commercial use is permitted to the extent
 * that the code/component(s) do NOT become part of another Open Source or Commercially
 * licensed development library or toolkit without explicit permission.
 * 
 * License details: http://www.gnu.org/licenses/lgpl.html
 */
if(Ext.ux.FileUploader){
    Ext.apply(Ext.ux.FileUploader.prototype, {
        jsonErrorText:'Fout bij decoderen JSON object',
        unknownErrorText:'Onbekende error'
    });
}

if(Ext.ux.UploadPanel){
    Ext.apply(Ext.ux.UploadPanel.prototype, {
        addText:'Toevoegen',
        clickRemoveText:'Klik om te verwijderen',
        clickStopText:'Klik om te stoppen',
        emptyText:'Geen bestanden',
        errorText:'Fout',
        fileQueuedText:'Bestand <b>{0}</b> staat in wachtrij voor upload' ,
        fileDoneText:'Bestand <b>{0}</b> is succesvol geupload',
        fileFailedText:'Bestand <b>{0}</b> is niet geupload',
        fileStoppedText:'Bestand <b>{0}</b> gestopt door gebruiker',
        fileUploadingText:'Uploaden bestand <b>{0}</b>',
        removeAllText:'Alles verwijderen',
        removeText:'Verwijderen',
        stopAllText:'Stop alles',
        uploadText:'Upload'
    });
}

if(Ext.ux.FileTreeMenu){
    Ext.apply(Ext.ux.FileTreeMenu.prototype, {
    collapseText: 'Alles inklappen',
    deleteKeyName:'Verwijder sleutel',
    deleteText:'Verwijderen',
    expandText: 'Alles uitklappen',
    newdirText:'Nieuwe map',
    openBlankText:'Openen in nieuw venster',
    openDwnldText:'Downloaden',
    openPopupText:'Openen in popup venster',
    openSelfText:'Openen in dit venster',
    openText:'Openen',
    reloadText:'H<span style="text-decoration:underline">e</span>rladen',
    renameText: 'Hernoemen',
    uploadFileText:'<span style="text-decoration:underline">U</span>pload bestand',
    uploadText:'Upload'
    });
}

if(Ext.ux.FileTreePanel){
    Ext.apply(Ext.ux.FileTreePanel.prototype, {
        confirmText:'Bevestig',
        deleteText:'Verwijderen',
        errorText:'Fout',
        existsText:'Bestand <b>{0}</b> bestaat al',
        fileText:'Bestand',
        newdirText:'Nieuwe map',
        overwriteText:'Wil je het overschrijven?',
        reallyWantText:'Weet je het zeker',
        rootText:'Hoofd map'
    });
}  
