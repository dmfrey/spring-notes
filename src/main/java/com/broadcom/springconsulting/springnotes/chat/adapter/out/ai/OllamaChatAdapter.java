package com.broadcom.springconsulting.springnotes.chat.adapter.out.ai;

import com.broadcom.springconsulting.springnotes.chat.application.domain.model.RetrievedNote;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.GenerateChatResponsePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@Component
class OllamaChatAdapter implements GenerateChatResponsePort {

    private static final Logger log = LoggerFactory.getLogger( OllamaChatAdapter.class );

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are a helpful assistant that answers questions about the user's own notes.
            Answer only using the note excerpts provided below - if they don't contain the
            answer, say you don't know rather than guessing. Never mention or imply notes
            belonging to anyone other than the current user; you have only ever been given
            their notes.

            Notes:
            %s
            """;

    private final ChatClient chatClient;

    OllamaChatAdapter( ChatClient.Builder chatClientBuilder, ChatMemory chatMemory ) {
        // MessageChatMemoryAdvisor.Builder's default scheduler resolution doesn't survive
        // native-image AOT bean instantiation - it throws "scheduler cannot be null" at
        // startup (works fine on the JVM). Supplying one explicitly sidesteps that entirely.
        var memoryAdvisor = MessageChatMemoryAdvisor.builder( chatMemory )
                .scheduler( Schedulers.boundedElastic() )
                .build();

        this.chatClient = chatClientBuilder
                .defaultAdvisors( memoryAdvisor )
                .build();
    }

    @Override
    public Flux<String> generate( String owner, String message, List<RetrievedNote> context ) {
        log.debug( "Generating chat response for owner {}", owner );

        var notesBlock = context.isEmpty()
                ? "(none found)"
                : context.stream()
                        .map( note -> "- %s: %s".formatted( note.title(), note.content() ) )
                        .collect( Collectors.joining( "\n" ) );

        return chatClient.prompt()
                .system( SYSTEM_PROMPT_TEMPLATE.formatted( notesBlock ) )
                .user( message )
                .advisors( a -> a.param( ChatMemory.CONVERSATION_ID, owner ) )
                .stream()
                .content();
    }

}
