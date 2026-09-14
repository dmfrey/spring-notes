package com.broadcom.springconsulting.springnotes.notes.adapter.in.endpoint;

import com.broadcom.springconsulting.springnotes.configuration.SecurityConfiguration;
import com.broadcom.springconsulting.springnotes.configuration.WebConfiguration;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.ChecklistItem;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.Note;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteSlice;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.NoteType;
import com.broadcom.springconsulting.springnotes.notes.application.domain.model.event.NoteCreated;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.AddChecklistItemUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.CreateNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.DeleteNoteUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNoteHistoryUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNoteHistoryUseCase.LoadNoteHistoryCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.LoadNotesUseCase.LoadNotesCommand;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.RemoveChecklistItemUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.ReorderChecklistItemsUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.ToggleChecklistItemUseCase;
import com.broadcom.springconsulting.springnotes.notes.application.port.in.UpdateNoteUseCase;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    LoadNoteHistoryUseCase loadNoteHistoryUseCase;

    @MockitoBean
    CreateNoteUseCase createNoteUseCase;

    @MockitoBean
    UpdateNoteUseCase updateNoteUseCase;

    @MockitoBean
    DeleteNoteUseCase deleteNoteUseCase;

    @MockitoBean
    AddChecklistItemUseCase addChecklistItemUseCase;

    @MockitoBean
    RemoveChecklistItemUseCase removeChecklistItemUseCase;

    @MockitoBean
    ToggleChecklistItemUseCase toggleChecklistItemUseCase;

    @MockitoBean
    ReorderChecklistItemsUseCase reorderChecklistItemsUseCase;

    @Test
    void loadNotes_firstPage_returnsSlice() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        var notes = List.of( new Note( noteId, "Test Note", "Test content", NoteType.TEXT, List.of() ) );
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
        var notes = List.of( new Note( noteId, "Test Note", "Test content", NoteType.TEXT, List.of() ) );
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
        var note = new Note( noteId, "My Title", "Some content", NoteType.TEXT, List.of() );
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

        verify( createNoteUseCase ).execute( new CreateNoteUseCase.CreateNoteCommand( TEST_SUBJECT, "My Title", "Some content", NoteType.TEXT, List.of() ) );

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

    @Test
    void loadNoteHistory_returnsEvents() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        var event = new NoteCreated( noteId, TEST_SUBJECT, "Test Note", "Test content", NoteType.TEXT, List.of(), Instant.parse( "2026-01-01T00:00:00Z" ) );
        when( loadNoteHistoryUseCase.execute( any() ) ).thenReturn( List.of( event ) );

        mockMvc.perform( get( "/notes/{id}/events", noteId )
                        .header( "API-Version", "1" )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.length()" ).value( 1 ) )
                .andExpect( jsonPath( "$[0].type" ).value( "NoteCreated" ) )
                .andExpect( jsonPath( "$[0].title" ).value( "Test Note" ) );

        verify( loadNoteHistoryUseCase ).execute( new LoadNoteHistoryCommand( noteId, TEST_SUBJECT ) );

    }

    @Test
    void loadNoteHistory_withNoEvents_returnsEmptyArray() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        when( loadNoteHistoryUseCase.execute( any() ) ).thenReturn( List.of() );

        mockMvc.perform( get( "/notes/{id}/events", noteId )
                        .header( "API-Version", "1" )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$" ).isArray() )
                .andExpect( jsonPath( "$.length()" ).value( 0 ) );

    }

    @Test
    void loadNoteHistory_withoutJwt_returnsUnauthorized() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();

        mockMvc.perform( get( "/notes/{id}/events", noteId )
                        .header( "API-Version", "1" ) )
                .andExpect( status().isUnauthorized() );

    }

    @Test
    void loadNoteHistory_withoutApiVersionHeader_returnsBadRequest() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();

        mockMvc.perform( get( "/notes/{id}/events", noteId )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isBadRequest() );

    }

    @Test
    void updateNote_returnsOkWithUpdatedBody() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        var note = new Note( noteId, "New Title", "New content", NoteType.TEXT, List.of() );
        when( updateNoteUseCase.execute( any() ) ).thenReturn( note );

        mockMvc.perform( put( "/notes/{id}", noteId )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"New Title","content":"New content"}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id" ).value( noteId.toString() ) )
                .andExpect( jsonPath( "$.title" ).value( "New Title" ) )
                .andExpect( jsonPath( "$.content" ).value( "New content" ) );

        verify( updateNoteUseCase ).execute( new UpdateNoteUseCase.UpdateNoteCommand( noteId, TEST_SUBJECT, "New Title", "New content" ) );

    }

    @Test
    void updateNote_withBlankTitle_returnsBadRequest() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();

        mockMvc.perform( put( "/notes/{id}", noteId )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"","content":"New content"}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isBadRequest() );

    }

    @Test
    void updateNote_withBlankContent_returnsBadRequest() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();

        mockMvc.perform( put( "/notes/{id}", noteId )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"New Title","content":""}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isBadRequest() );

    }

    @Test
    void updateNote_withoutJwt_returnsUnauthorized() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();

        mockMvc.perform( put( "/notes/{id}", noteId )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"New Title","content":"New content"}
                                """ ) )
                .andExpect( status().isUnauthorized() );

    }

    @Test
    void updateNote_withoutApiVersionHeader_returnsBadRequest() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();

        mockMvc.perform( put( "/notes/{id}", noteId )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"New Title","content":"New content"}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isBadRequest() );

    }

    @Test
    void deleteNote_returnsNoContent() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();

        mockMvc.perform( delete( "/notes/{id}", noteId )
                        .header( "API-Version", "1" )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isNoContent() );

        verify( deleteNoteUseCase ).execute( new DeleteNoteUseCase.DeleteNoteCommand( noteId, TEST_SUBJECT ) );

    }

    @Test
    void deleteNote_withoutJwt_returnsUnauthorized() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();

        mockMvc.perform( delete( "/notes/{id}", noteId )
                        .header( "API-Version", "1" ) )
                .andExpect( status().isUnauthorized() );

    }

    @Test
    void deleteNote_withoutApiVersionHeader_returnsBadRequest() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();

        mockMvc.perform( delete( "/notes/{id}", noteId )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isBadRequest() );

    }

    @Test
    void createNote_withListType_passesTypeAndItemsToCommand() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        var note = new Note( noteId, "Groceries", null, NoteType.LIST, List.of( new ChecklistItem( UuidCreator.getTimeOrderedEpoch(), "Milk", false ) ) );
        when( createNoteUseCase.execute( any() ) ).thenReturn( note );

        mockMvc.perform( post( "/notes" )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"title":"Groceries","type":"LIST","items":["Milk","Eggs"]}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.type" ).value( "LIST" ) );

        verify( createNoteUseCase ).execute( new CreateNoteUseCase.CreateNoteCommand( TEST_SUBJECT, "Groceries", null, NoteType.LIST, List.of( "Milk", "Eggs" ) ) );

    }

    @Test
    void addChecklistItem_returnsOkWithUpdatedNote() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        var itemId = UuidCreator.getTimeOrderedEpoch();
        var note = new Note( noteId, "Groceries", null, NoteType.LIST, List.of( new ChecklistItem( itemId, "Milk", false ) ) );
        when( addChecklistItemUseCase.execute( any() ) ).thenReturn( note );

        mockMvc.perform( post( "/notes/{id}/items", noteId )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"text":"Milk"}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isOk() )
                .andExpect( jsonPath( "$.items[0].text" ).value( "Milk" ) );

        verify( addChecklistItemUseCase ).execute( new AddChecklistItemUseCase.AddChecklistItemCommand( noteId, TEST_SUBJECT, "Milk" ) );

    }

    @Test
    void removeChecklistItem_returnsOkWithUpdatedNote() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        UUID itemId = UuidCreator.getTimeOrderedEpoch();
        var note = new Note( noteId, "Groceries", null, NoteType.LIST, List.of() );
        when( removeChecklistItemUseCase.execute( any() ) ).thenReturn( note );

        mockMvc.perform( delete( "/notes/{id}/items/{itemId}", noteId, itemId )
                        .header( "API-Version", "1" )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isOk() );

        verify( removeChecklistItemUseCase ).execute( new RemoveChecklistItemUseCase.RemoveChecklistItemCommand( noteId, TEST_SUBJECT, itemId ) );

    }

    @Test
    void toggleChecklistItem_passesDesiredCheckedStateToCommand() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        UUID itemId = UuidCreator.getTimeOrderedEpoch();
        var note = new Note( noteId, "Groceries", null, NoteType.LIST, List.of( new ChecklistItem( itemId, "Milk", true ) ) );
        when( toggleChecklistItemUseCase.execute( any() ) ).thenReturn( note );

        mockMvc.perform( patch( "/notes/{id}/items/{itemId}/toggle", noteId, itemId )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"checked":true}
                                """ )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isOk() );

        verify( toggleChecklistItemUseCase ).execute( new ToggleChecklistItemUseCase.ToggleChecklistItemCommand( noteId, TEST_SUBJECT, itemId, true ) );

    }

    @Test
    void reorderChecklistItems_passesOrderedIdsToCommand() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();
        UUID item1 = UuidCreator.getTimeOrderedEpoch();
        UUID item2 = UuidCreator.getTimeOrderedEpoch();
        var note = new Note( noteId, "Groceries", null, NoteType.LIST, List.of() );
        when( reorderChecklistItemsUseCase.execute( any() ) ).thenReturn( note );

        mockMvc.perform( put( "/notes/{id}/items", noteId )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"orderedItemIds":["%s","%s"]}
                                """.formatted( item2, item1 ) )
                        .with( jwt().jwt( b -> b.subject( TEST_SUBJECT ) ) ) )
                .andExpect( status().isOk() );

        verify( reorderChecklistItemsUseCase ).execute( new ReorderChecklistItemsUseCase.ReorderChecklistItemsCommand( noteId, TEST_SUBJECT, List.of( item2, item1 ) ) );

    }

    @Test
    void addChecklistItem_withoutJwt_returnsUnauthorized() throws Exception {

        UUID noteId = UuidCreator.getTimeOrderedEpoch();

        mockMvc.perform( post( "/notes/{id}/items", noteId )
                        .header( "API-Version", "1" )
                        .contentType( MediaType.APPLICATION_JSON )
                        .content( """
                                {"text":"Milk"}
                                """ ) )
                .andExpect( status().isUnauthorized() );

    }

}