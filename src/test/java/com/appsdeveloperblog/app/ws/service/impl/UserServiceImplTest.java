package com.appsdeveloperblog.app.ws.service.impl;

import com.appsdeveloperblog.app.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.app.ws.io.entity.AddressEntity;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import com.appsdeveloperblog.app.ws.io.repositories.UserRepository;
import com.appsdeveloperblog.app.ws.shared.AmazonSES;
import com.appsdeveloperblog.app.ws.shared.Utils;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDTO;
import com.appsdeveloperblog.app.ws.shared.dto.UserDTO;
import net.bytebuddy.description.method.MethodDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Mock
    Utils utils;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    AmazonSES amazonSES;

    String userId = "userIdTest";
    String encryptPass ="veryEncryptedPassword123";

    UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFirstName("Rand");
        userEntity.setUserId(userId);
        userEntity.setEncryptedPassword(encryptPass);
        userEntity.setEmail("test@test.com");
        userEntity.setEmailVerificationToken("tokenWhatever123");
        userEntity.setAddresses(getAddressesEntity());
    }


    @Test
    void getUser() {

        when(userRepository.findByEmail(anyString())).thenReturn(userEntity);

        UserDTO userDTO = userService.getUser("test@test.com");

        assertNotNull(userDTO);
        assertEquals("Rand", userDTO.getFirstName());

    }

    @Test
    void getUser_UsernameNotFoundException(){
        when(userRepository.findByEmail(anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
            ()-> userService.getUser("test@test.com"));

    }

    @Test
    void createUser_UserServiceException(){
        when(userRepository.findByEmail(anyString())).thenReturn(userEntity);

        UserDTO userDTO = new UserDTO();
        userDTO.setAddresses(getAddressesDto());
        userDTO.setFirstName("Rand");
        userDTO.setLastName("al'Thor");
        userDTO.setPassword("RandomPassword123");
        userDTO.setEmail("test@test.com");

        assertThrows(UserServiceException.class,
            ()-> userService.createUser(userDTO));

    }

    @Test
    final void createUser(){
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(utils.generateAddressId(anyInt())).thenReturn("AnAddressId123");
        when(utils.generateUserId(anyInt())).thenReturn(userId);
        when(bCryptPasswordEncoder.encode(anyString())).thenReturn(encryptPass);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        doNothing().when(amazonSES).verifyEmail(any(UserDTO.class));

        UserDTO userDTO = new UserDTO();
        userDTO.setAddresses(getAddressesDto());
        userDTO.setFirstName("Rand");
        userDTO.setLastName("al'Thor");
        userDTO.setPassword("RandomPassword123");
        userDTO.setEmail("test@test.com");

        UserDTO storedUserDetails = userService.createUser(userDTO);

        assertNotNull(storedUserDetails);
        assertEquals(userEntity.getFirstName(),storedUserDetails.getFirstName());
        assertEquals(userEntity.getLastName(), storedUserDetails.getLastName());
        assertNotNull(storedUserDetails.getUserId());
        assertEquals(userEntity.getAddresses().size(), storedUserDetails.getAddresses().size());
        verify(utils, times(storedUserDetails.getAddresses().size())).generateAddressId(30);
        verify(bCryptPasswordEncoder, times(1)).encode("RandomPassword123");
        verify(userRepository, times(1)).save(any(UserEntity.class));

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


    private List<AddressEntity> getAddressesEntity(){
        List<AddressDTO> addresses = getAddressesDto();
        Type listType = new TypeToken<List<AddressEntity>>(){}.getType();
        return new ModelMapper().map(addresses,listType);
    }



}