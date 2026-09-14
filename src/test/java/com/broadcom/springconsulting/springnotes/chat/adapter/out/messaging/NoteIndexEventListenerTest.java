package com.broadcom.springconsulting.springnotes.chat.adapter.out.messaging;

import com.broadcom.springconsulting.springnotes.chat.application.port.out.IndexNotePort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.RemoveNoteIndexPort;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistAction;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.ChecklistUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith( MockitoExtension.class )
class NoteIndexEventListenerTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    IndexNotePort indexNotePort;

    @Mock
    RemoveNoteIndexPort removeNoteIndexPort;

    NoteIndexEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NoteIndexEventListener( indexNotePort, removeNoteIndexPort, JsonMapper.builder().build() );
    }

    @Test
    void onMessage_forNoteCreated_indexesWithOwnerFromEvent() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteCreated( id, TEST_OWNER, "Title", "Content", NoteType.TEXT, List.of(), Instant.now() );

        listener.onMessage( toBytes( event ) );

        verify( indexNotePort ).index( id, TEST_OWNER, "Title", "Content" );
        verifyNoInteractions( removeNoteIndexPort );

    }

    @Test
    void onMessage_forNoteUpdated_reindexesWithoutOwner() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteUpdated( id, "New Title", "New content", Instant.now() );

        listener.onMessage( toBytes( event ) );

        verify( indexNotePort ).reindex( id, "New Title", "New content" );
        verify( indexNotePort, never() ).index( any(), any(), any(), any() );
        verifyNoInteractions( removeNoteIndexPort );

    }

    @Test
    void onMessage_forListNoteCreated_indexesRenderedItemsAsContent() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var items = List.of(
                new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", false ),
                new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Eggs", true )
        );
        var event = new NoteCreated( id, TEST_OWNER, "Groceries", null, NoteType.LIST, items, Instant.now() );

        listener.onMessage( toBytes( event ) );

        verify( indexNotePort ).index( id, TEST_OWNER, "Groceries", "- [ ] Milk\n- [x] Eggs" );

    }

    @Test
    void onMessage_forChecklistUpdated_reindexesFromSource() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var event = new ChecklistUpdated( id, ChecklistAction.ITEM_TOGGLED, UuidCreator.getTimeOrderedEpoch(), null, true, null, Instant.now() );

        listener.onMessage( toBytes( event ) );

        verify( indexNotePort ).reindexFromSource( id );
        verify( indexNotePort, never() ).index( any(), any(), any(), any() );
        verify( indexNotePort, never() ).reindex( any(), any(), any() );
        verifyNoInteractions( removeNoteIndexPort );

    }

    @Test
    void onMessage_forNoteDeleted_removesIndex() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteDeleted( id, Instant.now() );

        listener.onMessage( toBytes( event ) );

        verify( removeNoteIndexPort ).remove( id );
        verifyNoInteractions( indexNotePort );

    }

    private static byte[] toBytes( Object event ) {
        return JsonMapper.builder().build().writeValueAsString( event ).getBytes( StandardCharsets.UTF_8 );
    }

}
