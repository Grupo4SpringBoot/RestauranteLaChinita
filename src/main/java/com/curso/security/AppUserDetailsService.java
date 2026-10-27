package com.curso.security;

import com.curso.config.PermisosConfig;
import com.curso.model.AppUser;
import com.curso.model.UserRolType;
import com.curso.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;
    private final PermisosConfig permisosConfig;

    public AppUserDetailsService(AppUserRepository userRepository, PermisosConfig permisosConfig) {
        this.userRepository = userRepository;
        this.permisosConfig = permisosConfig;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        UserRolType rol = usuario.getRol() != null ? usuario.getRol() : UserRolType.CLIENTE;
        return new AppUserDetails(usuario, permisosConfig.permisosDe(rol));
    }
}
