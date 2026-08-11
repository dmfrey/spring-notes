package com.broadcom.springconsulting.springnotes.chat.application.domain.service;

import com.broadcom.springconsulting.springnotes.chat.application.domain.model.RetrievedNote;
import com.broadcom.springconsulting.springnotes.chat.application.port.in.ChatUseCase.ChatCommand;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.GenerateChatResponsePort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.RetrieveRelevantNotesPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith( MockitoExtension.class )
class ChatServiceTest {

    static final String TEST_OWNER = "test-user-sub";

    @Mock
    RetrieveRelevantNotesPort retrieveRelevantNotesPort;

    @Mock
    GenerateChatResponsePort generateChatResponsePort;

    @Captor
    ArgumentCaptor<List<RetrievedNote>> contextCaptor;

    ChatService service;

    @BeforeEach
    void setUp() {
        service = new ChatService( retrieveRelevantNotesPort, generateChatResponsePort );
    }

    @Test
    void execute_retrievesContextScopedToOwnerAndGeneratesResponse() {

        var context = List.of( new RetrievedNote( "Title", "Content" ) );
        when( retrieveRelevantNotesPort.retrieve( eq( TEST_OWNER ), eq( "What did I write?" ), anyInt() ) ).thenReturn( context );
        when( generateChatResponsePort.generate( eq( TEST_OWNER ), eq( "What did I write?" ), contextCaptor.capture() ) )
                .thenReturn( Flux.just( "Hello", ", ", "world" ) );

        var result = service.execute( new ChatCommand( TEST_OWNER, "What did I write?" ) );

        StepVerifier.create( result )
                .expectNext( "Hello", ", ", "world" )
                .verifyComplete();

        verify( retrieveRelevantNotesPort ).retrieve( eq( TEST_OWNER ), eq( "What did I write?" ), anyInt() );
        assertThat( contextCaptor.getValue() ).isEqualTo( context );

    }

    @Test
    void chatCommand_withBlankMessage_throwsIllegalArgumentException() {

        assertThatThrownBy( () -> new ChatCommand( TEST_OWNER, "  " ) )
                .isInstanceOf( IllegalArgumentException.class )
                .hasMessageContaining( "message" );

    }

}
