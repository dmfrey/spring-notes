package com.broadcom.springconsulting.springnotes.notes.application.domain.model;

import java.util.UUID;

// Thrown for both "no note with this id" and "note exists but belongs to a different owner" -
// deliberately not distinguished, so a caller probing for other users' note ids can't tell the
// two apart from the response.
public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException( UUID id ) {
        super( "Note not found: " + id );
    }

}
