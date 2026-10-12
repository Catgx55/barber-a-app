package com.elcaballero.barberia.service;

import com.elcaballero.barberia.dto.ApiDtos.BarberRequest;
import com.elcaballero.barberia.dto.ApiDtos.BarberResponse;
import com.elcaballero.barberia.entity.Barber;
import com.elcaballero.barberia.exception.ConflictException;
import com.elcaballero.barberia.exception.EntityLookup;
import com.elcaballero.barberia.repository.BarberRepository;
import com.elcaballero.barberia.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BarberService {

    private final BarberRepository barbers;
    private final UserRepository users;

    public BarberService(BarberRepository barbers, UserRepository users) {
        this.barbers = barbers;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<BarberResponse> findAll() {
        return barbers.findAllWithUsers().stream().map(BarberService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BarberResponse findById(Long id) {
        Barber barber = barbers.findWithUserById(id)
                .orElseThrow(() -> new com.elcaballero.barberia.exception.ResourceNotFoundException("Barbero", id));
        return toResponse(barber);
    }

    public BarberResponse create(BarberRequest request) {
        if (barbers.existsByUserId(request.userId())) {
            throw new ConflictException("Ese usuario ya tiene un perfil de barbero.");
        }
        Barber barber = new Barber();
        barber.setUser(EntityLookup.user(users, request.userId()));
        apply(barber, request);
        return toResponse(barbers.save(barber));
    }

    public BarberResponse update(Long id, BarberRequest request) {
        Barber barber = EntityLookup.barber(barbers, id);
        if (!barber.getUser().getId().equals(request.userId())
                && barbers.existsByUserIdAndIdNot(request.userId(), id)) {
            throw new ConflictException("Ese usuario ya tiene un perfil de barbero.");
        }
        barber.setUser(EntityLookup.user(users, request.userId()));
        apply(barber, request);
        return toResponse(barbers.save(barber));
    }

    // Se desactiva el perfil para conservar las citas y el historial del barbero.
    public void delete(Long id) {
        Barber barber = EntityLookup.barber(barbers, id);
        barber.setActive(false);
        barbers.save(barber);
    }

    private static void apply(Barber barber, BarberRequest request) {
        barber.setSpecialty(request.specialty().trim());
        barber.setActive(request.active());
    }

    private static BarberResponse toResponse(Barber barber) {
        return new BarberResponse(
                barber.getId(),
                barber.getUser().getId(),
                barber.getUser().getFirstName(),
                barber.getUser().getLastName(),
                barber.getUser().getEmail(),
                barber.getSpecialty(),
                barber.isActive(),
                barber.getCreatedAt()
        );
    }
}
