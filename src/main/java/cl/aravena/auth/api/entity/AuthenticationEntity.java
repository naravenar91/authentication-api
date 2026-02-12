package cl.aravena.auth.api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("authentication")
public class AuthenticationEntity implements Persistable<String> {

    @Id
    @Column("uuid")
    private String uuid;

    @Column("user_id")
    private String userId;

    @Column("password")
    private String password;

    @Column("is_active")
    private Boolean isActive;

    @Transient
    private boolean isNew = false;

    @Override
    public String getId() {
        return uuid;
    }

    @Override
    public boolean isNew() {
        return isNew || uuid == null;
    }
}
