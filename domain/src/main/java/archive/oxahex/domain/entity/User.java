package archive.oxahex.domain.entity;

import archive.oxahex.domain.type.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @OneToOne(mappedBy = "user")
    private Partners partners;

    @OneToMany(mappedBy = "user")
    private List<Reservation> reservations = new ArrayList<>();

    public void setRole(RoleType role) {
        this.role = role;
    }

    public void setPartners(Partners partners) {
        this.partners = partners;
    }


    @Builder
    private User(String name, String email, String password, String phoneNumber, RoleType role, LocalDateTime registeredDate) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.registeredDate = registeredDate;
    }

}
