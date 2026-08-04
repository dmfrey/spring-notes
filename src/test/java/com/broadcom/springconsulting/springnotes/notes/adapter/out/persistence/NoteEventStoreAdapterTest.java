package com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence;

import com.broadcom.springconsulting.springnotes.TestcontainersConfiguration;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import com.broadcom.springconsulting.springnotes.notes.configuration.NotesConfiguration;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
@Import( { NotesConfiguration.class, TestcontainersConfiguration.class } )
class NoteEventStoreAdapterTest {

    static final String OWNER = "user-sub-1";

    @Autowired
    DataSource dataSource;

    NoteEventStoreAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new NoteEventStoreAdapter( new NamedParameterJdbcTemplate( dataSource ), JsonMapper.builder().build() );
    }

    @Test
    void loadEvents_withNoEvents_returnsEmptyList() {

        var events = adapter.loadEvents( UuidCreator.getTimeOrderedEpoch() );

        assertThat( events ).isEmpty();

    }

    @Test
    void append_thenLoadEvents_returnsAppendedEvent() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteCreated( id, OWNER, "My Title", "Some content", Instant.now() );

        adapter.append( event, OWNER );

        var events = adapter.loadEvents( id );

        assertThat( events ).containsExactly( event );

    }

    @Test
    void append_multipleEvents_returnsThemInSequenceOrder() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var created = new NoteCreated( id, OWNER, "Title", "Content", Instant.now() );
        var updated = new NoteUpdated( id, "New Title", "New content", Instant.now() );
        var deleted = new NoteDeleted( id, Instant.now() );

        adapter.append( created, OWNER );
        adapter.append( updated, OWNER );
        adapter.append( deleted, OWNER );

        var events = adapter.loadEvents( id );

        assertThat( events ).containsExactly( created, updated, deleted );

    }

    @Test
    void loadEvents_onlyReturnsEventsForRequestedAggregate() {

        var id1 = UuidCreator.getTimeOrderedEpoch();
        var id2 = UuidCreator.getTimeOrderedEpoch();
        var event1 = new NoteCreated( id1, OWNER, "Note 1", "Content 1", Instant.now() );
        var event2 = new NoteCreated( id2, OWNER, "Note 2", "Content 2", Instant.now() );

        adapter.append( event1, OWNER );
        adapter.append( event2, OWNER );

        var events = adapter.loadEvents( id1 );

        assertThat( events ).containsExactly( event1 );

    }

}
