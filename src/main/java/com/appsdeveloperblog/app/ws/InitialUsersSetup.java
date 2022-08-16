package com.appsdeveloperblog.app.ws;

import com.appsdeveloperblog.app.ws.io.entity.AuthorityEntity;
import com.appsdeveloperblog.app.ws.io.entity.RoleEntity;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import com.appsdeveloperblog.app.ws.io.repositories.AuthorityRepository;
import com.appsdeveloperblog.app.ws.io.repositories.RoleRepository;
import com.appsdeveloperblog.app.ws.io.repositories.UserRepository;
import com.appsdeveloperblog.app.ws.shared.Roles;
import com.appsdeveloperblog.app.ws.shared.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

@Component
public class InitialUsersSetup {

    @Autowired
    AuthorityRepository authorityRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    Utils utils;


    @EventListener
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event){
        System.out.println("From ApplicationReadyEvent...");

        AuthorityEntity rdAuthority = createAuthority("READ_AUTHORITY");
        AuthorityEntity wrAuthority = createAuthority("WRITE_AUTHORITY");
        AuthorityEntity dlAuthority = createAuthority("DELETE_AUTHORITY");

        createRole(Roles.ROLE_USER.name(), List.of(rdAuthority,wrAuthority));
        RoleEntity roleAdmin = createRole(Roles.ROLE_ADMIN.name(), List.of(rdAuthority,wrAuthority,dlAuthority));

        UserEntity adminUser = new UserEntity();
        adminUser.setFirstName("Andres");
        adminUser.setLastName("Salazar");
        adminUser.setEmail("test@test.com");
        adminUser.setEmailVerificationStatus(true);
        adminUser.setUserId(utils.generateUserId(30));
        adminUser.setEncryptedPassword(bCryptPasswordEncoder.encode("12345678"));
        adminUser.setRoles(List.of(roleAdmin));

        userRepository.save(adminUser);
    }




    @Transactional
    AuthorityEntity createAuthority(String name){
        AuthorityEntity authority = authorityRepository.findByName(name);

        if(authority==null){
            authority = new AuthorityEntity(name);
            authorityRepository.save(authority);
        }
        return authority;
    }


    @Transactional
    RoleEntity createRole(String name, Collection<AuthorityEntity> authorities){
        RoleEntity role = roleRepository.findByName(name);

        if(role==null){
            role = new RoleEntity(name);
            role.setAuthorities(authorities);
            roleRepository.save(role);
        }
        return role;
    }


}
