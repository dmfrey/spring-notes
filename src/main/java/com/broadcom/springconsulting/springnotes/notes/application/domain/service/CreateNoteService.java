package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.CreateNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.SaveNotePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class CreateNoteService implements CreateNoteUseCase {

    private static final Logger log = LoggerFactory.getLogger( CreateNoteService.class );

    private final SaveNotePort saveNotePort;

    CreateNoteService( SaveNotePort saveNotePort ) {
        this.saveNotePort = saveNotePort;
    }

    @Override
    public Note execute( CreateNoteCommand command ) {
        log.debug( "Creating note for owner {}", command.owner() );

        return saveNotePort.saveNote( command.owner(), command.title(), command.content() );
    }

}