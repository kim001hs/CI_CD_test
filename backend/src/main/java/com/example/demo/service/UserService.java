package com.example.demo.service;

import com.example.demo.auth.JwtUtil;
import com.example.demo.domain.User;
import com.example.demo.dto.UserRequestDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService{
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Long register(UserRequestDto userRequestDto) {
        validateDuplicateUser(userRequestDto);
        
        String userId = userRequestDto.getUserId();
        String password = userRequestDto.getPassword();
        String name = userRequestDto.getName();

        User user = User.builder()
                .userId(userId)
                .password(passwordEncoder.encode(password)) // 실제로는 암호화된 비밀번호 사용
                .name(name)
                .build();

        userRepository.save(user);
        return user.getId();
    }

    private void validateDuplicateUser(UserRequestDto userRequestDto) {
        String userId = userRequestDto.getUserId();
        String password = userRequestDto.getPassword();
        String name = userRequestDto.getName();

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자 ID는 필수 입력 항목입니다.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수 입력 항목입니다.");
        }

        if (userRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }
    }

    @Transactional
    public void updateUser(UserRequestDto userRequestDto) {
        String userId = userRequestDto.getUserId();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (userRequestDto.getName() != null && !userRequestDto.getName().trim().isEmpty()) {
            user.updateName(userRequestDto.getName());
        }

        if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isEmpty()) {
            if (userRequestDto.getPassword().length() < 8) {
                throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
            }
            user.updatePassword(passwordEncoder.encode(userRequestDto.getPassword()));
        }
    }

    public UserResponseDto login(UserRequestDto userRequestDto) {
        String userId = userRequestDto.getUserId();
        String password = userRequestDto.getPassword();

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getUserId());

        return UserResponseDto.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .name(user.getName())
                .token(token)
                .build();
    }

    public UserResponseDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserResponseDto.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .name(user.getName())
                .build();
    }

    public UserResponseDto findByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserResponseDto.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .name(user.getName())
                .build();
    }

    public List<UserResponseDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> UserResponseDto.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .name(user.getName())
                .build())
                .toList();
    }
}
