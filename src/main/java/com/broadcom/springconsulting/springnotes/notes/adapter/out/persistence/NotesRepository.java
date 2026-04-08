package com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface NotesRepository extends ListCrudRepository<NoteEntity, UUID> {

    @Query( "SELECT * FROM notes ORDER BY id LIMIT :limit" )
    List<NoteEntity> findFirst( @Param( "limit" ) int limit );

    @Query( "SELECT * FROM notes WHERE id > :cursor ORDER BY id LIMIT :limit" )
    List<NoteEntity> findAfterCursor( @Param( "cursor" ) UUID cursor, @Param( "limit" ) int limit );

}