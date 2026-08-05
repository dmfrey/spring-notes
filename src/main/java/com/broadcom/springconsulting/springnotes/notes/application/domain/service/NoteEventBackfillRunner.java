package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNotesMissingEventsPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

// One-time backfill for notes created before event sourcing existed (Phase 0): synthesizes a
// NoteCreated event for every note that has none, so GET /notes/{id}/events and
// NoteAggregate.hydrate() work for pre-migration data too, not just notes created afterward.
// Safe to run on every startup indefinitely - loadNotesMissingEvents() only ever returns notes
// that still lack an event, so this becomes a no-op the moment every note has one (which is
// immediately true for any note created after this class was deployed, since Create already
// dual-writes its own event).
@Component
class NoteEventBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger( NoteEventBackfillRunner.class );

    private final LoadNotesMissingEventsPort loadNotesMissingEventsPort;
    private final AppendNoteEventPort appendNoteEventPort;

    NoteEventBackfillRunner( LoadNotesMissingEventsPort loadNotesMissingEventsPort, AppendNoteEventPort appendNoteEventPort ) {
        this.loadNotesMissingEventsPort = loadNotesMissingEventsPort;
        this.appendNoteEventPort = appendNoteEventPort;
    }

    @Override
    @Transactional
    public void run( ApplicationArguments args ) {
        var notes = loadNotesMissingEventsPort.loadNotesMissingEvents();

        if ( notes.isEmpty() ) {
            return;
        }

        log.info( "Backfilling NoteCreated events for {} pre-existing note(s)", notes.size() );

        for ( var note : notes ) {
            // createdDate is nullable (added in a later changeset than the notes table itself),
            // so very old rows may not have one - Instant.now() is an honest fallback, since we
            // genuinely don't know when they were really created.
            var occurredAt = note.createdDate() != null ? note.createdDate() : Instant.now();
            var event = new NoteCreated( note.id(), note.owner(), note.title(), note.content(), occurredAt );

            appendNoteEventPort.append( event, note.owner() );
        }
    }

}
