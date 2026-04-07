package com.broadcom.springconsulting.spring_notes.notes.application.domain.service;

import com.broadcom.springconsulting.spring_notes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.spring_notes.notes.application.port.in.LoadNotesUseCase;
import com.broadcom.springconsulting.spring_notes.notes.application.port.out.LoadNotesPort;
import org.springframework.stereotype.Service;

@Service
class LoadNotesService implements LoadNotesUseCase {

    private final LoadNotesPort loadNotesPort;

    LoadNotesService( LoadNotesPort loadNotesPort ) {
        this.loadNotesPort = loadNotesPort;
    }

    @Override
    public NoteSlice execute( LoadNotesCommand command ) {
        return loadNotesPort.loadNotes( command.cursor(), command.limit() );
    }

}