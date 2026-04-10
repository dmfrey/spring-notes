package com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface NotesRepository extends ListCrudRepository<NoteEntity, UUID> {

    @Query( "SELECT * FROM notes WHERE owner = :owner ORDER BY id LIMIT :limit" )
    List<NoteEntity> findFirst( @Param( "owner" ) String owner, @Param( "limit" ) int limit );

    @Query( "SELECT * FROM notes WHERE owner = :owner AND id > :cursor ORDER BY id LIMIT :limit" )
    List<NoteEntity> findAfterCursor( @Param( "owner" ) String owner, @Param( "cursor" ) UUID cursor, @Param( "limit" ) int limit );

}