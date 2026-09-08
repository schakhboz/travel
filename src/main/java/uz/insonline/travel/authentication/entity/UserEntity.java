package uz.insonline.travel.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_USERS")
public class UserEntity implements UserDetails {

    @Id
    @Column(name = "TB_ID")
    private Long tbId;

    @Column(name = "TB_DIVISION")
    private Long tbDivision;

    @Column(name = "TB_LOGIN")
    private String tbLogin;

    @Column(name = "TB_PASS")
    private String tbPass;

    @Column(name = "TB_PASSFROM")
    private LocalDate tbPassfrom;

    @Column(name = "TB_PASSTO")
    private LocalDate tbPassto;

    @Column(name = "TB_ACTIVE")
    private Long tbActive;

    @Column(name = "AGN_ID")
    private Long agnId;

    @Column(name = "TOKEN")
    private String token;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.tbPass;
    }

    @Override
    public String getUsername() {
        return this.tbLogin;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.tbPassfrom.isBefore(LocalDate.now());
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.tbActive == 1;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.tbPassto.isAfter(LocalDate.now());
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
