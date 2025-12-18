package com.example.demo.service;

import com.example.demo.domain.Post;
import com.example.demo.domain.User;
import com.example.demo.dto.PostRequestDto;
import com.example.demo.dto.PostResponseDto;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId("testuser")
                .password("password")
                .name("Test User")
                .build();

        post = Post.builder()
                .id(1L)
                .title("Test Title")
                .content("Test Content")
                .user(user)
                .comments(new java.util.ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("게시글 생성 테스트")
    void createPost() {
        // given
        PostRequestDto requestDto = new PostRequestDto("Test Title", "Test Content", null);
        given(userRepository.findByUserId("testuser")).willReturn(Optional.of(user));
        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        Long postId = postService.createPost("testuser", requestDto);

        // then
        assertThat(postId).isEqualTo(1L);
        verify(postRepository).save(any(Post.class));
        verify(userRepository).findByUserId("testuser");
    }

    @Test
    @DisplayName("게시글 조회 테스트")
    void getPost() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        PostResponseDto responseDto = postService.getPost(1L);

        // then
        assertThat(responseDto.getTitle()).isEqualTo("Test Title");
        assertThat(responseDto.getContent()).isEqualTo("Test Content");
        assertThat(responseDto.getAuthorName()).isEqualTo("Test User");
        verify(postRepository).findById(1L);
    }

    @Test
    @DisplayName("게시글 수정 테스트")
    void updatePost() {
        // given
        PostRequestDto requestDto = new PostRequestDto("Updated Title", "Updated Content", null);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        postService.updatePost("testuser", 1L, requestDto);

        // then
        assertThat(post.getTitle()).isEqualTo("Updated Title");
        assertThat(post.getContent()).isEqualTo("Updated Content");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자 불일치")
    void updatePost_fail_not_author() {
        // given
        PostRequestDto requestDto = new PostRequestDto("Updated Title", "Updated Content", null);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThrows(IllegalArgumentException.class,
            () -> postService.updatePost("anotheruser", 1L, requestDto));
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deletePost() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        postService.deletePost("testuser", 1L);

        // then
        verify(postRepository).delete(post);
    }
}
