package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadUnpublishedNoteEventsPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadUnpublishedNoteEventsPort.StoredNoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.MarkNoteEventPublishedPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.PublishNoteEventPort;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class NoteEventOutboxPollerTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    LoadUnpublishedNoteEventsPort loadUnpublishedNoteEventsPort;

    @Mock
    PublishNoteEventPort publishNoteEventPort;

    @Mock
    MarkNoteEventPublishedPort markNoteEventPublishedPort;

    NoteEventOutboxPoller poller;

    @BeforeEach
    void setUp() {
        poller = new NoteEventOutboxPoller( loadUnpublishedNoteEventsPort, publishNoteEventPort, markNoteEventPublishedPort );
    }

    @Test
    void publishPendingEvents_withNoPendingEvents_doesNothing() {

        when( loadUnpublishedNoteEventsPort.loadUnpublished( anyInt() ) ).thenReturn( List.of() );

        poller.publishPendingEvents();

        verifyNoInteractions( publishNoteEventPort, markNoteEventPublishedPort );

    }

    @Test
    void publishPendingEvents_publishesThenMarksEachEvent() {

        var eventId = UuidCreator.getTimeOrderedEpoch();
        var noteId = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteCreated( noteId, TEST_OWNER, "Title", "Content", Instant.now() );
        var stored = new StoredNoteEvent( eventId, event );
        when( loadUnpublishedNoteEventsPort.loadUnpublished( anyInt() ) ).thenReturn( List.of( stored ) );

        poller.publishPendingEvents();

        InOrder order = inOrder( publishNoteEventPort, markNoteEventPublishedPort );
        order.verify( publishNoteEventPort ).publish( eventId, event );
        order.verify( markNoteEventPublishedPort ).markPublished( eventId );

    }

    @Test
    void publishPendingEvents_publishesAllEventsInBatch() {

        var stored1 = new StoredNoteEvent(
                UuidCreator.getTimeOrderedEpoch(),
                new NoteCreated( UuidCreator.getTimeOrderedEpoch(), TEST_OWNER, "Title 1", "Content 1", Instant.now() )
        );
        var stored2 = new StoredNoteEvent(
                UuidCreator.getTimeOrderedEpoch(),
                new NoteCreated( UuidCreator.getTimeOrderedEpoch(), TEST_OWNER, "Title 2", "Content 2", Instant.now() )
        );
        when( loadUnpublishedNoteEventsPort.loadUnpublished( anyInt() ) ).thenReturn( List.of( stored1, stored2 ) );

        poller.publishPendingEvents();

        verify( publishNoteEventPort ).publish( stored1.eventId(), stored1.event() );
        verify( markNoteEventPublishedPort ).markPublished( stored1.eventId() );
        verify( publishNoteEventPort ).publish( stored2.eventId(), stored2.event() );
        verify( markNoteEventPublishedPort ).markPublished( stored2.eventId() );

    }

}
