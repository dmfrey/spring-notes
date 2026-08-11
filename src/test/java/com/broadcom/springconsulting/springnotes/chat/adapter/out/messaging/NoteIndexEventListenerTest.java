package com.broadcom.springconsulting.springnotes.chat.adapter.out.messaging;

import com.broadcom.springconsulting.springnotes.chat.application.port.out.IndexNotePort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.RemoveNoteIndexPort;
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
        var event = new NoteCreated( id, TEST_OWNER, "Title", "Content", Instant.now() );

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
