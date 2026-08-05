package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.UpdateNoteProjectionPort;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith( MockitoExtension.class )
class NoteProjectionListenerTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    UpdateNoteProjectionPort updateNoteProjectionPort;

    NoteProjectionListener listener;

    @BeforeEach
    void setUp() {
        listener = new NoteProjectionListener( updateNoteProjectionPort );
    }

    @Test
    void onNoteEventPublished_withNoteUpdated_updatesProjection() {

        var id = UuidCreator.getTimeOrderedEpoch();
        var occurredAt = Instant.now();
        var event = new NoteUpdated( id, "New Title", "New content", occurredAt );

        listener.onNoteEventPublished( new NoteEventPublished( event, TEST_OWNER ) );

        verify( updateNoteProjectionPort ).updateProjection( id, "New Title", "New content", TEST_OWNER, occurredAt );

    }

}
