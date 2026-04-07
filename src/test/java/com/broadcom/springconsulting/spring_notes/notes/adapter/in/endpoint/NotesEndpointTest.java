package com.broadcom.springconsulting.spring_notes.notes.adapter.in.endpoint;

import com.broadcom.springconsulting.spring_notes.configuration.WebConfiguration;
import com.broadcom.springconsulting.spring_notes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.spring_notes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.spring_notes.notes.application.port.in.LoadNotesUseCase;
import com.broadcom.springconsulting.spring_notes.notes.application.port.in.LoadNotesUseCase.LoadNotesCommand;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest( NotesEndpoint.class )
@Import( WebConfiguration.class )
class NotesEndpointTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LoadNotesUseCase loadNotesUseCase;

    @Test
    void loadNotes_firstPage_returnsSlice() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        var notes = List.of( new Note( noteId, "Test Note", "Test content" ) );
        when( loadNotesUseCase.execute( any() ) ).thenReturn( new NoteSlice( notes, null ) );

        mockMvc.perform( get( "/notes" ).header( "API-Version", "1" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.notes.length()" ).value( 1 ) )
                .andExpect( jsonPath( "$.notes[0].title" ).value( "Test Note" ) )
                .andExpect( jsonPath( "$.nextCursor" ).doesNotExist() );

    }

    @Test
    void loadNotes_withNextCursor_includesCursorInResponse() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        UUID nextCursor = UuidCreator.getTimeOrderedEpoch();
        var notes = List.of( new Note( noteId, "Test Note", "Test content" ) );
        when( loadNotesUseCase.execute( any() ) ).thenReturn( new NoteSlice( notes, nextCursor ) );

        mockMvc.perform( get( "/notes" ).header( "API-Version", "1" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.nextCursor" ).value( nextCursor.toString() ) );

    }

    @Test
    void loadNotes_withCursorAndLimit_passesCommandCorrectly() throws Exception {

        UUID cursor = UuidCreator.getTimeOrderedEpoch();
        when( loadNotesUseCase.execute( any() ) ).thenReturn( new NoteSlice( List.of(), null ) );

        mockMvc.perform( get( "/notes" )
                        .header( "API-Version", "1" )
                        .param( "cursor", cursor.toString() )
                        .param( "limit", "10" ) )
                .andExpect( status().isOk() );

        verify( loadNotesUseCase ).execute( new LoadNotesCommand( cursor, 10 ) );

    }

    @Test
    void loadNotes_withoutApiVersionHeader_returnsBadRequest() throws Exception {

        mockMvc.perform( get( "/notes" ) )
                .andExpect( status().isBadRequest() );

    }

    @Test
    void loadNotes_emptyResult_returnsEmptySlice() throws Exception {

        when( loadNotesUseCase.execute( any() ) ).thenReturn( new NoteSlice( List.of(), null ) );

        mockMvc.perform( get( "/notes" ).header( "API-Version", "1" ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.notes" ).isArray() )
                .andExpect( jsonPath( "$.notes.length()" ).value( 0 ) )
                .andExpect( jsonPath( "$.nextCursor" ).doesNotExist() );

    }

}