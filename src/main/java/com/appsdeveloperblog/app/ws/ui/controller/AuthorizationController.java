package com.appsdeveloperblog.app.ws.ui.controller;

import com.appsdeveloperblog.app.ws.ui.model.request.LoginRequestModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationController {

    @ApiOperation("User Login")
    @ApiResponses(value = {
            @ApiResponse(code=200,
            message = "Response Headers",
            responseHeaders = {
                    @ResponseHeader(name = "authorization",
                            description = "Bearer <JWT Value>",
                            response = String.class),
                    @ResponseHeader(name = "userId",
                            description = "<Public User Id Value>",
                            response = String.class)
            })
    })
    @PostMapping("/users/login")
    public void fakeLogin(@RequestBody LoginRequestModel loginModel){
        throw new IllegalStateException("This Method Should Not Be Called. This Method is Implemented By Spring Security");
    }

}
