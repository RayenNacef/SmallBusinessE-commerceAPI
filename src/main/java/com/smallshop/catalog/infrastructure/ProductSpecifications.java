package com.smallshop.catalog.infrastructure;

import com.smallshop.catalog.application.ProductSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

final class ProductSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    private ProductSpecifications() {
    }

    static Specification<ProductJpaEntity> matching(ProductSearchCriteria criteria) {
        List<Specification<ProductJpaEntity>> conditions = new ArrayList<>();
        if (criteria.activeOnly()) {
            conditions.add(isActive());
        }
        criteria.categoryId().ifPresent(categoryId -> conditions.add(inCategory(categoryId.value())));
        criteria.nameContains().ifPresent(text -> conditions.add(nameContains(text)));
        return Specification.allOf(conditions);
    }

    private static Specification<ProductJpaEntity> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    private static Specification<ProductJpaEntity> inCategory(UUID categoryId) {
        return (root, query, cb) -> cb.equal(root.get("categoryId"), categoryId);
    }

    private static Specification<ProductJpaEntity> nameContains(String text) {
        String pattern = "%" + escapeLikeWildcards(text.toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern, LIKE_ESCAPE);
    }

    // Without escaping, a search for "100%" would match every name starting with "100".
    private static String escapeLikeWildcards(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
