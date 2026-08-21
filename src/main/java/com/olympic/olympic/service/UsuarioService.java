package com.olympic.olympic.service;

import com.olympic.olympic.dto.UsuarioRequest;
import com.olympic.olympic.dto.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponse> obtenerTodos();

    UsuarioResponse obtenerPorId(Integer id);

    UsuarioResponse actualizar(Integer id, UsuarioRequest request);

    UsuarioResponse cambiarEstado(Integer id, boolean activo);
}
