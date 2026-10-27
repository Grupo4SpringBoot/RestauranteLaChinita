package com.curso.security;

import com.curso.model.AppUser;
import com.curso.model.UserRolType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class AppUserDetails implements UserDetails {

    private final AppUser usuario;
    private final UserRolType rol;
    private final Set<String> permisos;
    private final List<GrantedAuthority> authorities;

    public AppUserDetails(AppUser usuario, Set<String> permisos) {
        this.usuario = usuario;
        this.rol = usuario.getRol() != null ? usuario.getRol() : UserRolType.CLIENTE;
        this.permisos = Set.copyOf(permisos);

        List<GrantedAuthority> lista = new ArrayList<>();
        lista.add(new SimpleGrantedAuthority(this.rol.name()));
        this.permisos.forEach(permiso -> lista.add(new SimpleGrantedAuthority(permiso)));

        this.authorities = List.copyOf(lista);
    }

    public AppUser getUsuario() {
        return usuario;
    }

    public UserRolType getRol() {
        return rol;
    }

    public Set<String> getPermisos() {
        return permisos;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return usuario.isActivo();
    }
}
