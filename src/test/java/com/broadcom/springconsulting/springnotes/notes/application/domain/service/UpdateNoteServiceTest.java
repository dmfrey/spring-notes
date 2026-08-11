package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteEvent;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteUpdated;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.UpdateNoteUseCase.UpdateNoteCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.LoadNoteEventsPort;
import com.github.f4b6a3.uuid.UuidCreator;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class UpdateNoteServiceTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    LoadNoteEventsPort loadNoteEventsPort;

    @Mock
    AppendNoteEventPort appendNoteEventPort;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Captor
    ArgumentCaptor<NoteUpdated> eventCaptor;

    @Captor
    ArgumentCaptor<NoteEventPublished> publishedCaptor;

    UpdateNoteService service;

    @BeforeEach
    void setUp() {
        service = new UpdateNoteService( loadNoteEventsPort, appendNoteEventPort, eventPublisher, ObservationRegistry.NOOP );
    }

    @Test
    void execute_appendsNoteUpdatedEventAndReturnsUpdatedNote() {

        var id = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) ).thenReturn( existingEventFor( id ) );

        var result = service.execute( new UpdateNoteCommand( id, TEST_OWNER, "New Title", "New content" ) );

        assertThat( result ).isEqualTo( new Note( id, "New Title", "New content" ) );

        verify( appendNoteEventPort ).append( eventCaptor.capture(), eq( TEST_OWNER ) );
        assertThat( eventCaptor.getValue().noteId() ).isEqualTo( id );
        assertThat( eventCaptor.getValue().title() ).isEqualTo( "New Title" );
        assertThat( eventCaptor.getValue().content() ).isEqualTo( "New content" );

    }

    @Test
    void execute_publishesNoteEventPublished() {

        var id = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) ).thenReturn( existingEventFor( id ) );

        service.execute( new UpdateNoteCommand( id, TEST_OWNER, "New Title", "New content" ) );

        verify( eventPublisher ).publishEvent( publishedCaptor.capture() );
        assertThat( publishedCaptor.getValue().owner() ).isEqualTo( TEST_OWNER );
        assertThat( publishedCaptor.getValue().event() ).isInstanceOf( NoteUpdated.class );

    }

    @Test
    void execute_whenNoteNotOwnedOrMissing_throwsNoteNotFoundExceptionAndDoesNotAppendEvent() {

        var id = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteEventsPort.loadEvents( id, TEST_OWNER ) ).thenReturn( List.of() );

        assertThatThrownBy( () -> service.execute( new UpdateNoteCommand( id, TEST_OWNER, "New Title", "New content" ) ) )
                .isInstanceOf( NoteNotFoundException.class );

        verify( appendNoteEventPort, never() ).append( any(), eq( TEST_OWNER ) );

    }

    private static List<NoteEvent> existingEventFor( UUID id ) {
        return List.of( new NoteCreated( id, TEST_OWNER, "Existing Title", "Existing content", Instant.now() ) );
    }

    @Test
    void updateNoteCommand_withBlankTitle_throwsIllegalArgumentException() {

        var id = UuidCreator.getTimeOrderedEpoch();

        assertThatThrownBy( () -> new UpdateNoteCommand( id, TEST_OWNER, "  ", "Some content" ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "title" );

    }

    @Test
    void updateNoteCommand_withBlankContent_throwsIllegalArgumentException() {

        var id = UuidCreator.getTimeOrderedEpoch();

        assertThatThrownBy( () -> new UpdateNoteCommand( id, TEST_OWNER, "My Title", "  " ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "content" );

    }

}
