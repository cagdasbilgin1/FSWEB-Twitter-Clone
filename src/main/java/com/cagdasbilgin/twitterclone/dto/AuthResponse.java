package com.cagdasbilgin.twitterclone.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String message;
    private Long id;
    private String username;
    private String email;
    private String role;
}
