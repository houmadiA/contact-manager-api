package com.contactmanager.domain.port.out;

import com.contactmanager.domain.model.AccessToken;
import com.contactmanager.domain.model.User;

@FunctionalInterface
public interface AccessTokenIssuer {

    AccessToken issue(User user);
}
