package com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNoteEventsPort;
import com.github.f4b6a3.uuid.UuidCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

// Hand-rolled with NamedParameterJdbcTemplate rather than a Spring Data JDBC repository:
// polymorphic NoteEvent payloads stored as JSONB are simpler to map explicitly with Jackson
// than to fit into Spring Data JDBC's converter model.
@Repository
class NoteEventStoreAdapter implements AppendNoteEventPort, LoadNoteEventsPort {

    private static final Logger log = LoggerFactory.getLogger( NoteEventStoreAdapter.class );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    NoteEventStoreAdapter( NamedParameterJdbcTemplate jdbcTemplate, ObjectMapper objectMapper ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void append( NoteEvent event, String owner ) {
        log.debug( "Appending {} for note {}", event.getClass().getSimpleName(), event.noteId() );

        var params = new MapSqlParameterSource()
                .addValue( "id", UuidCreator.getTimeOrderedEpoch() )
                .addValue( "aggregateId", event.noteId() )
                .addValue( "owner", owner )
                .addValue( "type", event.getClass().getSimpleName() )
                .addValue( "payload", objectMapper.writeValueAsString( event ) )
                .addValue( "occurredAt", event.occurredAt() );

        jdbcTemplate.update( """
                INSERT INTO note_events (id, aggregate_id, owner, type, payload, sequence_number, occurred_at)
                VALUES (:id, :aggregateId, :owner, :type, :payload::jsonb,
                        (SELECT COALESCE(MAX(sequence_number), 0) + 1 FROM note_events WHERE aggregate_id = :aggregateId),
                        :occurredAt)
                """, params );
    }

    @Override
    public List<NoteEvent> loadEvents( UUID noteId ) {
        log.debug( "Loading events for note {}", noteId );

        var params = new MapSqlParameterSource().addValue( "aggregateId", noteId );

        return jdbcTemplate.query(
                "SELECT payload FROM note_events WHERE aggregate_id = :aggregateId ORDER BY sequence_number",
                params,
                ( rs, rowNum ) -> objectMapper.readValue( rs.getString( "payload" ), NoteEvent.class )
        );
    }

}
