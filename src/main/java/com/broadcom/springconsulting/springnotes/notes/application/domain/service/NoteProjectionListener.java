package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.UpdateNoteProjectionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// Keeps the notes read projection in sync with events already durably appended by the write
// side. Only handles NoteUpdated for now - NoteCreated/NoteDeleted still write the projection
// directly from CreateNoteService/DeleteNoteService until they're cut over too.
@Component
class NoteProjectionListener {

    private static final Logger log = LoggerFactory.getLogger( NoteProjectionListener.class );

    private final UpdateNoteProjectionPort updateNoteProjectionPort;

    NoteProjectionListener( UpdateNoteProjectionPort updateNoteProjectionPort ) {
        this.updateNoteProjectionPort = updateNoteProjectionPort;
    }

    @TransactionalEventListener( phase = TransactionPhase.AFTER_COMMIT )
    void onNoteEventPublished( NoteEventPublished published ) {
        if ( published.event() instanceof NoteUpdated updated ) {
            log.debug( "Updating projection for note {}", updated.noteId() );

            updateNoteProjectionPort.updateProjection(
                    updated.noteId(), updated.title(), updated.content(), published.owner(), updated.occurredAt()
            );
        }
    }

}
