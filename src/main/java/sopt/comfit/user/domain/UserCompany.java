package sopt.comfit.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sopt.comfit.company.domain.Company;
import sopt.comfit.global.base.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_companies", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "company_id"}))
public class UserCompany extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "is_connected", nullable = false)
    private boolean isConnected;

    @Builder(access = AccessLevel.PRIVATE)
    private UserCompany(final User user,
                        final Company company,
                        final boolean isConnected) {
        this.user = user;
        this.company = company;
        this.isConnected = isConnected;
    }

    public static UserCompany create(final User user,
                                     final Company company,
                                     final boolean isConnected) {
        return UserCompany.builder()
                .user(user)
                .company(company)
                .isConnected(isConnected)
                .build();
    }
}
