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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "review_count", columnDefinition = "int default 0")
    private Integer reviewCount;

    @Column(name = "registered_date")
    private LocalDateTime registeredDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partners_id")
    private Partners partners;

    // 매장 저장 시 partners.stores에도 매장 객체 저장
    public void setPartners(Partners partners) {
        this.partners = partners;
        partners.getStores().add(this);
    }

    // 예약 승인 시 테이블 수 감소 처리
    public void removeTableCount(int useTableCount) {
        this.tableCount -= useTableCount;
    }

    // 예약 취소 시 테이블 수 증가 처리
    public void addTableCount(int useTableCount) {
        this.tableCount += useTableCount;
    }

    // 리뷰 개수 증가
    public void increaseReviewCount() {
        this.reviewCount++;
    }

    @Builder
    private Store(
            String name,
            String address,
            String description,
            String businessNumber,
            Integer tableCount,
            Integer reviewCount,
            LocalDateTime registeredDate
    ) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.businessNumber = businessNumber;
        this.tableCount = tableCount;
        this.reviewCount = reviewCount;
        this.registeredDate = registeredDate;
    }
}
