package com.codegeneration.banking.api.dto.user;

import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.dto.account.AccountDTO;
import com.codegeneration.banking.api.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private UserRole role;
    private boolean enabled;
    private List<AccountDTO> accounts;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .accounts(
                    user.getAccounts() == null ? new ArrayList<>() :
                    user.getAccounts().stream()
                        .map(AccountDTO::fromEntity)
                        .collect(Collectors.toList())
                )
                .build();
    }
}
