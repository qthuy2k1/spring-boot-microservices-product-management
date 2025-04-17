package com.qthuy2k1.userservice.service;

import com.nimbusds.jose.JOSEException;
import com.qthuy2k1.userservice.dto.request.AuthenticationRequest;
import com.qthuy2k1.userservice.dto.response.AuthenticationResponse;
import com.qthuy2k1.userservice.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface IAuthenticationService {
    IntrospectResponse introspect(String authorizationHeader) throws JOSEException, ParseException;

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void logout(String authorizationHeader) throws ParseException, JOSEException;

    AuthenticationResponse refreshToken(String authorizationHeader) throws ParseException, JOSEException;
}
