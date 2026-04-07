package com.broadcom.springconsulting.spring_notes.notes.application.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude( JsonInclude.Include.NON_NULL )
public record NoteSlice( List<Note> notes, UUID nextCursor ) {
}