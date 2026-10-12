package com.elcaballero.barberia.exception;

import com.elcaballero.barberia.entity.Appointment;
import com.elcaballero.barberia.entity.AppointmentDetail;
import com.elcaballero.barberia.entity.AppointmentStatus;
import com.elcaballero.barberia.entity.Barber;
import com.elcaballero.barberia.entity.Role;
import com.elcaballero.barberia.entity.ServiceEntity;
import com.elcaballero.barberia.entity.User;
import com.elcaballero.barberia.repository.AppointmentDetailRepository;
import com.elcaballero.barberia.repository.AppointmentRepository;
import com.elcaballero.barberia.repository.AppointmentStatusRepository;
import com.elcaballero.barberia.repository.BarberRepository;
import com.elcaballero.barberia.repository.RoleRepository;
import com.elcaballero.barberia.repository.ServiceRepository;
import com.elcaballero.barberia.repository.UserRepository;

public final class EntityLookup {

    private EntityLookup() {
    }

    public static Role role(RoleRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Rol", id));
    }

    public static ServiceEntity service(ServiceRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Servicio", id));
    }

    public static AppointmentStatus status(AppointmentStatusRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Estado de cita", id));
    }

    public static User user(UserRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    public static Barber barber(BarberRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Barbero", id));
    }

    public static Barber barberForUpdate(BarberRepository repository, Long id) {
        return repository.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Barbero", id));
    }

    public static Appointment appointment(AppointmentRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cita", id));
    }

    public static AppointmentDetail detail(AppointmentDetailRepository repository, Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Detalle de cita", id));
    }
}
