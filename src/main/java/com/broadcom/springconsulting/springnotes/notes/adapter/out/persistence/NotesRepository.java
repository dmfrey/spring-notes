package com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface NotesRepository extends ListCrudRepository<NoteEntity, UUID> {

    @Query( "SELECT * FROM notes WHERE owner = :owner ORDER BY id LIMIT :limit" )
    List<NoteEntity> findFirst( @Param( "owner" ) String owner, @Param( "limit" ) int limit );

    @Query( "SELECT * FROM notes WHERE owner = :owner AND id > :cursor ORDER BY id LIMIT :limit" )
    List<NoteEntity> findAfterCursor( @Param( "owner" ) String owner, @Param( "cursor" ) UUID cursor, @Param( "limit" ) int limit );

    // note_events.aggregate_id is the leading column of the unique constraint on
    // (aggregate_id, sequence_number), so this anti-join is index-backed.
    @Query( "SELECT n.* FROM notes n WHERE NOT EXISTS ( SELECT 1 FROM note_events e WHERE e.aggregate_id = n.id )" )
    List<NoteEntity> findMissingEvents();

    // save() always inserts (NoteEntity.isNew() is hardcoded true for client-generated ids), so
    // updates go through this explicit statement instead - which also means it must set the
    // auditing columns itself, since @EnableJdbcAuditing only hooks save().
    @Modifying
    @Query( "UPDATE notes SET title = :title, content = :content, last_modified_date = :lastModifiedDate, last_modified_by = :lastModifiedBy WHERE id = :id AND owner = :owner" )
    int updateProjection(
            @Param( "id" ) UUID id,
            @Param( "title" ) String title,
            @Param( "content" ) String content,
            @Param( "owner" ) String owner,
            @Param( "lastModifiedDate" ) Instant lastModifiedDate,
            @Param( "lastModifiedBy" ) String lastModifiedBy
    );

}