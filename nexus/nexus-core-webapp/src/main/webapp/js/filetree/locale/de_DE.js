/**
 * FileTree Translation : German de_DE.UTF-8
 *
 * @author  Ing. Jozef Sakáloš
 * @translator René Bartholomay
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
        jsonErrorText:'JSON-Objekt konnte nicht dekodiert werden',
        unknownErrorText:'Unbekannter Fehler'
    });
}

if(Ext.ux.UploadPanel){
    Ext.apply(Ext.ux.UploadPanel.prototype, {
        addText:'Hinzufügen',
        clickRemoveText:'zum Entfernen klicken',
        clickStopText:'zum Abbrechen klicken',
        emptyText:'keine Dateien',
        errorText:'Fehler',
        fileQueuedText:'Datei <b>{0}</b> wurde für die Übertragung hinzugefügt' ,
        fileDoneText:'Datei <b>{0}</b> wurde erfolgreich übertragen',
        fileFailedText:'Datei <b>{0}</b> wurde nicht übertragen',
        fileStoppedText:'Übertragung der Datei <b>{0}</b> wurde vom Benutzer abgebrochen',
        fileUploadingText:'Übertrage Datei <b>{0}</b>',
        removeAllText:'Alles entfernen',
        removeText:'Entfernen',
        stopAllText:'Alles abbrechen',
        uploadText:'Übertragen'
    });
}

if(Ext.ux.FileTreeMenu){
    Ext.apply(Ext.ux.FileTreeMenu.prototype, {
        collapseText: 'alle zuklappen',
        deleteKeyName:'Lösche Schlüssel',
        deleteText:'Löschen',
        expandText: 'alle aufklappen',
        newdirText:'Neuer Ordner',
        openBlankText:'In neuem Fenster öffnen',
        openDwnldText:'Download',
        openPopupText:'In Popup öffnen',
        openSelfText:'In diesem Fenster öffnen',
        openText:'Öffnen',
        reloadText:'Aktualisi<span style="text-decoration:underline">e</span>ren',
        renameText: 'Umbenennen',
        uploadFileText:'Datei <span style="text-decoration:underline">ü</span>bertragen',
        uploadText:'Übertragen'
    });
}

if(Ext.ux.FileTreePanel){
    Ext.apply(Ext.ux.FileTreePanel.prototype, {
        confirmText:'Bestätigen',
        deleteText:'Löschen',
        errorText:'Fehler',
        existsText:'Datei <b>{0}</b> ist schon vorhanden',
        fileText:'Datei',
        newdirText:'Neuer Ordner',
        overwriteText:'Soll die Datei überschrieben werden?',
        reallyWantText:'Wollen Sie wirklich : ',
        rootText:'Wurzelverzeichnis'
    });
}

// eof
