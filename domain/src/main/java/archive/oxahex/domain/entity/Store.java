package archive.oxahex.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Entity
@Table(name = "store")
@DynamicInsert
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
