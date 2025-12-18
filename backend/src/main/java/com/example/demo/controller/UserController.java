package com.example.demo.controller;

import com.example.demo.dto.UserRequestDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
//    리액트에서 보낸 로그인/회원가입 요청을 가장 먼저 받는다.
//    요청을 받아 Repository에게 일을 시키고, 결과를 다시 리액트에게 돌려준다.
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public UserResponseDto login(@RequestBody UserRequestDto userRequestDto) {
        return userService.login(userRequestDto);
    }

    @PostMapping("/register")
    public UserResponseDto register(@RequestBody UserRequestDto userRequestDto) {
        userService.register(userRequestDto);
        return userService.login(userRequestDto);
    }

    @GetMapping("/me")
    public UserResponseDto me(@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        return userService.findByUserId(userDetails.getUsername());
    }

    @GetMapping("/users")
    public List<UserResponseDto> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/users/{id}")
    public UserResponseDto getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }
}