package com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence;

import com.broadcom.springconsulting.springnotes.TestcontainersConfiguration;
import com.broadcom.springconsulting.springnotes.notes.configuration.NotesConfiguration;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase( replace = AutoConfigureTestDatabase.Replace.NONE )
@Import( { NotesConfiguration.class, TestcontainersConfiguration.class } )
class NotesPersistenceAdapterTest {

    static final String OWNER = "user-sub-1";
    static final String OTHER_OWNER = "user-sub-2";

    @Autowired
    NotesRepository notesRepository;

    NotesPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        notesRepository.deleteAll();
        adapter = new NotesPersistenceAdapter( notesRepository );
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
        notesRepository.save( new NoteEntity( id1, "Note 1", "Content 1", OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id2, "Note 2", "Content 2", OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id3, "Note 3", "Content 3", OWNER, null, null, null, null ) );

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
        notesRepository.save( new NoteEntity( id1, "Note 1", "Content 1", OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id2, "Note 2", "Content 2", OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id3, "Note 3", "Content 3", OWNER, null, null, null, null ) );

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
        notesRepository.save( new NoteEntity( id1, "Note 1", "Content 1", OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id2, "Note 2", "Content 2", OWNER, null, null, null, null ) );

        var slice = adapter.loadNotes( OWNER, null, 2 );

        assertThat( slice.notes() ).hasSize( 2 );
        assertThat( slice.nextCursor() ).isEqualTo( id2 );

    }

    @Test
    void loadNotes_limitedPage_returnsOnlyRequestedCount() {

        for( int i = 0; i < 5; i++ ) {
            notesRepository.save( new NoteEntity( UuidCreator.getTimeOrderedEpoch(), "Note " + i, "Content " + i, OWNER, null, null, null, null ) );
        }

        var slice = adapter.loadNotes( OWNER, null, 3 );

        assertThat( slice.notes() ).hasSize( 3 );
        assertThat( slice.nextCursor() ).isNotNull();

    }

    @Test
    void loadNotes_onlyReturnsNotesForOwner() {

        UUID id1 = UuidCreator.getTimeOrderedEpoch();
        UUID id2 = UuidCreator.getTimeOrderedEpoch();
        notesRepository.save( new NoteEntity( id1, "My Note", "My content", OWNER, null, null, null, null ) );
        notesRepository.save( new NoteEntity( id2, "Other Note", "Other content", OTHER_OWNER, null, null, null, null ) );

        var slice = adapter.loadNotes( OWNER, null, 25 );

        assertThat( slice.notes() ).hasSize( 1 );
        assertThat( slice.notes().get( 0 ).id() ).isEqualTo( id1 );

    }

}