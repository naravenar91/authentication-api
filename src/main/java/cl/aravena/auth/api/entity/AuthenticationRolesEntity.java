package cl.aravena.auth.api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("authentication_roles")
public class AuthenticationRolesEntity implements Persistable<String> {

    @Column("auth_uuid")
    private String authUuid;

    @Column("role_id")
    private Integer roleId;

    @Transient
    @Builder.Default
    private boolean isNewEntry = true;

    @Override
    public String getId() {
        // En tablas intermedias, puedes devolver una combinación o null
        return authUuid + "-" + roleId;
    }

    @Override
    public boolean isNew() {
        return isNewEntry;
    }
}