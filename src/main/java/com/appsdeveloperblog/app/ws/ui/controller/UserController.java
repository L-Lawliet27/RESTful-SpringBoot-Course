package com.appsdeveloperblog.app.ws.ui.controller;

import com.appsdeveloperblog.app.ws.exceptions.UserServiceException;
import com.appsdeveloperblog.app.ws.service.AddressService;
import com.appsdeveloperblog.app.ws.service.UserService;
import com.appsdeveloperblog.app.ws.shared.dto.AddressDTO;
import com.appsdeveloperblog.app.ws.shared.dto.UserDTO;
import com.appsdeveloperblog.app.ws.ui.model.request.PasswordResetModel;
import com.appsdeveloperblog.app.ws.ui.model.request.PasswordResetRequestModel;
import com.appsdeveloperblog.app.ws.ui.model.request.UserDetailsRequestModel;
import com.appsdeveloperblog.app.ws.ui.model.response.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("users") //http://localhost:8080/SpringBoot-Course-Project/users
//@CrossOrigin(origins = {"http://localhost:8084","http://localhost:8083"})
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AddressService addressesService;

    @Autowired
    AddressService addressService;


    //Since we provide the public id of the user in the Get request it must include the parameter to get
    //The order of production matters as the default would be XML unless told otherwise in the request
    @ApiOperation(value = "Get User Details Web Service Endpoint",
            notes = "${userController.GetUser.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
    })
    @GetMapping(path="/{id}", produces = {MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
    public UserRest getUser(@PathVariable String id){
        UserRest returnValue = new UserRest();
        UserDTO userDTO = userService.getUserByUserId(id);

        BeanUtils.copyProperties(userDTO,returnValue);

        return returnValue;
    }

    @ApiOperation(value = "Create User Web Service Endpoint",
            notes = "${userController.CreateUser.ApiOperation.Notes}")
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws UserServiceException {

        if(userDetails.getFirstName().isEmpty()) throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        //UserDTO userDTO = new UserDTO();
        //BeanUtils.copyProperties(userDetails, userDTO);
        ModelMapper modelMapper = new ModelMapper();
        UserDTO userDTO = modelMapper.map(userDetails, UserDTO.class);
        UserDTO createdUser;
        try {
            createdUser = userService.createUser(userDTO);
            return modelMapper.map(createdUser, UserRest.class);
        }catch (Exception e){

            System.out.println(e.getMessage());
        }
        //BeanUtils.copyProperties(createdUser, returnValue);
        return null;
    }

    @ApiOperation(value = "Update User Web Service Endpoint",
            notes = "${userController.UpdateUser.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
    })
    @PutMapping(path="/{id}",
                consumes = {MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE},
                produces = {MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
    public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails){
        UserRest returnValue = new UserRest();

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(userDetails, userDTO);

        UserDTO updateUser = userService.updateUser(id,userDTO);
        BeanUtils.copyProperties(updateUser, returnValue);

        return returnValue;
    }

    @ApiOperation(value = "Delete User Web Service Endpoint",
            notes = "${userController.DeleteUser.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
    })
    @DeleteMapping(path="/{id}", produces = {MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
    public OperationStatusModel deleteUser(@PathVariable String id){
        OperationStatusModel returnValue = new OperationStatusModel();

        returnValue.setOperationName(RequestOperationName.DELETE.name());

        try {
            userService.deleteUser(id);
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        } catch (UserServiceException e){
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());
        }

        return returnValue;
    }

    @ApiOperation(value = "Get Users Web Service Endpoint",
            notes = "${userController.GetUsers.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
    })
    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
    public List<UserRest> getUsers(@RequestParam(value="page", defaultValue = "0")int page,
                                   @RequestParam(value="limit", defaultValue = "25")int limit){
        List<UserRest> returnValue = new ArrayList<>();
        List<UserDTO> users = userService.getUsers(page, limit);

        for (UserDTO userDto : users) {
            UserRest userModel = new UserRest();
            BeanUtils.copyProperties(userDto, userModel);
            returnValue.add(userModel);
        }

        return returnValue;
    }

    @ApiOperation(value = "Get User Addresses Web Service Endpoint",
            notes = "${userController.GetUserAddresses.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
    })
    @GetMapping(path="/{id}/addresses", produces = {MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
    public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String id){
        List<AddressesRest> returnValue = new ArrayList<>();
        List<AddressDTO> addressDTO = addressesService.getAddresses(id);

        Link selfLink;

        if(addressDTO != null && !addressDTO.isEmpty()){
            Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
            returnValue = new ModelMapper().map(addressDTO, listType);

            for (AddressesRest addressRest : returnValue) {
                selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                        .getUserAddress(id,addressRest.getAddressId())).withSelfRel();
                addressRest.add(selfLink);
            }//foreach
        }//if

        //http://localhost:8080/SpringBoot-Course-Project/users/userId
        Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).withRel("user");

        //http://localhost:8080/SpringBoot-Course-Project/users/userId/addresses [handled by this method]
        selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .getUserAddresses(id)).withSelfRel();

        return CollectionModel.of(returnValue, userLink, selfLink);
    }

    @ApiOperation(value = "Get User Address Web Service Endpoint",
            notes = "${userController.GetUserAddress.ApiOperation.Notes}")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="${userController.authorizationHeader.description}", paramType="header")
    })
    @GetMapping(path="/{userId}/addresses/{addressId}", produces = {MediaType.APPLICATION_XML_VALUE,MediaType.APPLICATION_JSON_VALUE})
    public EntityModel<AddressesRest> getUserAddress(@PathVariable String userId, @PathVariable String addressId){
        AddressDTO addressDTO = addressService.getAddress(addressId);
        ModelMapper modelMapper = new ModelMapper();
        AddressesRest returnValue = modelMapper.map(addressDTO, AddressesRest.class);

        List<Link> linkList = new ArrayList<>();

        //http://localhost:8080/SpringBoot-Course-Project/users/userId
        Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");

        //http://localhost:8080/SpringBoot-Course-Project/users/userId/addresses
        Link userAddressesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                        .getUserAddresses(userId)).withRel("addresses");

        //http://localhost:8080/SpringBoot-Course-Project/users/userId/addresses/addressId [handled by this method]
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                        .getUserAddress(userId,addressId)).withSelfRel();

        linkList.add(userLink);
        linkList.add(userAddressesLink);
        linkList.add(selfLink);

        return EntityModel.of(returnValue, linkList);
    }

    //http://localhost:8080/SpringBoot-Course-Project/users/email-verification?token=sdfsdf
    @GetMapping(path="/email-verification", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel verifyEmailToken(@RequestParam(value="token") String token){
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.VERIFY_EMAIL.name());

        if(userService.verifyEmailToken(token))
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        else
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

        return returnValue;
    }

    //http://localhost:8080/SpringBoot-Course-Project/users/password-reset-request
    @PostMapping(path = "/password-reset-request", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel pwdRequest){
        OperationStatusModel returnValue = new OperationStatusModel();
        returnValue.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());

        if(userService.requestPasswordReset(pwdRequest.getEmail()))
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        else
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

        return returnValue;
    }

    //http://localhost:8080/SpringBoot-Course-Project/users/password-reset
    @PostMapping(path="/password-reset", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public OperationStatusModel resetPassword(@RequestBody PasswordResetModel pwdReset){
        OperationStatusModel returnValue = new OperationStatusModel();

        boolean operationResult = userService.resetPassword(
                pwdReset.getToken(),
                pwdReset.getPassword());

        returnValue.setOperationName(RequestOperationStatus.PASSWORD_RESET.name());

        if (operationResult)
            returnValue.setOperationResult(RequestOperationStatus.SUCCESS.name());
        else
            returnValue.setOperationResult(RequestOperationStatus.ERROR.name());

        return returnValue;
    }


}
