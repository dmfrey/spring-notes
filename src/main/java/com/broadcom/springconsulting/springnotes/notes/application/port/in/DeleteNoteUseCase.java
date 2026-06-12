package com.broadcom.springconsulting.springnotes.notes.application.port.in;

import java.util.UUID;

public interface DeleteNoteUseCase {

    void execute( DeleteNoteCommand command );

    record DeleteNoteCommand( UUID id ) {}

}
