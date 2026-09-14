package com.broadcom.springconsulting.springnotes.notes.adapter.out.persistence;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

// items (JSONB) is deliberately NOT a mapped property here - Spring Data JDBC treats any bare
// List<T> entity property as a one-to-many relationship to a child table regardless of
// registered converters (collection-shaped properties get relational treatment before custom-
// conversion lookup runs), and working around that means either taking over the whole
// AbstractJdbcConfiguration bean graph (@ConditionalOnMissingBean gates Spring Boot's own JDBC
// autoconfiguration on that) or - simpler, and consistent with note_events.payload, the only
// other JSONB column in this app - handling it via hand-rolled NamedParameterJdbcTemplate in
// NotesPersistenceAdapter instead, same spirit as NoteEventStoreAdapter.
@Table( "notes" )
record NoteEntity(
        @Id UUID id,
        String title,
        String content,
        NoteType type,
        String owner,
        @CreatedDate Instant createdDate,
        @LastModifiedDate Instant lastModifiedDate,
        @CreatedBy String createdBy,
        @LastModifiedBy String lastModifiedBy
) implements Persistable<UUID> {

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }

}