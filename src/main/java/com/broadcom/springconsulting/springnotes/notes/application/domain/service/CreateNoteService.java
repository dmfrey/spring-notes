package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.CreateNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.SaveNotePort;
import com.github.f4b6a3.uuid.UuidCreator;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
class CreateNoteService implements CreateNoteUseCase {

    private static final Logger log = LoggerFactory.getLogger( CreateNoteService.class );

    private final SaveNotePort saveNotePort;
    private final AppendNoteEventPort appendNoteEventPort;
    private final ObservationRegistry observationRegistry;

    CreateNoteService( SaveNotePort saveNotePort, AppendNoteEventPort appendNoteEventPort, ObservationRegistry observationRegistry ) {
        this.saveNotePort = saveNotePort;
        this.appendNoteEventPort = appendNoteEventPort;
        this.observationRegistry = observationRegistry;
    }

    @Override
    @Transactional
    public Note execute( CreateNoteCommand command ) {
        log.debug( "Creating note for owner {}", command.owner() );

        return Observation.createNotStarted( "notes.create", observationRegistry )
                .observe( () -> {
                    var items = command.items().stream()
                            .map( text -> new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), text, false ) )
                            .toList();

                    var note = saveNotePort.saveNote( command.owner(), command.title(), command.content(), command.type(), items );

                    appendNoteEventPort.append(
                            new NoteCreated( note.id(), command.owner(), command.title(), command.content(), command.type(), items, Instant.now() ),
                            command.owner()
                    );

                    return note;
                } );
    }

}
