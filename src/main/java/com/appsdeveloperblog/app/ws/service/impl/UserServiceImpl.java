package com.appsdeveloperblog.app.ws.service.impl;

import com.appsdeveloperblog.app.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.app.ws.io.repositories.UserRepository;
import com.appsdeveloperblog.app.ws.io.entity.UserEntity;
import com.appsdeveloperblog.app.ws.service.UserService;
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
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    Utils utils;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    public UserDTO createUser(UserDTO user) {

        //This line checks whether the user already exists via the unique field
        if (userRepository.findByEmail(user.getEmail()) != null) throw new RuntimeException("Record Already Exists");

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

        UserEntity storedUserDetails = userRepository.save(userEntity);
        //UserDTO returnValue = new UserDTO();
        //BeanUtils.copyProperties(storedUserDetails, returnValue);

        return modelMapper.map(storedUserDetails, UserDTO.class);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
         UserEntity userEntity = userRepository.findByEmail(email);
         if (userEntity == null) throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

         //return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());

        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), userEntity.getEmailVerificationStatus(),
                true,true,true,new ArrayList<>());
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
}
