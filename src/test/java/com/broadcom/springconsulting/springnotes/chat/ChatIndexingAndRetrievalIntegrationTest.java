package com.broadcom.springconsulting.springnotes.chat;

import com.broadcom.springconsulting.springnotes.TestcontainersConfiguration;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.IndexNotePort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.LoadNotesMissingIndexPort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.RemoveNoteIndexPort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.RetrieveRelevantNotesPort;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

// Full application context (real Testcontainers Ollama, generating real embeddings) rather than
// a narrow slice - PgVectorStore's autoconfiguration needs a real EmbeddingModel bean, and the
// whole point of these tests is proving the owner filter actually works against a real
// similarity search, not a mocked one.
@SpringBootTest
@Import( TestcontainersConfiguration.class )
class ChatIndexingAndRetrievalIntegrationTest {

    static final String OWNER = "user-sub-1";
    static final String OTHER_OWNER = "user-sub-2";

    @MockitoBean
    JwtDecoder jwtDecoder;

    @Autowired
    IndexNotePort indexNotePort;

    @Autowired
    RemoveNoteIndexPort removeNoteIndexPort;

    @Autowired
    RetrieveRelevantNotesPort retrieveRelevantNotesPort;

    @Autowired
    LoadNotesMissingIndexPort loadNotesMissingIndexPort;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute( "DELETE FROM vector_store" );
        jdbcTemplate.execute( "DELETE FROM notes" );
    }

    @Test
    void retrieve_onlyReturnsNotesBelongingToRequestingOwner() {

        indexNotePort.index( UuidCreator.getTimeOrderedEpoch(), OWNER, "Grocery List", "Milk, eggs, bread" );
        indexNotePort.index( UuidCreator.getTimeOrderedEpoch(), OTHER_OWNER, "Secret Plan", "Do not tell owner one" );

        var results = retrieveRelevantNotesPort.retrieve( OWNER, "What is on my grocery list?", 5 );

        assertThat( results ).hasSize( 1 );
        assertThat( results.get( 0 ).title() ).isEqualTo( "Grocery List" );
        assertThat( results ).noneMatch( note -> note.content().contains( "owner one" ) );

    }

    @Test
    void index_isIdempotentByNoteId() {

        var noteId = UuidCreator.getTimeOrderedEpoch();

        indexNotePort.index( noteId, OWNER, "Title", "Original content" );
        indexNotePort.index( noteId, OWNER, "Title", "Original content" );

        var count = jdbcTemplate.queryForObject( "SELECT COUNT(*) FROM vector_store WHERE id = ?", Integer.class, noteId );

        assertThat( count ).isEqualTo( 1 );

    }

    @Test
    void reindex_recoversOwnerFromExistingRowAndUpdatesContent() {

        var noteId = UuidCreator.getTimeOrderedEpoch();
        indexNotePort.index( noteId, OWNER, "Original Title", "Original content" );

        indexNotePort.reindex( noteId, "Updated Title", "Updated content" );

        var results = retrieveRelevantNotesPort.retrieve( OWNER, "Updated content", 5 );

        assertThat( results ).anyMatch( note -> note.title().equals( "Updated Title" ) );

    }

    @Test
    void remove_deletesTheIndexedNote() {

        var noteId = UuidCreator.getTimeOrderedEpoch();
        indexNotePort.index( noteId, OWNER, "Title", "Content" );

        removeNoteIndexPort.remove( noteId );

        var count = jdbcTemplate.queryForObject( "SELECT COUNT(*) FROM vector_store WHERE id = ?", Integer.class, noteId );

        assertThat( count ).isZero();

    }

    @Test
    void loadNotesMissingIndex_onlyReturnsNotesWithoutAVectorStoreRow() {

        var indexedId = UuidCreator.getTimeOrderedEpoch();
        var unindexedId = UuidCreator.getTimeOrderedEpoch();

        jdbcTemplate.update(
                "INSERT INTO notes (id, title, content, owner) VALUES (?, ?, ?, ?)",
                indexedId, "Indexed", "Content", OWNER );
        jdbcTemplate.update(
                "INSERT INTO notes (id, title, content, owner) VALUES (?, ?, ?, ?)",
                unindexedId, "Not Indexed", "Content", OWNER );
        indexNotePort.index( indexedId, OWNER, "Indexed", "Content" );

        var missing = loadNotesMissingIndexPort.loadNotesMissingIndex();

        assertThat( missing ).extracting( LoadNotesMissingIndexPort.NoteToIndex::id ).containsExactly( unindexedId );

    }

}
