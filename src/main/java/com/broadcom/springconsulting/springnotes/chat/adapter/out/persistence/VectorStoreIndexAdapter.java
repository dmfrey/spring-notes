package com.broadcom.springconsulting.springnotes.chat.adapter.out.persistence;

import com.broadcom.springconsulting.springnotes.chat.application.port.out.IndexNotePort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.LoadNotesMissingIndexPort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.RemoveNoteIndexPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Document ids are always the note's own UUID (as a string), never Spring AI's random default -
// PgVectorStore's INSERT is "ON CONFLICT (id) DO UPDATE", so add() is naturally an idempotent
// upsert. That matters here specifically because NoteIndexEventListener's outbox delivery is
// at-least-once: without a deterministic id, redelivery would silently duplicate a note's
// content in retrieval results.
@Repository
class VectorStoreIndexAdapter implements IndexNotePort, RemoveNoteIndexPort, LoadNotesMissingIndexPort {

    private static final Logger log = LoggerFactory.getLogger( VectorStoreIndexAdapter.class );

    private final VectorStore vectorStore;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    VectorStoreIndexAdapter( VectorStore vectorStore, NamedParameterJdbcTemplate jdbcTemplate ) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void index( UUID noteId, String owner, String title, String content ) {
        log.debug( "Indexing note {} for owner {}", noteId, owner );

        vectorStore.add( List.of( toDocument( noteId, owner, title, content ) ) );
    }

    @Override
    public void reindex( UUID noteId, String title, String content ) {
        log.debug( "Reindexing note {}", noteId );

        var params = new MapSqlParameterSource().addValue( "id", noteId );
        var owners = jdbcTemplate.queryForList(
                "SELECT metadata->>'owner' AS owner FROM vector_store WHERE id = :id",
                params, String.class );

        if ( owners.isEmpty() ) {
            log.warn( "Skipping reindex for note {} - no existing vector_store row to recover its owner from", noteId );
            return;
        }

        vectorStore.add( List.of( toDocument( noteId, owners.get( 0 ), title, content ) ) );
    }

    @Override
    public void remove( UUID noteId ) {
        log.debug( "Removing index for note {}", noteId );

        vectorStore.delete( List.of( noteId.toString() ) );
    }

    @Override
    public List<NoteToIndex> loadNotesMissingIndex() {
        log.debug( "Loading notes missing a chat index entry" );

        // vector_store.id is the primary key, so this anti-join is index-backed on both sides -
        // same spirit as NotesRepository.findMissingEvents()'s anti-join.
        return jdbcTemplate.query(
                """
                SELECT n.id, n.owner, n.title, n.content
                FROM notes n
                WHERE NOT EXISTS ( SELECT 1 FROM vector_store v WHERE v.id = n.id )
                """,
                ( rs, rowNum ) -> new NoteToIndex(
                        rs.getObject( "id", UUID.class ),
                        rs.getString( "owner" ),
                        rs.getString( "title" ),
                        rs.getString( "content" ) )
        );
    }

    private static Document toDocument( UUID noteId, String owner, String title, String content ) {
        return new Document( noteId.toString(), content, Map.of( "owner", owner, "title", title ) );
    }

}
