package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNotesPort;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class LoadNotesService implements LoadNotesUseCase {

    private static final Logger log = LoggerFactory.getLogger( LoadNotesService.class );

    private final LoadNotesPort loadNotesPort;
    private final ObservationRegistry observationRegistry;

    LoadNotesService( LoadNotesPort loadNotesPort, ObservationRegistry observationRegistry ) {
        this.loadNotesPort = loadNotesPort;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public NoteSlice execute( LoadNotesCommand command ) {
        log.debug( "Loading notes with cursor {} and limit {}", command.cursor(), command.limit() );

        return Observation.createNotStarted( "notes.load", observationRegistry )
                .observe( () -> loadNotesPort.loadNotes( command.owner(), command.cursor(), command.limit() ) );
    }

}