package com.example.community.common.exception;

import com.example.community.domain.user.repository.ActiveSessionRepository;
import com.example.community.domain.user.repository.UserRepository;
import com.example.community.security.config.SecurityConfig;
import com.example.community.security.handler.JwtAccessDeniedHandler;
import com.example.community.security.handler.JwtAuthenticationEntryPoint;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class, GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean ActiveSessionRepository activeSessionRepository;
    @MockBean UserRepository userRepository;

    @RestController
    static class TestController {
        @GetMapping("/test/error")
        public void throwError() {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Test
    @WithMockUser
    void customException_returns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/test/error"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
    }
}
