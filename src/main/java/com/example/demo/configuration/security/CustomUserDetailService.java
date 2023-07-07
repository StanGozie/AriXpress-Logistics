package com.example.demo.configuration.security;

import com.example.demo.model.Customer;
import com.example.demo.model.Staff;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {


    private final StaffRepository staffRepository;

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<Staff> staff = staffRepository.findByEmail(email);
        if (staff.isPresent()) {
            return new org.springframework.security.core.userdetails.User(staff.get().getEmail(),
                    staff.get().getPassword(), Collections.singleton(new SimpleGrantedAuthority(staff.get().getRole().name())));
        }
        Optional<Customer> customer = customerRepository.findByEmail(email);
        if (customer.isPresent()) {
            return new org.springframework.security.core.userdetails.User(customer.get().getEmail(),
                    customer.get().getPassword(), Collections.singleton(new SimpleGrantedAuthority(customer.get().getRole().name())));
        }
        throw new UsernameNotFoundException("User not found with email: " + email);


    }

}
