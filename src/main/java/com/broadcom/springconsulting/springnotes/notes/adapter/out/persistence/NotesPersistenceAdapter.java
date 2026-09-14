package com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.DeleteNotePort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNotesMissingEventsPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNotesPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.SaveNotePort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.UpdateNoteItemsProjectionPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.UpdateNoteProjectionPort;
import com.github.f4b6a3.uuid.UuidCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// items (notes.items, JSONB) is handled via hand-rolled NamedParameterJdbcTemplate rather than
// through NotesRepository/NoteEntity, matching NoteEventStoreAdapter's approach for
// note_events.payload (the only other JSONB column in this app) - Spring Data JDBC treats any
// bare List<T> entity property as a one-to-many child-table relationship regardless of
// registered converters, and the supported way around that (a custom AbstractJdbcConfiguration
// subclass) would mean taking over Spring Boot's entire JDBC autoconfiguration bean graph just
// for one column.
@Repository
class NotesPersistenceAdapter implements LoadNotesPort, SaveNotePort, DeleteNotePort, UpdateNoteProjectionPort, UpdateNoteItemsProjectionPort, LoadNotesMissingEventsPort {

    private static final Logger log = LoggerFactory.getLogger( NotesPersistenceAdapter.class );

    private final NotesRepository notesRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    NotesPersistenceAdapter( NotesRepository notesRepository, NamedParameterJdbcTemplate jdbcTemplate, ObjectMapper objectMapper ) {
        this.notesRepository = notesRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public NoteSlice loadNotes( String owner, UUID cursor, int limit ) {
        log.debug( "Loading notes for owner {} with cursor {} and limit {}", owner, cursor, limit );

        List<NoteEntity> entities = cursor == null
                ? notesRepository.findFirst( owner, limit )
                : notesRepository.findAfterCursor( owner, cursor, limit );

        var itemsByNoteId = loadItems( entities.stream().map( NoteEntity::id ).toList() );

        List<Note> notes = entities.stream()
                .map( e -> new Note( e.id(), e.title(), e.content(), e.type(), itemsByNoteId.getOrDefault( e.id(), List.of() ) ) )
                .toList();

        UUID nextCursor = notes.size() == limit ? notes.getLast().id() : null;

        return new NoteSlice( notes, nextCursor );
    }

    @Override
    public Note saveNote( String owner, String title, String content, NoteType type, List<ChecklistItem> items ) {
        log.debug( "Saving note for owner {}", owner );

        var entity = new NoteEntity( UuidCreator.getTimeOrderedEpoch(), title, content, type, owner, null, null, null, null );
        var saved = notesRepository.save( entity );

        if ( !items.isEmpty() ) {
            writeItems( saved.id(), items );
        }

        return new Note( saved.id(), saved.title(), saved.content(), saved.type(), items );
    }

    @Override
    public boolean deleteNote( UUID id, String owner ) {
        log.debug( "Deleting note {} for owner {}", id, owner );

        return notesRepository.deleteByIdAndOwner( id, owner ) > 0;
    }

    @Override
    public void updateProjection( UUID id, String title, String content, String owner, Instant occurredAt ) {
        log.debug( "Updating projection for note {}", id );

        notesRepository.updateProjection( id, title, content, owner, occurredAt, owner );
    }

    @Override
    public void updateItems( UUID id, List<ChecklistItem> items, String owner, Instant occurredAt ) {
        log.debug( "Updating items projection for note {}", id );

        var params = new MapSqlParameterSource()
                .addValue( "id", id )
                .addValue( "owner", owner )
                .addValue( "items", objectMapper.writeValueAsString( items ) )
                .addValue( "lastModifiedDate", occurredAt.atOffset( ZoneOffset.UTC ) )
                .addValue( "lastModifiedBy", owner );

        jdbcTemplate.update( """
                UPDATE notes SET items = :items::jsonb, last_modified_date = :lastModifiedDate, last_modified_by = :lastModifiedBy
                WHERE id = :id AND owner = :owner
                """, params );
    }

    @Override
    public List<NoteSnapshot> loadNotesMissingEvents() {
        log.debug( "Loading notes missing an event history" );

        var entities = notesRepository.findMissingEvents();
        var itemsByNoteId = loadItems( entities.stream().map( NoteEntity::id ).toList() );

        return entities.stream()
                .map( e -> new NoteSnapshot( e.id(), e.owner(), e.title(), e.content(), e.type(), itemsByNoteId.getOrDefault( e.id(), List.of() ), e.createdDate() ) )
                .toList();
    }

    private void writeItems( UUID id, List<ChecklistItem> items ) {
        var params = new MapSqlParameterSource()
                .addValue( "id", id )
                .addValue( "items", objectMapper.writeValueAsString( items ) );

        jdbcTemplate.update( "UPDATE notes SET items = :items::jsonb WHERE id = :id", params );
    }

    private Map<UUID, List<ChecklistItem>> loadItems( List<UUID> ids ) {
        if ( ids.isEmpty() ) {
            return Map.of();
        }

        var params = new MapSqlParameterSource().addValue( "ids", ids );

        return jdbcTemplate.query(
                "SELECT id, items FROM notes WHERE id IN (:ids) AND items IS NOT NULL",
                params,
                rs -> {
                    Map<UUID, List<ChecklistItem>> result = new HashMap<>();

                    while ( rs.next() ) {
                        List<ChecklistItem> items = objectMapper.readValue( rs.getString( "items" ), new TypeReference<List<ChecklistItem>>() {} );
                        result.put( rs.getObject( "id", UUID.class ), items );
                    }

                    return result;
                }
        );
    }

}
