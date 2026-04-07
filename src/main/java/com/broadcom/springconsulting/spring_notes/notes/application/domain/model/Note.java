package com.broadcom.springconsulting.spring_notes.notes.application.domain.model;

import java.util.UUID;

public record Note( UUID id, String title, String content ) {
}