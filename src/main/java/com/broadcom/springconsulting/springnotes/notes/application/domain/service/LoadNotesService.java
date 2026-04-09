package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNotesPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class LoadNotesService implements LoadNotesUseCase {

    private static final Logger log = LoggerFactory.getLogger( LoadNotesService.class );

    private final LoadNotesPort loadNotesPort;

    LoadNotesService( LoadNotesPort loadNotesPort ) {
        this.loadNotesPort = loadNotesPort;
    }

    @Override
    public NoteSlice execute( LoadNotesCommand command ) {
        log.debug( "Loading notes with cursor {} and limit {}", command.cursor(), command.limit() );

        return loadNotesPort.loadNotes( command.cursor(), command.limit() );
    }

}