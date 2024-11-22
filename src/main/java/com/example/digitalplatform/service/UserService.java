package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.repository.RoleRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.dto.UserAccountDto;
import com.example.digitalplatform.dto.UserType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Role;
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
        UserType type = accountDto.getType();

        User user = new User();
        user.setLogin(accountDto.getUserName());
        user.setPassword(passwordEncoder.encode(accountDto.getPassword()));
        user.setEnabled(true);

        user.setRoles(Collections.singletonList(roleRepository.findByName("ROLE_USER")));

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
            dto.setType(null);
            dto.setInstitution(user.getInstitute());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            result.add(dto);
        }
        return result;
    }
}
