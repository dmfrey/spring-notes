package com.broadcom.springconsulting.springnotes.chat.application.domain.service;

import com.broadcom.springconsulting.springnotes.chat.application.port.out.IndexNotePort;
import com.broadcom.springconsulting.springnotes.chat.application.port.out.LoadNotesMissingIndexPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// One-time backfill for notes that existed before the chat feature did, mirroring
// NoteEventBackfillRunner's pattern: safe to run on every startup indefinitely, since
// loadNotesMissingIndex() only ever returns notes that still lack a vector_store row.
@Component
class NoteIndexBackfillRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger( NoteIndexBackfillRunner.class );

    private final LoadNotesMissingIndexPort loadNotesMissingIndexPort;
    private final IndexNotePort indexNotePort;

    NoteIndexBackfillRunner( LoadNotesMissingIndexPort loadNotesMissingIndexPort, IndexNotePort indexNotePort ) {
        this.loadNotesMissingIndexPort = loadNotesMissingIndexPort;
        this.indexNotePort = indexNotePort;
    }

    @Override
    public void run( ApplicationArguments args ) {
        var notes = loadNotesMissingIndexPort.loadNotesMissingIndex();

        if ( notes.isEmpty() ) {
            return;
        }

        log.info( "Indexing {} pre-existing note(s) for chat retrieval", notes.size() );

        for ( var note : notes ) {
            indexNotePort.index( note.id(), note.owner(), note.title(), note.content() );
        }
    }

}
