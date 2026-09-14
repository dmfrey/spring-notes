package com.broadcom.springconsulting.springnotes.notes.application.domain.model;

import java.util.UUID;

public class ChecklistItemNotFoundException extends RuntimeException {

    public ChecklistItemNotFoundException( UUID noteId, UUID itemId ) {
        super( "Checklist item not found: " + itemId + " on note " + noteId );
    }

}
