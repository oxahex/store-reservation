package archive.oxahex.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

@Entity
@Table(name = "store")
@DynamicInsert
@Getter
@Setter
@NoArgsConstructor
public class Store extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    private String name;
    private String address;
    private String description;

    @Column(name = "business_number", length = 10, unique = true)
    private String businessNumber;

    @Column(name = "table_count")
    private Integer tableCount;

    @Column(columnDefinition = "decimal(2,1) default '0.0'")
    private BigDecimal rating;

    @Column(name = "review_count", columnDefinition = "int default 0")
    private Integer reviewCount;

    @Column(name = "registered_date")
    private LocalDateTime registeredDate;

    @ManyToOne
    @JoinColumn(name = "partners_id")
    private Partners partners;

    // 매장 저장 시 partners.stores에도 매장 객체 저장
    public void setPartners(Partners partners) {
        this.partners = partners;
        partners.getStores().add(this);
    }

    // 예약 생성 시 테이블 수 감소 처리
    public void removeTableCount(int useTableCount) {
        this.tableCount -= useTableCount;
    }

}
