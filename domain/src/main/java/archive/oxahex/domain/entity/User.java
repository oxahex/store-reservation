package archive.oxahex.domain.entity;

import archive.oxahex.domain.type.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 50)
    private String name;
    private String password;

    @Column(name = "phone_number", length = 11)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @Column(name = "registered_date")
    private LocalDateTime registeredDate;

    @OneToMany(mappedBy = "user")
    private List<Partners> partners = new ArrayList<>();

    public void setRole(RoleType role) {
        this.role = role;
    }
}
