/**
 * FileTree Translation : Slovak sk_SK
 *
 * @author     Ing. Jozef Sakáloš
 * @translator Ing. Jozef Sakáloš
 * @date       21. March 2008
 *
 *
 * @license FileTree Translation file is licensed under the terms of
 * the Open Source LGPL 3.0 license.  Commercial use is permitted to the extent
 * that the code/component(s) do NOT become part of another Open Source or Commercially
 * licensed development library or toolkit without explicit permission.
 * 
 * License details: http://www.gnu.org/licenses/lgpl.html
 */
if(Ext.ux.FileUploader){
    Ext.override(Ext.ux.FileUploader, {
        jsonErrorText:'JSON objekt sa nedá dekódovať',
        unknownErrorText:'Neznáma chyba'
    });
}

if(Ext.ux.UploadPanel){
    Ext.override(Ext.ux.UploadPanel, {
        addText:'Pridať',
        clickRemoveText:'Klikni na odobratie',
        clickStopText:'Klikni na zastavenie',
        emptyText:'Žiadne súbory',
        errorText:'Chyba',
        fileQueuedText:'Súbor <b>{0}</b> je pripravený na odoslanie' ,
        fileDoneText:'Súbor <b>{0}</b> bol úspešne odoslaný',
        fileFailedText:'Odosielanie súboru <b>{0}</b> zlyhalo',
        fileStoppedText:'Odosielanie súboru <b>{0}</b> zastavené užívateľom',
        fileUploadingText:'Odosielanie súboru <b>{0}</b>',
        removeAllText:'Odobrať všetky',
        removeText:'Odobrať',
        stopAllText:'Zastaviť všetky',
        uploadText:'Odoslať'
    });
}

if(Ext.ux.FileTreeMenu){
    Ext.override(Ext.ux.FileTreeMenu, {
    collapseText: 'Zbaliť všetky',
    deleteKeyName:'Kláves Delete',
    deleteText:'Vymazať',
    expandText: 'Rozbaliť všetky',
    newdirText:'<span style="text-decoration:underline">N</span>ová zložka',
    openBlankText:'Otvoriť v novom okne',
    openDwnldText:'Stiahnuť',
    openPopupText:'Otvoriť v dialógu',
    openSelfText:'Otvoriť v tomto okne',
    openText:'Otvoriť',
    reloadText:'Aktualizovať',
    renameText: 'Premenovať',
    uploadFileText:'Odoslať s<span style="text-decoration:underline">ú</span>bor',
    uploadText:'Odoslať'
    });
}

if(Ext.ux.FileTreePanel){
    Ext.override(Ext.ux.FileTreePanel, {
        confirmText:'Potvrdiť',
        deleteText:'Vymazať',
        errorText:'Chyba',
        existsText:'Súbor <b>{0}</b> už existuje',
        fileText:'Súbor',
        newdirText:'Nová zložka',
        overwriteText:'Chceš ho prepísať?',
        reallyWantText:'Naozaj chceš',
        rootText:'Koreň stromu'
    });
}

// eof
