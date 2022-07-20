package com.appsdeveloperblog.app.ws.ui.model.response;

public enum ErrorMessages {

    MISSING_REQUIRED_FIELD("Missing Required Fields. Please Check Documentation for Required Fields"),
    RECORD_ALREADY_EXISTS("Record Already Exists"),
    INTERNAL_SERVER_ERROR("Internal Server Error"),
    NO_RECORD_FOUND("Record with Provided ID Not Found"),
    AUTHENTICATION_FAILED("Authentication Failed"),
    COULD_NOT_UPDATE_RECORD("Could not Update Record"),
    COULD_NOT_DELETE_RECORD("Could Not Delete Record"),
    EMAIL_ADDRESS_NOT_VERIFIED("Email Address Could Not Be Verified"),
    FIRSTNAME_NOT_SET("FirstName Was Not Set. It Cannot Be Empty"),
    LASTNAME_NOT_SET("LastName Was Not Set. It Cannot Be Empty");

    private String errorMessage;

    ErrorMessages(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
