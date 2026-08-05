package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface UpdateNoteProjectionPort {

    void updateProjection( UUID id, String title, String content, String owner, Instant occurredAt );

}
