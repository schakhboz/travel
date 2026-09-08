package uz.insonline.travel.authentication.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import uz.insonline.travel.authentication.entity.UserEntity;
import uz.insonline.travel.authentication.payload.request.LoginDTO;
import uz.insonline.travel.authentication.payload.response.ApiResponse;
import uz.insonline.travel.authentication.repository.UserRepository;
import uz.insonline.travel.commons.security.JwtProvider;

import static uz.insonline.travel.authentication.payload.enums.ResponseEnum.BEARER;
import static uz.insonline.travel.authentication.payload.enums.ResponseEnum.YOUR_TOKEN;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthService {

    final UserRepository userRepository;

    final AuthenticationManager authenticationManager;

    private final JwtProvider jwtProvider;

    public ApiResponse loginUser(LoginDTO loginDTO) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getLogin(), loginDTO.getPassword()));
        UserEntity user = (UserEntity) authenticate.getPrincipal();

        if (user.getToken() == null || !user.getToken().startsWith(BEARER.getText()) || jwtProvider.getUsernameFromToken(user.getToken().substring(7)) == null) {
            String token = jwtProvider.generateToken(user.getTbLogin());
            user.setToken(BEARER.getText() + token);
            userRepository.save(user);
        }
        return new ApiResponse(YOUR_TOKEN.getText(), 0, user.getToken());
    }
}
