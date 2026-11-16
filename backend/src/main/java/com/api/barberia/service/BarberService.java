package com.api.barberia.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.api.barberia.dto.BarberCreateDTO;
import com.api.barberia.dto.BarberResponseDTO;
import com.api.barberia.model.Barbers;
import com.api.barberia.model.Users;
import com.api.barberia.repository.BarberRepository;
import com.api.barberia.repository.UserRepository;

@Service
@Transactional
public class BarberService {
    
    private final BarberRepository barberRepository;
    private final UserRepository userRepository;

    public BarberService(BarberRepository barberRepository, UserRepository userRepository) {
        this.barberRepository = barberRepository;
        this.userRepository = userRepository;
    }

    // Obtiene la lista de todos los barberos registrados.
    public List<BarberResponseDTO> getAllBarbers() {
        return  barberRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    // Obtiene únicamente los barberos que se encuentran activos.
    public List<BarberResponseDTO> getActiveBarbers() {
        return  barberRepository.findByIsActiveTrue().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    // Registra un nuevo barbero asociándolo a un usuario existente.
    public BarberResponseDTO createBarber(BarberCreateDTO dto) {
        Users user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        validateBarberUser(user);
        if (barberRepository.findByUserIdId(user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario ya tiene un perfil de barbero.");
        }

        Barbers barber = new Barbers();
        barber.setUser(user);
        barber.setSpecialty(dto.getSpecialty());
        barber.setActive(dto.getIsActive());

        Barbers saved = barberRepository.save(barber);
        return mapToResponseDTO(saved);
    }

    
    // Actualiza la información de un barbero existente.
    public BarberResponseDTO updateBarber(Long id, BarberCreateDTO dto) {
        Barbers barber = barberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Barbero no encontrado."));
        
        Users user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        validateBarberUser(user);
        if (!barber.getUser().getId().equals(user.getId())
                && barberRepository.findByUserIdId(user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario ya tiene un perfil de barbero.");
        }
        
        barber.setUser(user);
        barber.setSpecialty(dto.getSpecialty());
        barber.setActive(dto.getIsActive());
        
        Barbers updatedBarber = barberRepository.save(barber);
        return mapToResponseDTO(updatedBarber);
    }
    
    // Elimina un barbero por su ID.
    public void deleteBarber(Long id) {
        Barbers barber = barberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Barbero no encontrado."));
        barber.setActive(false);
        barberRepository.save(barber);
    }

    private static void validateBarberUser(Users user) {
        if (!"BARBER".equalsIgnoreCase(user.getRolesId().getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El usuario debe tener el rol BARBER para crear un perfil.");
        }
    }
    // Mapea una entidad Barbers a su respectivo DTO de respuesta.
    private BarberResponseDTO mapToResponseDTO(Barbers barber) {
        BarberResponseDTO dto = new BarberResponseDTO();
        dto.setId(barber.getId());
        dto.setSpecialty(barber.getSpecialty());
        dto.setIsActive(barber.isActive());
        dto.setUserId(barber.getUser() != null ? barber.getUser().getId() : null);
        if (barber.getUser() != null) {
            dto.setFirstName(barber.getUser().getFirstName());
            dto.setLastName(barber.getUser().getLastName());
        }
        dto.setCreatedAt(barber.getCreatedAt());
        return dto;
    }

}
