package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteNotFoundException;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteDeleted;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.DeleteNoteUseCase.DeleteNoteCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.AppendNoteEventPort;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.DeleteNotePort;
import com.github.f4b6a3.uuid.UuidCreator;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class DeleteNoteServiceTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    DeleteNotePort deleteNotePort;

    @Mock
    AppendNoteEventPort appendNoteEventPort;

    @Captor
    ArgumentCaptor<NoteDeleted> eventCaptor;

    DeleteNoteService service;

    @BeforeEach
    void setUp() {
        service = new DeleteNoteService( deleteNotePort, appendNoteEventPort, ObservationRegistry.NOOP );
    }

    @Test
    void execute_delegatesToPort() {

        var id = UuidCreator.getTimeOrderedEpoch();
        when( deleteNotePort.deleteNote( id, TEST_OWNER ) ).thenReturn( true );

        service.execute( new DeleteNoteCommand( id, TEST_OWNER ) );

        verify( deleteNotePort ).deleteNote( id, TEST_OWNER );

    }

    @Test
    void execute_appendsNoteDeletedEvent() {

        var id = UuidCreator.getTimeOrderedEpoch();
        when( deleteNotePort.deleteNote( id, TEST_OWNER ) ).thenReturn( true );

        service.execute( new DeleteNoteCommand( id, TEST_OWNER ) );

        verify( appendNoteEventPort ).append( eventCaptor.capture(), eq( TEST_OWNER ) );
        assertThat( eventCaptor.getValue().noteId() ).isEqualTo( id );

    }

    @Test
    void execute_whenNoteNotOwnedOrMissing_throwsNoteNotFoundExceptionAndDoesNotAppendEvent() {

        var id = UuidCreator.getTimeOrderedEpoch();
        when( deleteNotePort.deleteNote( id, TEST_OWNER ) ).thenReturn( false );

        assertThatThrownBy( () -> service.execute( new DeleteNoteCommand( id, TEST_OWNER ) ) )
                .isInstanceOf( NoteNotFoundException.class );

        verify( appendNoteEventPort, never() ).append( any(), eq( TEST_OWNER ) );

    }

}
