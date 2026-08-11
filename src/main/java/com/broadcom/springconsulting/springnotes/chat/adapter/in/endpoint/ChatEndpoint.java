package com.broadcom.springconsulting.springnotes.chat.adapter.in.endpoint;

import com.broadcom.springconsulting.springnotes.chat.application.port.in.ChatUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
class ChatEndpoint {

    private static final Logger log = LoggerFactory.getLogger( ChatEndpoint.class );

    private final ChatUseCase chatUseCase;

    ChatEndpoint( ChatUseCase chatUseCase ) {
        this.chatUseCase = chatUseCase;
    }

    @PostMapping( value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE, version = "1+" )
    Flux<String> chat( @RequestBody ChatRequest request, @AuthenticationPrincipal Jwt jwt ) {
        log.debug( "Handling chat message for owner {}", jwt.getSubject() );

        return chatUseCase.execute( new ChatUseCase.ChatCommand( jwt.getSubject(), request.message() ) );
    }

    @ExceptionHandler( IllegalArgumentException.class )
    ResponseEntity<Void> handleValidation() {
        return ResponseEntity.badRequest().build();
    }

    record ChatRequest( String message ) {}

}
