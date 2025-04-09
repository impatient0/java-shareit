package ru.practicum.shareit.item;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@SuppressWarnings("unused")
public interface ItemJpaRepository extends ItemRepository, JpaRepository<Item, Long> {

    @Override
    @Query("SELECT i FROM Item i " +
        "WHERE (LOWER(i.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
        "LOWER(i.description) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
        "AND i.status = 'AVAILABLE'")
    List<Item> search(@Param("searchText") String text);
}
