package com.kapitalbank.payment.service;

import com.kapitalbank.payment.dao.entity.Token;
import com.kapitalbank.payment.dao.entity.User;
import com.kapitalbank.payment.dao.repo.UserRepository;
import com.kapitalbank.payment.mapper.TokenMapper;
import com.kapitalbank.payment.mapper.UserMapper;
import com.kapitalbank.payment.model.dto.AuthRequestDto;
import com.kapitalbank.payment.model.dto.ChangePasswordDto;
import com.kapitalbank.payment.model.dto.EmailDto;
import com.kapitalbank.payment.model.dto.ForgetPasswordDto;
import com.kapitalbank.payment.model.dto.UserRegistrationDto;
import com.kapitalbank.payment.model.dto.UserResponseDto;
import com.kapitalbank.payment.model.exception.NotFoundException;
import com.kapitalbank.payment.model.exception.UserRegisterException;
import com.kapitalbank.payment.util.EmailUtil;
import com.kapitalbank.payment.util.JwtUtil;
import com.kapitalbank.payment.util.TokenUtil;
import com.kapitalbank.payment.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.kapitalbank.payment.model.enums.LinkType.FORGET_PASSWORD;
import static com.kapitalbank.payment.model.enums.LinkType.REGISTRATION;

@Service
public class UserService implements UserDetailsService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserUtil userUtil;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final TokenMapper tokenMapper;
    private final EmailUtil emailUtil;
    private final TokenUtil tokenUtil;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(@Lazy AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       UserUtil userUtil,
                       TokenUtil tokenUtil,
                       TokenService tokenService,
                       TokenMapper tokenMapper,
                       JwtUtil jwtUtil,
                       EmailUtil emailUtil,
                       UserMapper userMapper,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userUtil = userUtil;
        this.tokenUtil = tokenUtil;
        this.tokenMapper = tokenMapper;
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
        this.emailUtil = emailUtil;
        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Value("${token.lifetime}")
    private Long tokenLifeTime;

    @Value("${token.extended-lifetime}")
    private Long tokenExtendedLifeTime;

    @Transactional
    public void registerUser(UserRegistrationDto dto) {
        List<User> userEntities = userUtil.findUserByEmail(dto.getEmail());
        String activationToken = tokenUtil.generateToken();
        User user;

        if (userEntities.isEmpty()) {
            user = userRepository.save(userUtil.buildUser(dto, false));

            tokenService.saveToken(tokenMapper.toToken(activationToken, user.getId()));

            EmailDto emailDto = emailUtil.generateActivationEmail(activationToken, REGISTRATION);
            emailUtil.send(emailDto.getFrom(), dto.getEmail(), emailDto.getSubject(), emailDto.getBody());
        } else {
            user = userEntities.getFirst();

            if (Boolean.TRUE.equals(user.getIsActive()))
                throw new UserRegisterException("User already exist");

            tokenService.reSetActivationToken(user, activationToken);

            EmailDto emailDto = emailUtil.generateActivationEmail(activationToken, REGISTRATION);
            emailUtil.send(emailDto.getFrom(), dto.getEmail(), emailDto.getSubject(), emailDto.getBody());

            throw new UserRegisterException("Activation email has sent");
        }
    }

    public void activateAccount(String activationToken) {
        Token tokenEntity = tokenService.getToken(activationToken);

        User user = userUtil.findUserById(tokenEntity.getUserId());
        user.setIsActive(true);
        userRepository.save(user);

        tokenService.deleteById(tokenEntity.getId());
    }

    public void forgetPassword(ForgetPasswordDto dto) {
        String email = dto.getEmail();

        User user = userUtil.findActiveUserByEmail(email);

        String activationToken = tokenUtil.generateToken(email);
        tokenService.saveToken(tokenMapper.toToken(activationToken, user.getId()));

        EmailDto emailDto = emailUtil.generateActivationEmail(activationToken, FORGET_PASSWORD);
        emailUtil.send(emailDto.getFrom(), dto.getEmail(), emailDto.getSubject(), emailDto.getBody());
    }

    public void updatePassword(String token, ChangePasswordDto dto) {
        Token tokenEntity = tokenService.getToken(token);

        User user = userUtil.findUserById(tokenEntity.getUserId());
        user.setPassword(bCryptPasswordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        tokenService.deleteById(tokenEntity.getId());
    }

    @Transactional
    public String authenticate(AuthRequestDto dto) {
        List<User> users = userUtil.findUserByEmailOrUsername(dto.getEmailOrNickname(), dto.getEmailOrNickname());
        if (users.isEmpty()) throw new NotFoundException("The username or password not found");

        User user = users.getFirst();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), dto.getPassword())
        );

        Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
        String token = jwtUtil.generateToken(userDetails);

        return token;
    }

    public UserResponseDto getUser(HttpServletRequest request) {
        User currentUser = userUtil.getCurrentUser(request);

        return userMapper.toDto(currentUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User userEntity = userUtil.findActiveUserByEmail(email);

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        userEntity.getRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority(role.getName()))
        );

        return new org.springframework.security.core.userdetails.User(userEntity.getEmail(), userEntity.getPassword(), authorities);
    }

}
