package com.broadcom.springconsulting.springnotes.notes.application.port.out;

import java.util.UUID;

public interface MarkNoteEventPublishedPort {

    void markPublished( UUID eventId );

}
