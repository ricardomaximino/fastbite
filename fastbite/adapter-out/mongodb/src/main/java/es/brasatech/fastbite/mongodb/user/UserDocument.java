package es.brasatech.fastbite.mongodb.user;

import es.brasatech.fastbite.domain.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private String fullName;

    private Set<Role> roles;

    private boolean active;
}
