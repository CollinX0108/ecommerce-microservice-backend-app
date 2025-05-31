package com.selimhorri.app.unit.util;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.CredentialDto;

import java.util.Arrays;
import java.util.List;

public class UserUtil {

    public static UserDto getSampleUserDto() {
        CredentialDto credentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("collin0108")
                .password("password123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        return UserDto.builder()
                .userId(1)
                .firstName("Collin")
                .lastName("Gonzalez")
                .imageUrl("https://bootdey.com/img/Content/avatar/avatar1.png")
                .email("collin.gonzalez@gmail.com")
                .phone("+573218770876")
                .credentialDto(credentialDto)
                .build();
    }

    public static User getSampleUser() {
        Credential credential = Credential.builder()
                .credentialId(1)
                .username("collin0108")
                .password("password123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        User user = User.builder()
                .userId(1)
                .firstName("Collin")
                .lastName("Gonzalez")
                .imageUrl("https://bootdey.com/img/Content/avatar/avatar1.png")
                .email("collin.gonzalez@gmail.com")
                .phone("+573218770876")
                .credential(credential)
                .build();
        credential.setUser(user);
        return user;
    }

    public static User getSampleUser2() {
        Credential credential = Credential.builder()
                .credentialId(2)
                .username("maria123")
                .password("password456")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();
        User user = User.builder()
                .userId(2)
                .firstName("Maria")
                .lastName("Rodriguez")
                .imageUrl("https://bootdey.com/img/Content/avatar/avatar2.png")
                .email("maria.rodriguez@gmail.com")
                .phone("+573218770877")
                .credential(credential)
                .build();
        credential.setUser(user);
        return user;
    }

    public static List<User> getSampleUsers() {
        return Arrays.asList(getSampleUser(), getSampleUser2());
    }
}