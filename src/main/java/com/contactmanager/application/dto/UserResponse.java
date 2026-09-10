package com.contactmanager.application.dto;

import java.util.List;

public record UserResponse(String id, String username, String displayName, List<String> roles) {
}
