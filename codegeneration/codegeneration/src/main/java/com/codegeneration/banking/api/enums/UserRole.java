package com.codegeneration.banking.api.enums;

public enum UserRole {
    CLIENT("ROLE_CLIENT", "Client"),

    EMPLOYEE("ROLE_EMPLOYEE", "Employee");

    private final String code;
    private final String displayName;

    UserRole(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }


    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}