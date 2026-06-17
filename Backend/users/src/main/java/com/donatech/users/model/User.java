package com.donatech.users.model;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
@Data // Lombok genera getters, setters, equals, hashCode y toString automáticamente
@NoArgsConstructor // Constructor vacío por defecto (necesario para JPA)
@AllArgsConstructor // Constructor con todos los parámetros, útil si lo necesitas para la creación
                    // rápida de instancias
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+$", message = "'name' solo puede contener letras, sin espacios")
    @NotEmpty(message = "'name' no puede estar vacío")
    @Size(min = 2, max = 100, message = "'name' debe tener entre 2 y 100 caracteres")
    private String name;

    // Apellido: nullable para no romper filas existentes; la obligatoriedad se valida en los DTOs de entrada.
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ]+$", message = "'apellido' solo puede contener letras, sin espacios")
    @Size(min = 2, max = 100, message = "'apellido' debe tener entre 2 y 100 caracteres")
    @Column(name = "apellido")
    private String apellido;

    @Email(message = "'email' debe ser válido")
    @NotEmpty(message = "'email' no puede estar vacío")
    private String email;

    @NotEmpty(message = "'password' no puede estar vacía")
    @Size(min = 6, message = "'password' debe tener al menos 6 caracteres")
    private String password;

    @ManyToOne(fetch = FetchType.EAGER) // EAGER para que cargue el rol junto con el usuario
    @JoinColumn(name = "id_role", nullable = false)
    @NotNull(message = "'role' no puede ser nulo")
    private Role role;

    /*
     * @NotNull(message = "'role' no puede ser nulo")
     * 
     * @Min(value = 0, message = "'role' debe ser mayor o igual a 0")
     * private Integer role;
     */

    @NotNull(message = "'status' no puede ser nulo")
    @Min(value = 0, message = "El 'status' debe ser 1 o 0")
    @Max(value = 1, message = "El 'status' debe ser 1 o 0")
    private Integer status;

    // Imagen de perfil legacy (BLOB — no usar en nuevos flujos)
    @Lob
    @Column(name = "imagen", nullable = true)
    private byte[] imagen;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    // Phone number (nullable)
    @Column(name = "phone", nullable = true)
    private String phone;

    // Region (nullable)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_region", nullable = true)
    private Region region;

    // Comuna (nullable)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_comuna", nullable = true)
    private Comuna comuna;

    // 🔹 Constructor principal sin los campos opcionales
    public User(String name, String email, String password, Role role, Integer status) {
        this(name, email, password, role, status, null, null, null, null);
    }

    // 🔹 Constructor completo con los campos opcionales
    public User(String name, String email, String password, Role role, Integer status, byte[] imagen,
            String phone, Region region, Comuna comuna) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.imagen = imagen;
        this.phone = phone;
        this.region = region;
        this.comuna = comuna;
    }
}
