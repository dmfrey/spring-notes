package com.broadcom.springconsulting.springnotes.chat.adapter.out.ai;

import com.broadcom.springconsulting.springnotes.chat.application.domain.model.RetrievedNote;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.RetrieveRelevantNotesPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

// Filters server-side via FilterExpressionBuilder (never string concatenation) so a note never
// reaches the LLM prompt for any owner but its own - mirrors LoadNotesPort's
// "WHERE owner = :owner" as the one safe ownership-scoping pattern already established in this
// codebase, rather than trusting Spring AI's QuestionAnswerAdvisor/RetrievalAugmentationAdvisor
// to get it right implicitly.
@Component
class VectorStoreRetrievalAdapter implements RetrieveRelevantNotesPort {

    private static final Logger log = LoggerFactory.getLogger( VectorStoreRetrievalAdapter.class );

    private final VectorStore vectorStore;

    VectorStoreRetrievalAdapter( VectorStore vectorStore ) {
        this.vectorStore = vectorStore;
    }

    @Override
    public List<RetrievedNote> retrieve( String owner, String query, int topK ) {
        log.debug( "Retrieving up to {} relevant notes for owner {}", topK, owner );

        var ownerFilter = new FilterExpressionBuilder().eq( "owner", owner ).build();

        var request = SearchRequest.builder()
                .query( query )
                .topK( topK )
                .filterExpression( ownerFilter )
                .build();

        return vectorStore.similaritySearch( request ).stream()
                .map( document -> new RetrievedNote(
                        String.valueOf( document.getMetadata().get( "title" ) ),
                        document.getText() ) )
                .toList();
    }

}
