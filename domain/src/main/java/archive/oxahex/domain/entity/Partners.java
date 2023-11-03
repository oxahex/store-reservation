package archive.oxahex.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Partners extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "partners_id")
    private Long id;

    @Column(length = 100, unique = true)
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 매장 정보 조회용 필드
    @OneToMany(mappedBy = "partners", cascade = CascadeType.REMOVE)
    List<Store> stores = new ArrayList<>();

    // 파트너스 생성 시 유저에 해당 파트너스 저장(리스트)
    public void setUser(User user) {
        user.setPartners(this);
        this.user = user;
    }

    @Builder
    private Partners(String name) {
        this.name = name;
    }
}
