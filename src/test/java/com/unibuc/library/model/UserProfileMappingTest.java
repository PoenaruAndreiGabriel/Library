package com.unibuc.library.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileMappingTest {

    @Test
    void userHasOneToOneProfileMapping() throws Exception {
        Field profileField = User.class.getDeclaredField("profile");

        assertTrue(profileField.isAnnotationPresent(OneToOne.class));
        assertTrue(profileField.isAnnotationPresent(JoinColumn.class));

        OneToOne oneToOne = profileField.getAnnotation(OneToOne.class);
        JoinColumn joinColumn = profileField.getAnnotation(JoinColumn.class);

        assertTrue(java.util.Arrays.asList(oneToOne.cascade()).contains(CascadeType.ALL));
        assertEquals("profile_id", joinColumn.name());
        assertTrue(joinColumn.unique());
    }

    @Test
    void userProfileHasMappedByUserRelationship() throws Exception {
        Field userField = UserProfile.class.getDeclaredField("user");

        assertTrue(userField.isAnnotationPresent(OneToOne.class));
        OneToOne oneToOne = userField.getAnnotation(OneToOne.class);
        assertEquals("profile", oneToOne.mappedBy());
    }

    @Test
    void userAndProfileCanBeLinkedInMemory() {
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setRole(UserRole.MEMBER);
        user.setMaxBorrowLimit(5);

        UserProfile profile = new UserProfile();
        profile.setPhoneNumber("0712345678");
        profile.setAddress("Bucharest");

        user.setProfile(profile);
        profile.setUser(user);

        assertSame(profile, user.getProfile());
        assertSame(user, profile.getUser());
    }
}


