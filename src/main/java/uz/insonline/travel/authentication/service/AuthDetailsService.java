package uz.insonline.travel.authentication.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.insonline.travel.authentication.entity.UserEntity;
import uz.insonline.travel.authentication.repository.UserRepository;

import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthDetailsService implements UserDetailsService {
    final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> optionalUser = userRepository.findByTbLogin(username);
        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        UserEntity userEntity = optionalUser.get();
        if (userEntity.getTbActive() != 1) {
            throw new LockedException(username);
        }
        return userEntity;
    }
}
