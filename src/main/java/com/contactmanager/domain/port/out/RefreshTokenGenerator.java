package com.contactmanager.domain.port.out;

@FunctionalInterface
public interface RefreshTokenGenerator {

    String newToken();
}
