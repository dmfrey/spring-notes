package com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence;

import com.broadcom.springconsulting.springnotes.DataTestcontainersConfiguration;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNotesMissingEventsPort;
import com.broadcom.springconsulting.springnotes.notes.configuration.NotesConfiguration;
import com.github.f4b6a3.uuid.UuidCreator;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
@Import( { NotesConfiguration.class, DataTestcontainersConfiguration.class } )
class NotesPersistenceAdapterTest {

    static final String OWNER = "user-sub-1";
    static final String OTHER_OWNER = "user-sub-2";

    @MockitoBean
    ObservationRegistry observationRegistry;

    @Autowired
    NotesRepository notesRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    ObjectMapper objectMapper;

    NotesPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        notesRepository.deleteAll();
        adapter = new NotesPersistenceAdapter( notesRepository, namedParameterJdbcTemplate, objectMapper );
    }

    @Test
    void loadNotes_emptyTable_returnsEmptySlice() {

        var slice = adapter.loadNotes( OWNER, null, 25 );

        assertThat( slice.notes() ).isEmpty();
        assertThat( slice.nextCursor() ).isNull();

    }

    @Test
    void loadNotes_firstPage_returnsNotesOrderedById() {

        UUID id1 = UuidCreator.getTimeOrderedEpoch();
        UUID id2 = UuidCreator.getTimeOrderedEpoch();
        UUID id3 = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( id1, "Note 1", "Content 1", NoteType.TEXT, OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id2, "Note 2", "Content 2", NoteType.TEXT, OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id3, "Note 3", "Content 3", NoteType.TEXT, OWNER, null, null, null, null ) );

        var slice = adapter.loadNotes( OWNER, null, 25 );

        assertThat( slice.notes() ).hasSize( 3 );
        assertThat( slice.notes().get( 0 ).id() ).isEqualTo( id1 );
        assertThat( slice.notes().get( 1 ).id() ).isEqualTo( id2 );
        assertThat( slice.notes().get( 2 ).id() ).isEqualTo( id3 );
        assertThat( slice.nextCursor() ).isNull();

    }

    @Test
    void loadNotes_withCursor_returnsNotesAfterCursor() {

        UUID id1 = UuidCreator.getTimeOrderedEpoch();
        UUID id2 = UuidCreator.getTimeOrderedEpoch();
        UUID id3 = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( id1, "Note 1", "Content 1", NoteType.TEXT, OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id2, "Note 2", "Content 2", NoteType.TEXT, OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id3, "Note 3", "Content 3", NoteType.TEXT, OWNER, null, null, null, null ) );

        var slice = adapter.loadNotes( OWNER, id1, 25 );

        assertThat( slice.notes() ).hasSize( 2 );
        assertThat( slice.notes().get( 0 ).id() ).isEqualTo( id2 );
        assertThat( slice.notes().get( 1 ).id() ).isEqualTo( id3 );
        assertThat( slice.nextCursor() ).isNull();

    }

    @Test
    void loadNotes_whenResultsFillPage_includesNextCursor() {

        UUID id1 = UuidCreator.getTimeOrderedEpoch();
        UUID id2 = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( id1, "Note 1", "Content 1", NoteType.TEXT, OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id2, "Note 2", "Content 2", NoteType.TEXT, OWNER, null, null, null, null ) );

        var slice = adapter.loadNotes( OWNER, null, 2 );

        assertThat( slice.notes() ).hasSize( 2 );
        assertThat( slice.nextCursor() ).isEqualTo( id2 );

    }

    @Test
    void loadNotes_limitedPage_returnsOnlyRequestedCount() {

        for( int i = 0; i < 5; i++ ) {
            notesRepository.save( new NoteEntity( UuidCreator.getTimeOrderedEpoch(), "Note " + i, "Content " + i, NoteType.TEXT, OWNER, null, null, null, null ) );
        }

        var slice = adapter.loadNotes( OWNER, null, 3 );

        assertThat( slice.notes() ).hasSize( 3 );
        assertThat( slice.nextCursor() ).isNotNull();

    }

    @Test
    void loadNotes_onlyReturnsNotesForOwner() {

        UUID id1 = UuidCreator.getTimeOrderedEpoch();
        UUID id2 = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( id1, "My Note", "My content", NoteType.TEXT, OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id2, "Other Note", "Other content", NoteType.TEXT, OTHER_OWNER, null, null, null, null ) );

        var slice = adapter.loadNotes( OWNER, null, 25 );

        assertThat( slice.notes() ).hasSize( 1 );
        assertThat( slice.notes().get( 0 ).id() ).isEqualTo( id1 );

    }

    @Test
    void saveNote_persistsNoteAndReturnsItWithGeneratedId() {

        var note = adapter.saveNote( OWNER, "New Note", "Note content", NoteType.TEXT, List.of() );

        assertThat( note.id() ).isNotNull();
        assertThat( note.title() ).isEqualTo( "New Note" );
        assertThat( note.content() ).isEqualTo( "Note content" );
        assertThat( notesRepository.findById( note.id() ) ).isPresent();

    }

    @Test
    void saveNote_appearsInSubsequentLoadResults() {

        adapter.saveNote( OWNER, "Saved Note", "Saved content", NoteType.TEXT, List.of() );

        var slice = adapter.loadNotes( OWNER, null, 25 );

        assertThat( slice.notes() ).hasSize( 1 );
        assertThat( slice.notes().get( 0 ).title() ).isEqualTo( "Saved Note" );

    }

    @Test
    void deleteNote_removesNoteFromDatabase() {

        UUID id = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( id, "To Delete", "Content", NoteType.TEXT, OWNER, null, null, null, null ) );

        boolean deleted = adapter.deleteNote( id, OWNER );

        assertThat( deleted ).isTrue();
        assertThat( notesRepository.findById( id ) ).isEmpty();

    }

    @Test
    void deleteNote_onlyDeletesSpecifiedNote() {

        UUID id1 = UuidCreator.getTimeOrderedEpoch();
        UUID id2 = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( id1, "Note 1", "Content 1", NoteType.TEXT, OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id2, "Note 2", "Content 2", NoteType.TEXT, OWNER, null, null, null, null ) );

        adapter.deleteNote( id1, OWNER );

        assertThat( notesRepository.findById( id1 ) ).isEmpty();
        assertThat( notesRepository.findById( id2 ) ).isPresent();

    }

    @Test
    void deleteNote_whenOwnerDoesNotMatch_doesNotDeleteAndReturnsFalse() {

        UUID id = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( id, "Not Yours", "Content", NoteType.TEXT, OWNER, null, null, null, null ) );

        boolean deleted = adapter.deleteNote( id, "someone-else" );

        assertThat( deleted ).isFalse();
        assertThat( notesRepository.findById( id ) ).isPresent();

    }

    @Test
    void updateProjection_updatesTitleAndContent() {

        UUID id = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( id, "Original Title", "Original content", NoteType.TEXT, OWNER, null, null, null, null ) );

        adapter.updateProjection( id, "New Title", "New content", OWNER, Instant.now() );

        var updated = notesRepository.findById( id ).orElseThrow();
        assertThat( updated.title() ).isEqualTo( "New Title" );
        assertThat( updated.content() ).isEqualTo( "New content" );

    }

    @Test
    void updateProjection_withMismatchedOwner_doesNotUpdate() {

        UUID id = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( id, "Original Title", "Original content", NoteType.TEXT, OWNER, null, null, null, null ) );

        adapter.updateProjection( id, "New Title", "New content", OTHER_OWNER, Instant.now() );

        var unchanged = notesRepository.findById( id ).orElseThrow();
        assertThat( unchanged.title() ).isEqualTo( "Original Title" );
        assertThat( unchanged.content() ).isEqualTo( "Original content" );

    }

    @Test
    void loadNotesMissingEvents_returnsOnlyNotesWithoutAnEvent() {

        UUID withEvent = UuidCreator.getTimeOrderedEpoch();
        UUID withoutEvent = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( withEvent, "Has Event", "Content", NoteType.TEXT, OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( withoutEvent, "No Event", "Content", NoteType.TEXT, OWNER, null, null, null, null ) );

        jdbcTemplate.update(
                "INSERT INTO note_events (id, aggregate_id, owner, type, payload, sequence_number, occurred_at) " +
                        "VALUES (?, ?, ?, 'NoteCreated', '{}'::jsonb, 1, now())",
                UuidCreator.getTimeOrderedEpoch(), withEvent, OWNER
        );

        var missing = adapter.loadNotesMissingEvents();

        assertThat( missing )
                .extracting( LoadNotesMissingEventsPort.NoteSnapshot::id )
                .containsExactly( withoutEvent );

    }

    @Test
    void loadNotesMissingEvents_withNoNotesAtAll_returnsEmpty() {

        assertThat( adapter.loadNotesMissingEvents() ).isEmpty();

    }

    // First Spring Data JDBC JSONB write/read in this codebase (items is handled via
    // hand-rolled NamedParameterJdbcTemplate, not the entity mapping) - exercised against real
    // Postgres rather than trusted from a converter unit test, per this repo's own scepticism
    // about untested JSONB-binding assumptions.
    @Test
    void saveNote_withListTypeAndItems_persistsAndReturnsItems() {

        var items = List.of(
                new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", false ),
                new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Eggs", true )
        );

        var note = adapter.saveNote( OWNER, "Groceries", null, NoteType.LIST, items );

        assertThat( note.type() ).isEqualTo( NoteType.LIST );
        assertThat( note.items() ).isEqualTo( items );

    }

    @Test
    void saveNote_withListTypeAndItems_itemsSurviveAReload() {

        var items = List.of( new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", false ) );
        var saved = adapter.saveNote( OWNER, "Groceries", null, NoteType.LIST, items );

        var slice = adapter.loadNotes( OWNER, null, 25 );

        assertThat( slice.notes() )
                .filteredOn( n -> n.id().equals( saved.id() ) )
                .singleElement()
                .satisfies( n -> {
                    assertThat( n.type() ).isEqualTo( NoteType.LIST );
                    assertThat( n.items() ).isEqualTo( items );
                } );

    }

    @Test
    void loadNotes_withTextNote_returnsEmptyItems() {

        adapter.saveNote( OWNER, "Plain Note", "Some content", NoteType.TEXT, List.of() );

        var slice = adapter.loadNotes( OWNER, null, 25 );

        assertThat( slice.notes() ).singleElement().satisfies( n -> {
            assertThat( n.type() ).isEqualTo( NoteType.TEXT );
            assertThat( n.items() ).isEmpty();
        } );

    }

    @Test
    void updateItems_replacesItemsAndSurvivesAReload() {

        var original = List.of( new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", false ) );
        var saved = adapter.saveNote( OWNER, "Groceries", null, NoteType.LIST, original );

        var updated = List.of(
                new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", true ),
                new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Bread", false )
        );
        adapter.updateItems( saved.id(), updated, OWNER, Instant.now() );

        var slice = adapter.loadNotes( OWNER, null, 25 );

        assertThat( slice.notes() ).singleElement().satisfies( n -> assertThat( n.items() ).isEqualTo( updated ) );

    }

    @Test
    void updateItems_withMismatchedOwner_doesNotUpdate() {

        var original = List.of( new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", false ) );
        var saved = adapter.saveNote( OWNER, "Groceries", null, NoteType.LIST, original );

        adapter.updateItems( saved.id(), List.of(), OTHER_OWNER, Instant.now() );

        var slice = adapter.loadNotes( OWNER, null, 25 );

        assertThat( slice.notes() ).singleElement().satisfies( n -> assertThat( n.items() ).isEqualTo( original ) );

    }

}