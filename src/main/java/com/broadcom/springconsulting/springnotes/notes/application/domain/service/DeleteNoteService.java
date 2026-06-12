package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.port.in.DeleteNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.DeleteNotePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class DeleteNoteService implements DeleteNoteUseCase {

    private static final Logger log = LoggerFactory.getLogger( DeleteNoteService.class );

    private final DeleteNotePort deleteNotePort;

    DeleteNoteService( DeleteNotePort deleteNotePort ) {
        this.deleteNotePort = deleteNotePort;
    }

    @Override
    public void execute( DeleteNoteCommand command ) {
        log.debug( "Deleting note {}", command.id() );

        deleteNotePort.deleteNote( command.id() );
    }

}
