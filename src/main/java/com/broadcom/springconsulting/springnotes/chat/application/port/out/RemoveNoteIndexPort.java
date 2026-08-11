package com.broadcom.springconsulting.springnotes.chat.application.port.out;

import java.util.UUID;

public interface RemoveNoteIndexPort {

    void remove( UUID noteId );

}
