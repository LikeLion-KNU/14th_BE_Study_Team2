package com.example.community.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RejectResponse {
    private Long userId;
    private String status;
}
