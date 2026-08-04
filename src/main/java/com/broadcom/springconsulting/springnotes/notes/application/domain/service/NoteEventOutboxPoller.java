package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadUnpublishedNoteEventsPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.MarkNoteEventPublishedPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.PublishNoteEventPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Polls note_events for rows not yet published to the broker and publishes them. Assumes a
// single poller instance; concurrent instances could double-publish the same event, which is
// safe as long as consumers are idempotent (this is at-least-once delivery, same as the
// outbox pattern generally provides).
@Component
class NoteEventOutboxPoller {

    private static final Logger log = LoggerFactory.getLogger( NoteEventOutboxPoller.class );
    private static final int BATCH_SIZE = 50;

    private final LoadUnpublishedNoteEventsPort loadUnpublishedNoteEventsPort;
    private final PublishNoteEventPort publishNoteEventPort;
    private final MarkNoteEventPublishedPort markNoteEventPublishedPort;

    NoteEventOutboxPoller(
            LoadUnpublishedNoteEventsPort loadUnpublishedNoteEventsPort,
            PublishNoteEventPort publishNoteEventPort,
            MarkNoteEventPublishedPort markNoteEventPublishedPort
    ) {
        this.loadUnpublishedNoteEventsPort = loadUnpublishedNoteEventsPort;
        this.publishNoteEventPort = publishNoteEventPort;
        this.markNoteEventPublishedPort = markNoteEventPublishedPort;
    }

    @Scheduled( fixedDelay = 5000 )
    void publishPendingEvents() {
        var pending = loadUnpublishedNoteEventsPort.loadUnpublished( BATCH_SIZE );

        for ( var stored : pending ) {
            publishNoteEventPort.publish( stored.eventId(), stored.event() );
            markNoteEventPublishedPort.markPublished( stored.eventId() );
        }

        if ( !pending.isEmpty() ) {
            log.debug( "Published {} pending note events", pending.size() );
        }
    }

}
