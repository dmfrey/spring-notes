package com.broadcom.springconsulting.springnotes.notes.application.domain.service;

import com.broadcom.springconsulting.springnotes.notes.application.port.in.DeleteNoteUseCase.DeleteNoteCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.out.DeleteNotePort;
import com.github.f4b6a3.uuid.UuidCreator;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith( MockitoExtension.class )
class DeleteNoteServiceTest {

    @Mock
    DeleteNotePort deleteNotePort;

    DeleteNoteService service;

    @BeforeEach
    void setUp() {
        service = new DeleteNoteService( deleteNotePort, ObservationRegistry.NOOP );
    }

    @Test
    void execute_delegatesToPort() {

        var id = UuidCreator.getTimeOrderedEpoch();

        service.execute( new DeleteNoteCommand( id ) );

        verify( deleteNotePort ).deleteNote( id );

    }

}
