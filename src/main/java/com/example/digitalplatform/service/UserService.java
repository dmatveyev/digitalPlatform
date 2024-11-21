package com.example.digitalplatform.service;

import com.example.digitalplatform.db.model.Customer;
import com.example.digitalplatform.db.model.User;
import com.example.digitalplatform.db.model.Worker;
import com.example.digitalplatform.db.repository.CustomerRepository;
import com.example.digitalplatform.db.repository.RoleRepository;
import com.example.digitalplatform.db.repository.UserRepository;
import com.example.digitalplatform.db.repository.WorkerRepository;
import com.example.digitalplatform.dto.UserAccountDto;
import com.example.digitalplatform.dto.UserType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    CustomerRepository customerRepository;
    WorkerRepository workerRepository;

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
        User saved = userRepository.save(user);

        switch (type) {
            case TEACHER -> {
                Worker worker = new Worker();
                worker.setUser(saved);
                worker.setFirstName(accountDto.getFirstName());
                worker.setLastName(accountDto.getLastName());
                worker.setInstitute(accountDto.getInstitution());
                workerRepository.save(worker);
            }
            case STUDENT -> {
                Customer customer = new Customer();
                customer.setUser(saved);
                customer.setFirstName(accountDto.getFirstName());
                customer.setLastName(accountDto.getLastName());
                customer.setSchool(accountDto.getInstitution());
                customerRepository.save(customer);
            }
        }
        return saved;
    }


    public List<UserAccountDto> getAllUserAccounts() {
        List<Worker> workers = workerRepository.findAll();
        List<Customer> customers = customerRepository.findAll();
        List<UserAccountDto> result = new ArrayList<>();
        for (Worker worker : workers) {
            User user = worker.getUser();
            UserAccountDto dto = new UserAccountDto();
            dto.setUserName(user.getLogin());
            dto.setType(UserType.TEACHER);
            dto.setInstitution(worker.getInstitute());
            dto.setFirstName(worker.getFirstName());
            dto.setLastName(worker.getLastName());
            result.add(dto);
        }
        for (Customer worker : customers) {
            User user = worker.getUser();
            UserAccountDto dto = new UserAccountDto();
            dto.setUserName(user.getLogin());
            dto.setType(UserType.STUDENT);
            dto.setInstitution(worker.getSchool());
            dto.setFirstName(worker.getFirstName());
            dto.setLastName(worker.getLastName());
            result.add(dto);
        }
        return result;
    }
}
