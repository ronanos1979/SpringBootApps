package com.ronanos.lexiconlair.userbook.persistence;

import com.ronanos.lexiconlair.userbook.domain.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    @Query("SELECT ub.bookId FROM UserBook ub WHERE ub.userId = :userId")
    List<Long> findBookIdsByUserId(@Param("userId") Long userId);
}
