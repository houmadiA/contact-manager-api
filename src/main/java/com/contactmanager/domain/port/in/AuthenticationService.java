package com.contactmanager.domain.port.in;

import com.contactmanager.domain.model.AuthenticationResult;
import com.contactmanager.domain.model.Credentials;

public interface AuthenticationService {

    AuthenticationResult login(Credentials credentials);

    AuthenticationResult refresh(String refreshToken);

    void logout(String refreshToken);
}
