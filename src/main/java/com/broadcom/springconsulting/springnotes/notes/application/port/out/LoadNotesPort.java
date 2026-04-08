package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;

import java.util.UUID;

public interface LoadNotesPort {

    NoteSlice loadNotes( UUID cursor, int limit );

}