package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.Role;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.RoleRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.dto.RoleDto;
import com.example.digitalplatform.dto.UserAccountDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;


    public User registerNewUserAccount(UserAccountDto accountDto)  {
        User old = userRepository.findByLogin(accountDto.getUserName());
        if (Objects.nonNull(old)) {
            log.error("There is an account with that user name: " + accountDto.getUserName());
        }
        User user = new User();
        user.setLogin(accountDto.getUserName());
        user.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        user.setEnabled(true);

        Role byCode = roleRepository.findByCode(accountDto.getRoleCode());
        user.setRole(byCode);

        user.setFirstName(accountDto.getFirstName());
        user.setLastName(accountDto.getLastName());
        user.setInstitute(accountDto.getInstitution());
        User saved = userRepository.save(user);

        return saved;
    }


    public List<UserAccountDto> getAllUserAccounts() {
        List<User> users = userRepository.findAll();
        List<UserAccountDto> result = new ArrayList<>();
        for (User user : users) {
            UserAccountDto dto = new UserAccountDto();
            dto.setUserName(user.getLogin());
            Role role = user.getRole();
            dto.setRoleCode(role.getCode());
            dto.setRoleName(role.getName());
            dto.setInstitution(user.getInstitute());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            result.add(dto);
        }
        return result;
    }

    public List<RoleDto> getAvailableRoles() {
        List<Role> all = roleRepository.findAll();
        List<RoleDto> dtos = all.stream().map(role ->
            new RoleDto(role.getCode(), role.getName())
        ).toList();
        return dtos;
    }
}
