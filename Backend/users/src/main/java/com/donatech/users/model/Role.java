package com.donatech.users.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "role")
@Data  // Lombok genera getters, setters, equals, hashCode y toString automáticamente
@NoArgsConstructor  // Constructor vacío por defecto (necesario para JPA)
@AllArgsConstructor  // Constructor con todos los parámetros, útil si lo necesitas para la creación rápida de instancias
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "'name' no puede estar vacío")
    @Size(min = 2, max = 100, message = "'name' debe tener entre 2 y 100 caracteres")
    private String name;

    public Role(String name) {
        this.name = name;
    }

}
