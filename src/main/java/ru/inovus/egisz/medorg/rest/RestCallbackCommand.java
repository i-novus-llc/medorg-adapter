package ru.inovus.egisz.medorg.rest;

import java.io.Serializable;

public class RestCallbackCommand implements Serializable {

    private static final long serialVersionUID = 2L;

    private final String callbackUrl;
    private final String authorizedUserName;

    public RestCallbackCommand(String authorizedUserName, String callbackUrl) {
        this.authorizedUserName = authorizedUserName;
        this.callbackUrl = callbackUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getAuthorizedUserName() {
        return authorizedUserName;
    }
}

