package archive.oxahex.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "partners")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partners extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partners_id")
    private Long id;

    @Column(length = 100, unique = true)
    private String name;

    @Column(name = "business_number", length = 10, unique = true)
    private String businessNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 매장 정보 조회용 필드
}
