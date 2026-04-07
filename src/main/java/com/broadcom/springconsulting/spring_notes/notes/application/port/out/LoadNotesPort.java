package com.broadcom.springconsulting.spring_notes.notes.application.port.out;

import com.broadcom.springconsulting.spring_notes.notes.application.domain.model.NoteSlice;

import java.util.UUID;

public interface LoadNotesPort {

    NoteSlice loadNotes( UUID cursor, int limit );

}