package com.appsdeveloperblog.app.ws.io.repositories;

import com.appsdeveloperblog.app.ws.io.entity.AddressEntity;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    static boolean recordsCreated = false;
    private final String userId = "userId123";

    @BeforeEach
    void setUp() throws Exception{
        if (!recordsCreated) createRecords();
    }

    @Test
    final void testGetVerifiedUsers(){
        Pageable pageableRequest = PageRequest.of(0,2);
        Page<UserEntity> pages = userRepository.findAllUsersWithVerifiedEmail(pageableRequest);
        assertNotNull(pages);

        List<UserEntity> userEntities = pages.getContent();
        assertNotNull(userEntities);
        assertEquals(1,userEntities.size());
    }


    @Test
    final void testFindUserByFirstname() {

        String firstName ="Andres";

        List<UserEntity> users = userRepository.findUserByFirstName(firstName);
        assertNotNull(users);
        assertEquals(1,users.size());

        UserEntity user = users.get(0);
        assertEquals(firstName,user.getFirstName());
    }


    @Test
    final void testFindUserByLastname() {

        String lastName ="Salazar";

        List<UserEntity> users = userRepository.findUserByLastName(lastName);
        assertNotNull(users);
        assertEquals(1,users.size());

        UserEntity user = users.get(0);
        assertEquals(lastName,user.getLastName());
    }

    @Test
    final void testFindUserByKeyword() {

        String keyword ="Sal";

        List<UserEntity> users = userRepository.findUserByKeyword(keyword);
        assertNotNull(users);

        UserEntity user = users.get(0);
        assertTrue(user.getFirstName().contains(keyword) ||
                user.getLastName().contains(keyword));
    }

    @Test
    final void testFindUserFirstNameAndLastNameByKeyword() {

        String keyword ="Sal";

        List<Object[]> users = userRepository.findUserFirstNameAndLastNameByKeyword(keyword);
        assertNotNull(users);
        assertEquals(1, users.size());

        Object[] user = users.get(0);
        assertEquals(2, user.length);

        String userFirstName = String.valueOf(user[0]);
        String userLastName = String.valueOf(user[1]);

        assertNotNull(userFirstName);
        assertNotNull(userLastName);
    }
    @Test
    final void testUpdateUserEmailVerificationStatus() {

        boolean statusValue = true;
        userRepository.updateUserEmailVerificationStatus(statusValue,userId);

        UserEntity storedUserDetails = userRepository.findByUserId(userId);

        boolean storedEmailStatus = storedUserDetails.getEmailVerificationStatus();

        assertEquals(statusValue,storedEmailStatus);
    }


    @Test
    final void testFindUserEntityByUserId(){
        UserEntity userEntity = userRepository.findUserEntityByUserId(userId);

        assertNotNull(userEntity);
        assertEquals(userId, userEntity.getUserId());
    }


    @Test
    final void testGetUserEntityFullNameById(){
        List<Object[]> users = userRepository.findUserEntityFullNameByUserId(userId);
        assertNotNull(users);
        assertEquals(1, users.size());

        Object[] user = users.get(0);
        assertEquals(2, user.length);

        String userFirstName = String.valueOf(user[0]);
        String userLastName = String.valueOf(user[1]);

        assertNotNull(userFirstName);
        assertNotNull(userLastName);
    }

    @Test
    final void testUpdateUserEntityVerificationStatus(){
        boolean statusValue = true;
        userRepository.updateUserEntityVerificationStatus(statusValue,userId);

        UserEntity storedUserDetails = userRepository.findByUserId(userId);

        boolean storedEmailStatus = storedUserDetails.getEmailVerificationStatus();

        assertEquals(statusValue,storedEmailStatus);
    }




    private void createRecords(){
        UserEntity userEntity =  new UserEntity();
        userEntity.setFirstName("Andres");
        userEntity.setLastName("Salazar");
        userEntity.setUserId(userId);
        userEntity.setEncryptedPassword("xxx");
        userEntity.setEmail("test@test.com");
        userEntity.setEmailVerificationStatus(true);


        AddressEntity address = new AddressEntity();
        address.setType("shipping");
        address.setAddressId("addressId123");
        address.setCity("Luthadel");
        address.setCountry("Scadriel");
        address.setPostalCode("Code123");
        address.setStreet("Keep Venture");

        List<AddressEntity> addresses = new ArrayList<>();
        addresses.add(address);


        userEntity.setAddresses(addresses);
        userRepository.save(userEntity);

        recordsCreated=true;
    }
}