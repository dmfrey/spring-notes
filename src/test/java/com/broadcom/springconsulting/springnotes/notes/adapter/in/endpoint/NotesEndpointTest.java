package com.broadcom.springconsulting.springnotes.notes.adapter.in.endpoint;

import com.broadcom.springconsulting.springnotes.configuration.SecurityConfiguration;
import com.broadcom.springconsulting.springnotes.configuration.WebConfiguration;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.CreateNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase.LoadNotesCommand;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest( NotesEndpoint.class )
@Import( { WebConfiguration.class, SecurityConfiguration.class } )
class NotesEndpointTest {

    static final String TEST_SUBJECT = "test-user-sub";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    LoadNotesUseCase loadNotesUseCase;

    @MockitoBean
    CreateNoteUseCase createNoteUseCase;

    @Test
    void loadNotes_firstPage_returnsSlice() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        var notes = List.of( new Note( noteId, "Test Note", "Test content" ) );
        when( loadNotesUseCase.execute( any() ) ).thenReturn( new NoteSlice( notes, null ) );

        mockMvc.perform( get( "/notes" )
                        .header( "API-Version", "1" )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
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

        mockMvc.perform( get( "/notes" )
                        .header( "API-Version", "1" )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
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
                        .param( "limit", "10" )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isOk() );

        verify( loadNotesUseCase ).execute( new LoadNotesCommand( TEST_SUBJECT, cursor, 10 ) );

    }

    @Test
    void loadNotes_withoutJwt_returnsUnauthorized() throws Exception {

        mockMvc.perform( get( "/notes" ).header( "API-Version", "1" ) )
                .andExpect( status().isUnauthorized() );

    }

    @Test
    void loadNotes_withoutApiVersionHeader_returnsBadRequest() throws Exception {

        mockMvc.perform( get( "/notes" )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isBadRequest() );

    }

    @Test
    void loadNotes_emptyResult_returnsEmptySlice() throws Exception {

        when( loadNotesUseCase.execute( any() ) ).thenReturn( new NoteSlice( List.of(), null ) );

        mockMvc.perform( get( "/notes" )
                        .header( "API-Version", "1" )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.notes" ).isArray() )
                .andExpect( jsonPath( "$.notes.length()" ).value( 0 ) )
                .andExpect( jsonPath( "$.nextCursor" ).doesNotExist() );

    }

    @Test
    void createNote_returnsCreatedWithLocationAndBody() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        var note = new Note( noteId, "My Title", "Some content" );
        when( createNoteUseCase.execute( any() ) ).thenReturn( note );

        mockMvc.perform( post( "/notes" )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"My Title","content":"Some content"}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isCreated() )
                .andExpect( header().string( "Location", containsString( noteId.toString() ) ) )
                .andExpect( jsonPath( "$.id" ).value( noteId.toString() ) )
                .andExpect( jsonPath( "$.title" ).value( "My Title" ) )
                .andExpect( jsonPath( "$.content" ).value( "Some content" ) );

        verify( createNoteUseCase ).execute( new CreateNoteUseCase.CreateNoteCommand( TEST_SUBJECT, "My Title", "Some content" ) );

    }

    @Test
    void createNote_withBlankContent_returnsBadRequest() throws Exception {

        mockMvc.perform( post( "/notes" )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"My Title","content":""}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isBadRequest() );

    }

    @Test
    void createNote_withBlankTitle_returnsBadRequest() throws Exception {

        mockMvc.perform( post( "/notes" )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"","content":"Some content"}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isBadRequest() );

    }

    @Test
    void createNote_withoutJwt_returnsUnauthorized() throws Exception {

        mockMvc.perform( post( "/notes" )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"My Title","content":"Some content"}
                                """ ) )
                .andExpect( status().isUnauthorized() );

    }

    @Test
    void createNote_withoutApiVersionHeader_returnsBadRequest() throws Exception {

        mockMvc.perform( post( "/notes" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"My Title","content":"Some content"}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isBadRequest() );

    }

}