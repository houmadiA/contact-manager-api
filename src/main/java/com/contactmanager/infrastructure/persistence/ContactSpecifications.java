package com.contactmanager.infrastructure.persistence;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

final class ContactSpecifications {

    private static final char ESCAPE = '\\';

    private ContactSpecifications() {}

    static Specification<ContactEntity> matching(String search) {
        return (root, query, builder) -> {
            if (search == null || search.isBlank()) {
                return builder.conjunction();
            }

            String pattern = "%" + escapeLikeWildcards(search.trim().toLowerCase(Locale.ROOT)) + "%";
            return builder.or(
                    like(builder, root.get("firstName"), pattern),
                    like(builder, root.get("lastName"), pattern),
                    like(builder, root.get("email"), pattern),
                    like(builder, root.get("company"), pattern));
        };
    }

    private static jakarta.persistence.criteria.Predicate like(
            CriteriaBuilder builder, Expression<String> column, String pattern) {
        return builder.like(builder.lower(column), pattern, ESCAPE);
    }

    private static String escapeLikeWildcards(String term) {
        return term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
