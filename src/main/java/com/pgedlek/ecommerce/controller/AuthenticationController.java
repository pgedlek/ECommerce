package com.pgedlek.ecommerce.controller;

import com.pgedlek.ecommerce.exception.ResourceNotFoundException;
import com.pgedlek.ecommerce.model.AppRole;
import com.pgedlek.ecommerce.model.Role;
import com.pgedlek.ecommerce.model.User;
import com.pgedlek.ecommerce.payload.*;
import com.pgedlek.ecommerce.repository.RoleRepository;
import com.pgedlek.ecommerce.repository.UserRepository;
import com.pgedlek.ecommerce.security.jwt.JwtUtils;
import com.pgedlek.ecommerce.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        ResponseCookie responseCookie = jwtUtils.generateJwtCookie(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), responseCookie.getValue(), roles);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(response);
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return new ResponseEntity<>(new MessageResponse("Error: username is already taken!"), HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return new ResponseEntity<>(new MessageResponse("Error: email is already taken!"), HttpStatus.BAD_REQUEST);
        }

        User user = new User(signupRequest.getUsername(), signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword()));

        Set<String> requestRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (requestRoles == null || requestRoles.isEmpty()) {
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("User Role is not found"));
            roles.add(userRole);
        } else {
            requestRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Admin Role is not found"));
                        roles.add(adminRole);
                        break;
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("Seller Role is not found"));
                        roles.add(sellerRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("User Role is not found"));
                        roles.add(userRole);
                        break;
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);
        return new ResponseEntity<>(new MessageResponse("User registered successfully!"), HttpStatus.OK);
    }

    @GetMapping("/username")
    public ResponseEntity<MessageResponse> currentUserName(Authentication authentication) {
        if (authentication != null) {
            return ResponseEntity.ok().body(new MessageResponse(authentication.getName()));
        }
        return ResponseEntity.ok().body(new MessageResponse("Unknown user"));
    }

    @GetMapping("/user")
    public ResponseEntity<?> currentUserDetails(Authentication authentication) {
        if (authentication == null) {
            log.error("Empty authentication provided");
            return new ResponseEntity<>("Wrong authentication provided.", HttpStatus.UNAUTHORIZED);
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/userdetails")
    public ResponseEntity<?> currentUserWithAddress(Authentication authentication) {
        if (authentication == null) {
            log.error("Empty authentication provided");
            return new ResponseEntity<>("Wrong authentication provided.", HttpStatus.UNAUTHORIZED);
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));

        List<AddressDTO> addressDTOS = user.getAddresses().stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();

        UserDetailsResponse response = new UserDetailsResponse(user.getUsername(), user.getEmail(), addressDTOS);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signOut() {
        ResponseCookie responseCookie = jwtUtils.deleteJwtCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
}
