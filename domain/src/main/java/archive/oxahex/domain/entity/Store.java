package archive.oxahex.domain.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "store")
@NoArgsConstructor
public class Store extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    private String name;
    private String address;
    private String description;

    @Column(name = "table_count")
    private Integer tableCount;
    private BigDecimal rating;

    @Column(name = "review_count")
    private Integer reviewCount;
}
