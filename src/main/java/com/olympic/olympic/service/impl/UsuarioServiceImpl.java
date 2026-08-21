package com.olympic.olympic.service.impl;

import com.olympic.olympic.dto.UsuarioRequest;
import com.olympic.olympic.dto.UsuarioResponse;
import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.exception.RecursoDuplicadoException;
import com.olympic.olympic.exception.RecursoNoEncontradoException;
import com.olympic.olympic.repository.UsuarioRepository;
import com.olympic.olympic.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Integer id) {
        return UsuarioResponse.fromEntity(buscarPorId(id));
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioRequest request) {
        Usuario usuario = buscarPorId(id);

        boolean correoCambio = !usuario.getCorreo().equalsIgnoreCase(request.getCorreo());
        if (correoCambio && usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new RecursoDuplicadoException("Ese correo ya está registrado");
        }

        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setRol(request.getRol());

        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarEstado(Integer id, boolean activo) {
        Usuario usuario = buscarPorId(id);
        usuario.setActivo(activo);
        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }

    private Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
