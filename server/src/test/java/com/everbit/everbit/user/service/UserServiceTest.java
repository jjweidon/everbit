package com.everbit.everbit.user.service;

import com.everbit.everbit.user.entity.BotSetting;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.entity.enums.Role;
import com.everbit.everbit.user.exception.UserException;
import com.everbit.everbit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private OAuth2User oAuth2User;
    private User existingUser;
    private User newUser;
    private BotSetting botSetting;

    @BeforeEach
    void setUp() {
        // OAuth2User Mock 설정
        Map<String, Object> attributes = Map.of(
            "id", "123456789",
            "kakao_account", Map.of(
                "profile", Map.of(
                    "nickname", "테스트유저",
                    "profile_image_url", "https://test.com/image.jpg"
                )
            )
        );
        oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);

        // 기존 사용자 Mock 설정
        existingUser = User.builder()
                .username("kakao-123456789")
                .role(Role.ROLE_USER)
                .nickname("기존유저")
                .image("https://existing.com/image.jpg")
                .isBotActive(false)
                .build();

        // 새 사용자 Mock 설정
        newUser = User.builder()
                .username("kakao-123456789")
                .role(Role.ROLE_USER)
                .nickname("테스트유저")
                .image("https://test.com/image.jpg")
                .isBotActive(false)
                .build();

        // BotSetting Mock 설정
        botSetting = BotSetting.builder()
                .user(newUser)
                .build();
    }

    @Test
    void createOrFindUser_기존사용자존재시_기존사용자반환() {
        // given
        String username = "kakao-123456789";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        // when
        User result = userService.createOrFindUser(oAuth2User);

        // then
        assertNotNull(result);
        assertEquals(existingUser.getUsername(), result.getUsername());
        assertEquals(existingUser.getNickname(), result.getNickname());
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createOrFindUser_새사용자인경우_새사용자생성후반환() {
        // given
        String username = "kakao-123456789";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // when
        User result = userService.createOrFindUser(oAuth2User);

        // then
        assertNotNull(result);
        assertEquals(newUser.getUsername(), result.getUsername());
        assertEquals(newUser.getNickname(), result.getNickname());
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void findUserByUserId_존재하는사용자ID_사용자반환() {
        // given
        String userId = "test-user-id";
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // when
        User result = userService.findUserByUserId(userId);

        // then
        assertNotNull(result);
        assertEquals(existingUser.getUsername(), result.getUsername());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findUserByUserId_존재하지않는사용자ID_UserException발생() {
        // given
        String userId = "non-existent-user-id";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        UserException exception = assertThrows(UserException.class, () -> {
            userService.findUserByUserId(userId);
        });
        assertTrue(exception.getMessage().contains("존재하지 않는 사용자 아이디입니다"));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findUserByUsername_존재하는사용자이름_사용자반환() {
        // given
        String username = "kakao-123456789";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        // when
        User result = userService.findUserByUsername(username);

        // then
        assertNotNull(result);
        assertEquals(existingUser.getUsername(), result.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void findUserByUsername_존재하지않는사용자이름_UserException발생() {
        // given
        String username = "non-existent-username";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when & then
        UserException exception = assertThrows(UserException.class, () -> {
            userService.findUserByUsername(username);
        });
        assertTrue(exception.getMessage().contains("존재하지 않는 사용자 이름입니다"));
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void saveUser_사용자저장_저장성공() {
        // given
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // when
        userService.saveUser(existingUser);

        // then
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void findUsersWithActiveBots_활성화된봇사용자목록_사용자리스트반환() {
        // given
        User activeUser1 = User.builder()
                .username("user1")
                .isBotActive(true)
                .build();
        User activeUser2 = User.builder()
                .username("user2")
                .isBotActive(true)
                .build();
        List<User> activeUsers = Arrays.asList(activeUser1, activeUser2);
        
        when(userRepository.findByIsBotActiveTrue()).thenReturn(activeUsers);

        // when
        List<User> result = userService.findUsersWithActiveBots();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(User::getIsBotActive));
        verify(userRepository, times(1)).findByIsBotActiveTrue();
    }

    @Test
    void findUsersWithActiveBots_활성화된봇없음_빈리스트반환() {
        // given
        when(userRepository.findByIsBotActiveTrue()).thenReturn(List.of());

        // when
        List<User> result = userService.findUsersWithActiveBots();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByIsBotActiveTrue();
    }
}