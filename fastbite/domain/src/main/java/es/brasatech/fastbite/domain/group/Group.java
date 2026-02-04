package es.brasatech.fastbite.domain.group;

import java.util.List;

public record Group(
                String id,
                String name,
                String description,
                String icon,
                List<String> products // List of product IDs
) {
}
