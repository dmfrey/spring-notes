package com.broadcom.springconsulting.springnotes.notes.application.domain.model;

import java.util.UUID;

public record Note( UUID id, String title, String content ) {
}