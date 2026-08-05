package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNoteHistoryUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNoteEventsPort;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class LoadNoteHistoryService implements LoadNoteHistoryUseCase {

    private static final Logger log = LoggerFactory.getLogger( LoadNoteHistoryService.class );

    private final LoadNoteEventsPort loadNoteEventsPort;
    private final ObservationRegistry observationRegistry;

    LoadNoteHistoryService( LoadNoteEventsPort loadNoteEventsPort, ObservationRegistry observationRegistry ) {
        this.loadNoteEventsPort = loadNoteEventsPort;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public List<NoteEvent> execute( LoadNoteHistoryCommand command ) {
        log.debug( "Loading history for note {}", command.id() );

        return Observation.createNotStarted( "notes.history", observationRegistry )
                .observe( () -> loadNoteEventsPort.loadEvents( command.id(), command.owner() ) );
    }

}
