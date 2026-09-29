package es.brasatech.fastbite.security;

import es.brasatech.fastbite.application.tenant.TenantLocationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TenantAccessFilterTest {

    private final TenantLocationService tenantLocationService = mock(TenantLocationService.class);
    private final TenantAccessFilter filter = new TenantAccessFilter(tenantLocationService);

    private static Authentication login(String username, String homeTenantId, String... roles) {
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(roles);
        TenantUser user = new TenantUser(username, "hash", true, authorities, homeTenantId);
        return UsernamePasswordAuthenticationToken.authenticated(user, null, authorities);
    }

    private static List<String> roles(Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }

    @Test
    void staffKeepTheirRolesInTheirOwnRestaurant() {
        Authentication cashier = login("ana", "pizza", "ROLE_CASHIER");

        assertThat(filter.scope(cashier, "pizza")).isSameAs(cashier);
        assertThat(filter.scope(cashier, "PIZZA")).isSameAs(cashier);
    }

    @Test
    void staffAreAnonymousInOtherRestaurantsAndOnPlatformPages() {
        Authentication admin = login("ana", "pizza", "ROLE_ADMIN");

        assertThat(filter.scope(admin, "burger")).isNull();
        assertThat(filter.scope(admin, null)).isNull();
    }

    @Test
    void staffAccountsNeverActAsOwners() {
        Authentication fakeOwner = login("bob", "pizza", "ROLE_OWNER", "ROLE_ADMIN");

        assertThat(roles(filter.scope(fakeOwner, "pizza"))).containsExactly("ROLE_ADMIN");
    }

    @Test
    void ownersBecomeAdminInRestaurantsTheyOwn() {
        when(tenantLocationService.isOwnerOf("alice", "pizza")).thenReturn(true);
        Authentication owner = login("alice", null, "ROLE_OWNER");

        assertThat(roles(filter.scope(owner, "pizza"))).containsExactlyInAnyOrder("ROLE_OWNER", "ROLE_ADMIN");
    }

    @Test
    void ownersAreAnonymousInRestaurantsTheyDoNotOwn() {
        Authentication owner = login("alice", null, "ROLE_OWNER", "ROLE_ADMIN");

        assertThat(filter.scope(owner, "burger")).isNull();
    }

    @Test
    void platformAccountsKeepOnlyTheOwnerRoleOutsideRestaurants() {
        Authentication owner = login("alice", null, "ROLE_OWNER", "ROLE_ADMIN");

        assertThat(roles(filter.scope(owner, null))).containsExactly("ROLE_OWNER");
    }

    @Test
    void sessionsFromBeforeTenantScopedLoginsAreAnonymous() {
        Authentication legacy = UsernamePasswordAuthenticationToken.authenticated(
                "kebabowner", null, AuthorityUtils.createAuthorityList("ROLE_OWNER"));

        assertThat(filter.scope(legacy, "kebab")).isNull();
    }
}
