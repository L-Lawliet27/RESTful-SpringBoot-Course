package com.appsdeveloperblog.app.ws.shared;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UtilsTest {

    @Autowired
    Utils utils;

    @BeforeEach
    void setUp() {
    }

    @Test
    void generateUserId() {
        String userId = utils.generateUserId(30);
        String userId2 = utils.generateUserId(30);
        assertNotNull(userId);
        assertEquals(30, userId.length());
        assertFalse(userId.equalsIgnoreCase(userId2));
    }

    @Test
    void hasTokenNotExpired() {
        String token = utils.generateEmailVerificationToken("userId123");
        assertNotNull(token);

        boolean hasTokenExpired = Utils.hasTokenExpired(token);
        assertFalse(hasTokenExpired);
    }

    @Test
    void hasTokenExpired(){
        //Got it from a previous email confirmation test
        String expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ6a1RrWnJyUmFvNFBISTcxdW04WnRvQURPYmpleWwiLCJleHAiOjE2NTkzMDM4MjZ9.xfteSQBGonLsrsl1k8tMbGpcfr0IXdn-vET9tCuUtnRSixfPJnQqtPC_xZicyljeDGfzcShUpA_gbjAa66-MFg";
        boolean hasTokenExpired = Utils.hasTokenExpired(expiredToken);
        assertTrue(hasTokenExpired);
    }
}