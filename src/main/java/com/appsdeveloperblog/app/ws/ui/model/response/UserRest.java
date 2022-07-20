package com.appsdeveloperblog.app.ws.ui.model.response;

import java.util.List;

public class UserRest {
    //This is the class that you are going to return in the method createUser

    //Define fields that will be sent back as a JSON Doc
    private String userId; //Different from the auto-incremented userId from the DB Table
    //Get the fields from the Request-equivalent class
    private String firstName;
    private String lastName;
    private String email;
    private List<AddressesRest> addresses;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<AddressesRest> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<AddressesRest> addresses) {
        this.addresses = addresses;
    }
}
