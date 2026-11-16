package com.api.barberia.service;

import java.util.List;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.api.barberia.dto.RoleDTO;
import com.api.barberia.dto.UserDTO;
import com.api.barberia.dto.UserResponseDTO;
import com.api.barberia.model.Roles;
import com.api.barberia.model.Users;
import com.api.barberia.repository.RoleRepository;
import com.api.barberia.repository.UserRepository;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // Obtiene una lista de todos los usuarios registrados en formato de respuesta DTO.
    public List<UserResponseDTO> getAllUsers(){
        return userRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    // Busca un usuario específico por su ID único.
    public UserResponseDTO getUserById(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        return  mapToResponseDTO(user);
    }

    // Registra un nuevo usuario validando que el correo no esté duplicado y que el rol exista.
    public UserResponseDTO createUser(UserDTO dto) {
        validatePassword(dto.getPassword(), true);
        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        if(userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado.");
        }

        Roles role = roleRepository.findById(dto.getRolesId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El rol especificado no existe."));

        Users user = new Users();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRolesId(role);

        Users savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }

    
    // Actualiza la información de un usuario existente.
    public UserResponseDTO updateUser(Long id, UserDTO dto) {
        validatePassword(dto.getPassword(), false);
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        
        Roles role = roleRepository.findById(dto.getRolesId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El rol especificado no existe."));

        String email = dto.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!user.getEmail().equalsIgnoreCase(email)
                && userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado.");
        }
        
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setEmail(email);
        
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        
        user.setRolesId(role);
        
        Users updatedUser = userRepository.save(user);
        return mapToResponseDTO(updatedUser);
    }
    
    // Elimina un usuario del sistema por su ID.
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }
        userRepository.deleteById(id);
    }

    // Método auxiliar para mapear una entidad Users a un UserResponseDTO.
    private UserResponseDTO mapToResponseDTO(Users user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        if(user.getRolesId() != null) {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setName(user.getRolesId().getName());
            dto.setRolesId(roleDTO);
        }


        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private static void validatePassword(String password, boolean required) {
        if (password == null || password.isBlank()) {
            if (required) {
                throw new IllegalArgumentException("La contraseña es obligatoria para crear un usuario.");
            }
            return;
        }
        int bytes = password.getBytes(StandardCharsets.UTF_8).length;
        if (bytes < 8 || bytes > 72) {
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 72 bytes UTF-8.");
        }
    }

}
