package com.broadcom.springconsulting.springnotes.chat.adapter.out.messaging;

import com.broadcom.springconsulting.springnotes.chat.application.port.out.IndexNotePort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.RemoveNoteIndexPort;
import com.broadcom.springconsulting.springnotes.chat.configuration.ChatConfiguration;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

// The app's first-ever RabbitMQ *consumer* (previously only a publisher existed). Deliberately
// bound to the existing notes.events exchange rather than any notes-package code knowing about
// chat - pure pub/sub, notes stays completely unaware this feature exists. Deserializes the
// raw body with the same hand-rolled ObjectMapper approach as NoteEventStoreAdapter, rather
// than a framework message converter, since NoteEvent's polymorphic payload needs the same
// @JsonTypeInfo-aware handling either way.
@Component
class NoteIndexEventListener {

    private static final Logger log = LoggerFactory.getLogger( NoteIndexEventListener.class );

    private final IndexNotePort indexNotePort;
    private final RemoveNoteIndexPort removeNoteIndexPort;
    private final ObjectMapper objectMapper;

    NoteIndexEventListener( IndexNotePort indexNotePort, RemoveNoteIndexPort removeNoteIndexPort, ObjectMapper objectMapper ) {
        this.indexNotePort = indexNotePort;
        this.removeNoteIndexPort = removeNoteIndexPort;
        this.objectMapper = objectMapper;
    }

    @RabbitListener( queues = ChatConfiguration.NOTE_INDEX_QUEUE )
    void onMessage( byte[] body ) {
        var event = objectMapper.readValue( body, NoteEvent.class );

        log.debug( "Indexing update from {} for note {}", event.getClass().getSimpleName(), event.noteId() );

        switch ( event ) {
            case NoteCreated created -> {
                var content = created.type() == NoteType.LIST ? renderItems( created.items() ) : created.content();
                indexNotePort.index( created.noteId(), created.owner(), created.title(), content );
            }
            case NoteUpdated updated -> indexNotePort.reindex( updated.noteId(), updated.title(), updated.content() );
            // ChecklistUpdated only carries a delta, not the note's full current items/title -
            // the port implementation re-reads current state from notes itself.
            case ChecklistUpdated checklistUpdated -> indexNotePort.reindexFromSource( checklistUpdated.noteId() );
            case NoteDeleted deleted -> removeNoteIndexPort.remove( deleted.noteId() );
        }
    }

    // Duplicated in VectorStoreIndexAdapter rather than shared - see that class's comment on
    // why a helper can't cross the messaging/persistence adapter package boundary here.
    private static String renderItems( List<ChecklistItem> items ) {
        return items.stream()
                .map( item -> ( item.checked() ? "- [x] " : "- [ ] " ) + item.text() )
                .collect( Collectors.joining( "\n" ) );
    }

}
