package com.broadcom.springconsulting.springnotes.notes.application.domain.model;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NoteAggregateTest {

    static final String TEST_OWNER = "test-user-sub";

    @Test
    void hydrate_withNoEvents_returnsEmpty() {

        var result = NoteAggregate.hydrate( List.of() );

        assertThat( result ).isEmpty();

    }

    @Test
    void hydrate_withOnlyCreatedEvent_returnsNote() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "My Title", "Some content", Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "My Title", "Some content" ) );

    }

    @Test
    void hydrate_withCreatedThenUpdated_returnsNoteWithUpdatedFields() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "My Title", "Some content", Instant.now() ),
                new NoteUpdated( id, "New Title", "New content", Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "New Title", "New content" ) );

    }

    @Test
    void hydrate_withCreatedThenDeleted_returnsEmpty() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "My Title", "Some content", Instant.now() ),
                new NoteDeleted( id, Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).isEmpty();

    }

    @Test
    void hydrate_withCreatedUpdatedThenDeleted_returnsEmpty() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "My Title", "Some content", Instant.now() ),
                new NoteUpdated( id, "New Title", "New content", Instant.now() ),
                new NoteDeleted( id, Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).isEmpty();

    }

    @Test
    void hydrate_withMultipleUpdates_returnsNoteWithLatestFields() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var events = List.<NoteEvent>of(
                new NoteCreated( id, TEST_OWNER, "My Title", "Some content", Instant.now() ),
                new NoteUpdated( id, "Second Title", "Second content", Instant.now() ),
                new NoteUpdated( id, "Third Title", "Third content", Instant.now() )
        );

        var result = NoteAggregate.hydrate( events );

        assertThat( result ).contains( new Note( id, "Third Title", "Third content" ) );

    }

}
