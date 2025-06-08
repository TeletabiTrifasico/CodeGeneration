package com.codegeneration.banking.api.dto.login;

import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User data")
public class UserDTO {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Full name", example = "John Doe")
    private String name;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User role", example = "CLIENT")
    private UserRole role;

    @Schema(description = "Whether the user account is enabled", example = "true")
    private boolean enabled;

    /**
     * method to create a UserDTO from a User entity.
     * @param user The User entity.
     * @return A UserDTO instance.
     */
    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .build();
    }
}