package com.cagdasbilgin.twitterclone.dto;

import lombok.*;

@Getter
@Setter
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
