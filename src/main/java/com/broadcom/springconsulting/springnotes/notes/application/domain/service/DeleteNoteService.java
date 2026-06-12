package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.port.in.DeleteNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.DeleteNotePort;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class DeleteNoteService implements DeleteNoteUseCase {

    private static final Logger log = LoggerFactory.getLogger( DeleteNoteService.class );

    private final DeleteNotePort deleteNotePort;
    private final ObservationRegistry observationRegistry;

    DeleteNoteService( DeleteNotePort deleteNotePort, ObservationRegistry observationRegistry ) {
        this.deleteNotePort = deleteNotePort;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public void execute( DeleteNoteCommand command ) {
        log.debug( "Deleting note {}", command.id() );

        Observation.createNotStarted( "notes.delete", observationRegistry )
                .observe( () -> deleteNotePort.deleteNote( command.id() ) );
    }

}
