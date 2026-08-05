package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNotesMissingEventsPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNotesMissingEventsPort.NoteSnapshot;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class NoteEventBackfillRunnerTest {

    static final String OWNER = "test-user-sub";

    @Mock
    LoadNotesMissingEventsPort loadNotesMissingEventsPort;

    @Mock
    AppendNoteEventPort appendNoteEventPort;

    @Captor
    ArgumentCaptor<NoteCreated> eventCaptor;

    NoteEventBackfillRunner runner;

    @BeforeEach
    void setUp() {
        runner = new NoteEventBackfillRunner( loadNotesMissingEventsPort, appendNoteEventPort );
    }

    @Test
    void run_withNoMissingEvents_appendsNothing() throws Exception {

        when( loadNotesMissingEventsPort.loadNotesMissingEvents() ).thenReturn( List.of() );

        runner.run( null );

        verifyNoInteractions( appendNoteEventPort );

    }

    @Test
    void run_withMissingEvents_appendsNoteCreatedUsingCreatedDate() throws Exception {

        var id = UuidCreator.getTimeOrderedEpoch();
        var createdDate = Instant.parse( "2025-01-01T00:00:00Z" );
        when( loadNotesMissingEventsPort.loadNotesMissingEvents() )
                .thenReturn( List.of( new NoteSnapshot( id, OWNER, "Title", "Content", createdDate ) ) );

        runner.run( null );

        verify( appendNoteEventPort ).append( eventCaptor.capture(), eq( OWNER ) );
        assertThat( eventCaptor.getValue() ).isEqualTo( new NoteCreated( id, OWNER, "Title", "Content", createdDate ) );

    }

    @Test
    void run_withNullCreatedDate_fallsBackToNow() throws Exception {

        var id = UuidCreator.getTimeOrderedEpoch();
        when( loadNotesMissingEventsPort.loadNotesMissingEvents() )
                .thenReturn( List.of( new NoteSnapshot( id, OWNER, "Title", "Content", null ) ) );

        var before = Instant.now();
        runner.run( null );
        var after = Instant.now();

        verify( appendNoteEventPort ).append( eventCaptor.capture(), eq( OWNER ) );
        assertThat( eventCaptor.getValue().occurredAt() ).isBetween( before, after );

    }

    @Test
    void run_withMultipleMissingEvents_appendsOnePerNote() throws Exception {

        var id1 = UuidCreator.getTimeOrderedEpoch();
        var id2 = UuidCreator.getTimeOrderedEpoch();
        when( loadNotesMissingEventsPort.loadNotesMissingEvents() ).thenReturn( List.of(
                new NoteSnapshot( id1, OWNER, "Title 1", "Content 1", Instant.now() ),
                new NoteSnapshot( id2, OWNER, "Title 2", "Content 2", Instant.now() )
        ) );

        runner.run( null );

        verify( appendNoteEventPort, times( 2 ) ).append( eventCaptor.capture(), eq( OWNER ) );
        assertThat( eventCaptor.getAllValues() )
                .extracting( NoteCreated::noteId )
                .containsExactly( id1, id2 );

    }

}
