package com.broadcom.springconsulting.springnotes.chat.application.port.in;

import reactor.core.publisher.Flux;

public interface ChatUseCase {

    Flux<String> execute( ChatCommand command );

    record ChatCommand( String owner, String message ) {

        public ChatCommand {
            if ( message == null || message.isBlank() ) throw new IllegalArgumentException( "message must not be blank" );
        }

    }

}
