package es.brasatech.fastbite.jpa.tenant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenant_locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantLocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername;

    @Column(name = "tenant_id", unique = true, nullable = false)
    private String tenantId;

    @Column(name = "plan", nullable = false)
    private String plan;
}
