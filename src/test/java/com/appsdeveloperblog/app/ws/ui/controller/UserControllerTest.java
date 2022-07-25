package com.appsdeveloperblog.app.ws.ui.controller;

import com.appsdeveloperblog.app.ws.service.impl.UserServiceImpl;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDTO;
import com.appsdeveloperblog.app.ws.shared.dto.UserDTO;
import com.appsdeveloperblog.app.ws.ui.model.response.UserRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    UserController userController;

    @Mock
    UserServiceImpl userService;

    UserDTO userDTO;
    final String USER_ID="ImAnUser123";


    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setAddresses(getAddressesDto());
        userDTO.setFirstName("Rand");
        userDTO.setLastName("al'Thor");
        userDTO.setPassword("RandomPassword123");
        userDTO.setEmail("test@test.com");
        userDTO.setUserId(USER_ID);
        userDTO.setEmailVerificationStatus(Boolean.FALSE);
        userDTO.setEmailVerificationToken(null);
        userDTO.setEncryptedPassword("ImEncrypted123");
    }

    @Test
    void getUser() {
        when(userService.getUserByUserId(anyString())).thenReturn(userDTO);

        UserRest userRest = userController.getUser(USER_ID);
        assertNotNull(userRest);
        assertEquals(USER_ID, userRest.getUserId());
        assertEquals(userDTO.getFirstName(), userRest.getFirstName());
        assertEquals(userDTO.getLastName(), userRest.getLastName());
        assertEquals(userDTO.getAddresses().size(), userRest.getAddresses().size());

    }


    private List<AddressDTO> getAddressesDto(){
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setType("Shipping");
        addressDTO.setCity("Caimlyn");
        addressDTO.setCountry("Andor");
        addressDTO.setPostalCode("WHATEVER123");
        addressDTO.setStreet("21 Jump Street");

        AddressDTO addressSecDTO = new AddressDTO();
        addressSecDTO.setType("Billing");
        addressSecDTO.setCity("Emond's Field");
        addressSecDTO.setCountry("Two Rivers");
        addressSecDTO.setPostalCode("123WHATEVER");
        addressSecDTO.setStreet("22 Jump Street");

        List<AddressDTO> addresses = new ArrayList<>();
        addresses.add(addressDTO);
        addresses.add(addressSecDTO);

        return addresses;
    }



}