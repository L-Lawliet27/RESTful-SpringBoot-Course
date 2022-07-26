package com.appsdeveloperblog.app.ws.service.impl;

import com.appsdeveloperblog.app.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.app.ws.io.entity.PasswordResetTokenEntity;
import com.appsdeveloperblog.app.ws.io.entity.RoleEntity;
import com.appsdeveloperblog.app.ws.io.repositories.PasswordResetTokenRepository;
import com.appsdeveloperblog.app.ws.io.repositories.RoleRepository;
import com.appsdeveloperblog.app.ws.io.repositories.UserRepository;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import com.appsdeveloperblog.app.ws.security.UserPrincipal;
import com.appsdeveloperblog.app.ws.service.UserService;
import com.appsdeveloperblog.app.ws.shared.AmazonSES;
import com.appsdeveloperblog.app.ws.shared.Utils;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDTO;
import com.appsdeveloperblog.app.ws.shared.dto.UserDTO;
import com.appsdeveloperblog.app.ws.ui.model.response.ErrorMessages;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    Utils utils;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    AmazonSES amazonSES;

    @Autowired
    RoleRepository roleRepository;

    @Override
    public UserDTO createUser(UserDTO user) {

        //This line checks whether the user already exists via the unique field
        if (userRepository.findByEmail(user.getEmail()) != null)
            throw new UserServiceException("Record Already Exists");

        for (int i = 0; i < user.getAddresses().size(); i++) {
            AddressDTO address = user.getAddresses().get(i);
            address.setUserDetails(user);
            address.setAddressId(utils.generateAddressId(30));

            user.getAddresses().set(i,address);
        }

        ModelMapper modelMapper = new ModelMapper();

        //UserEntity userEntity = new UserEntity();
        //BeanUtils.copyProperties(user, userEntity); //Remember that for this to work, fields must match
        UserEntity userEntity = modelMapper.map(user, UserEntity.class);

        //To Delete - These fields cannot be null, so for now we hardcode until we have them set-up
        //userEntity.setEncryptedPassword("tmp");
        //userEntity.setUserId("tmp_userId");
        //--------------------------------------------------------------------------------------------

        String publicUserId = utils.generateUserId(30); //you must pass the number of characters
        userEntity.setUserId(publicUserId);

        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));

        //Set Roles
        Collection<RoleEntity> roleEntities = new HashSet<>();
        for (String role : user.getRoles()) {
            RoleEntity roleEntity = roleRepository.findByName(role);
            if (roleEntity!=null)
                roleEntities.add(roleEntity);
        }
        userEntity.setRoles(roleEntities);

        UserEntity storedUserDetails = userRepository.save(userEntity);
        //UserDTO returnValue = new UserDTO();
        //BeanUtils.copyProperties(storedUserDetails, returnValue);

        UserDTO returnValue = modelMapper.map(storedUserDetails, UserDTO.class);

        //Send email to user for verification
        amazonSES.verifyEmail(returnValue);

        return returnValue;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
         UserEntity userEntity = userRepository.findByEmail(email);
         if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

         //return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());

        //return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), userEntity.getEmailVerificationStatus(),
        //        true,true,true,new ArrayList<>());

        return new UserPrincipal(userEntity);
    }


    @Override
    public UserDTO getUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) throw new UsernameNotFoundException("User with Email: "+ email + " Not Found");
        UserDTO returnValue = new UserDTO();
        BeanUtils.copyProperties(userEntity, returnValue);
        return returnValue;
    }

    @Override
    public UserDTO getUserByUserId(String userId) {
        UserDTO returnValue = new UserDTO();
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UsernameNotFoundException("User with ID: "+ userId + " Not Found");
        BeanUtils.copyProperties(userEntity, returnValue);
        return returnValue;
    }

    @Override
    public UserDTO updateUser(String userId,UserDTO userDTO) {
        UserDTO returnValue = new UserDTO();
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UsernameNotFoundException("User with ID: "+ userId + " Not Found");

        String updatedName = userDTO.getFirstName();
        if (updatedName.isEmpty()) throw new UserServiceException(ErrorMessages.FIRSTNAME_NOT_SET.getErrorMessage());
        userEntity.setFirstName(updatedName);

        String updatedLastName = userDTO.getLastName();
        if (updatedLastName.isEmpty()) throw new UserServiceException(ErrorMessages.LASTNAME_NOT_SET.getErrorMessage());
        userEntity.setLastName(updatedLastName);
        //Password and Email must be authenticated outside of this scope

        UserEntity updatedUserDetails = userRepository.save(userEntity); //save() method saves data to the database
        BeanUtils.copyProperties(updatedUserDetails, returnValue);

        return returnValue;
    }

    @Override
    public void deleteUser(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        userRepository.delete(userEntity);
    }

    @Override
    public List<UserDTO> getUsers(int page, int limit) {
        List<UserDTO> returnValue = new ArrayList<>();

        if (page > 0) page--;

        Pageable pageableRequest = PageRequest.of(page, limit);
        Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
        List<UserEntity> users = usersPage.getContent();

        for (UserEntity userEntity : users) {
            UserDTO userDto = new UserDTO();
            BeanUtils.copyProperties(userEntity,userDto);
            returnValue.add(userDto);
        }
        return returnValue;
    }

    @Override
    public boolean verifyEmailToken(String token) {

        UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);

        if(userEntity == null || Utils.hasTokenExpired(token)) return false;

        userEntity.setEmailVerificationToken(null);
        userEntity.setEmailVerificationStatus(Boolean.TRUE);
        userRepository.save(userEntity);

        return true;
    }

    @Override
    public boolean requestPasswordReset(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity==null) return false;

        String token = utils.generatePasswordVerificationToken(userEntity.getUserId());
        PasswordResetTokenEntity pwdResetToken = new PasswordResetTokenEntity();
        pwdResetToken.setToken(token);
        pwdResetToken.setUserDetails(userEntity);

        passwordResetTokenRepository.save(pwdResetToken);

        boolean returnValue = new AmazonSES().sendPasswordResetRequest(
                    userEntity.getFirstName(),
                    userEntity.getEmail(),
                    token);

        return returnValue;
    }

    @Override
    public boolean resetPassword(String token, String password) {
        if (Utils.hasTokenExpired(token)) return false;

        PasswordResetTokenEntity pwdResetToken = passwordResetTokenRepository.findByToken(token);
        if(pwdResetToken == null) return false;

        //Prepare new Password
        String encodedPwd = bCryptPasswordEncoder.encode(password);

        //Update user password on db
        UserEntity userEntity = pwdResetToken.getUserDetails();
        userEntity.setEncryptedPassword(encodedPwd);
        UserEntity savedUserEntity = userRepository.save(userEntity);

        //Check if password was saved
        boolean returnValue = savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPwd);

        //Delete password reset token from db
        passwordResetTokenRepository.delete(pwdResetToken);

        return returnValue;
    }
}
