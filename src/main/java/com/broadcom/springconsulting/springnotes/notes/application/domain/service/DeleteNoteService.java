package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.DeleteNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.DeleteNotePort;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
class DeleteNoteService implements DeleteNoteUseCase {

    private static final Logger log = LoggerFactory.getLogger( DeleteNoteService.class );

    private final DeleteNotePort deleteNotePort;
    private final AppendNoteEventPort appendNoteEventPort;
    private final ObservationRegistry observationRegistry;

    DeleteNoteService( DeleteNotePort deleteNotePort, AppendNoteEventPort appendNoteEventPort, ObservationRegistry observationRegistry ) {
        this.deleteNotePort = deleteNotePort;
        this.appendNoteEventPort = appendNoteEventPort;
        this.observationRegistry = observationRegistry;
    }

    @Override
    @Transactional
    public void execute( DeleteNoteCommand command ) {
        log.debug( "Deleting note {}", command.id() );

        Observation.createNotStarted( "notes.delete", observationRegistry )
                .observe( () -> {
                    if ( !deleteNotePort.deleteNote( command.id(), command.owner() ) ) {
                        throw new NoteNotFoundException( command.id() );
                    }

                    appendNoteEventPort.append( new NoteDeleted( command.id(), Instant.now() ), command.owner() );
                } );
    }

}
