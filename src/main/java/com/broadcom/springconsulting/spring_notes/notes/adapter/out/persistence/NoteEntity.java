package com.broadcom.springconsulting.spring_notes.notes.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table( "notes" )
record NoteEntity( @Id UUID id, String title, String content ) implements Persistable<UUID> {

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }

}