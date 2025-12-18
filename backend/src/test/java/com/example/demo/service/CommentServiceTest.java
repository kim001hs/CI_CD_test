package com.example.demo.service;

import com.example.demo.domain.Comment;
import com.example.demo.domain.Post;
import com.example.demo.domain.User;
import com.example.demo.dto.CommentRequestDto;
import com.example.demo.dto.CommentResponseDto;
import com.example.demo.repository.CommentRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Post post;
    private Comment comment;

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
                .build();

        comment = Comment.builder()
                .id(1L)
                .content("Test Comment")
                .user(user)
                .post(post)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("댓글 생성 테스트")
    void createComment() {
        // given
        CommentRequestDto requestDto = new CommentRequestDto("Test Comment", null);
        given(userRepository.findByUserId("testuser")).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        CommentResponseDto responseDto = commentService.createComment("testuser", 1L, requestDto);

        // then
        assertThat(responseDto.getContent()).isEqualTo("Test Comment");
        assertThat(responseDto.getAuthorName()).isEqualTo("Test User");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 생성 테스트")
    void createReplyComment() {
        // given
        CommentRequestDto requestDto = new CommentRequestDto("Reply Comment", 1L);
        Comment replyComment = Comment.builder()
                .id(2L)
                .content("Reply Comment")
                .user(user)
                .post(post)
                .parent(comment)
                .build();

        given(userRepository.findByUserId("testuser")).willReturn(Optional.of(user));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
        given(commentRepository.save(any(Comment.class))).willReturn(replyComment);

        // when
        CommentResponseDto responseDto = commentService.createComment("testuser", 1L, requestDto);

        // then
        assertThat(responseDto.getContent()).isEqualTo("Reply Comment");
        assertThat(responseDto.getParentId()).isEqualTo(1L);
    }
}
