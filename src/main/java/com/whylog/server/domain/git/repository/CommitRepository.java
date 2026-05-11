package com.whylog.server.domain.git.repository;

import com.whylog.server.domain.git.entity.Commit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CommitRepository extends JpaRepository<Commit, Long> {

    // 레포지토리 ID와 해시로 커밋 조회
    Optional<Commit> findByRepositoryIdAndHash(Long repositoryId, String hash);

    // 해시 조회
    @Query("SELECT c.hash FROM Commit c " +
            "WHERE c.repository.id = :repositoryId " +
            "AND c.hash IN :hashes")
    Set<String> findExistingHashes(
            @Param("repositoryId") Long repositoryId,
            @Param("hashes") List<String> hashes
    );

    // 커밋 ID 목록에 해당하는 커밋과 저장소 정보를 함께 조회
    @Query("""
            SELECT c
            FROM Commit c
            JOIN FETCH c.repository
            WHERE c.id IN :commitIds
            """)
    List<Commit> findAllWithRepositoryByIdIn(@Param("commitIds") List<Long> commitIds);

    @Query("""
            SELECT c.id
            FROM Commit c
            WHERE c.repository.id = :repositoryId
            """)
    List<Long> findIdsByRepositoryId(@Param("repositoryId") Long repositoryId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM Commit c
            WHERE c.repository.id = :repositoryId
            """)
    void deleteByRepositoryId(@Param("repositoryId") Long repositoryId);

    // 커서 기반 무한스크롤 - 커밋 목록 조회
    @Query("SELECT c FROM Commit c " +
            "WHERE c.repository.id = :repositoryId " +
            "AND (" +
            "  :cursorId IS NULL " +
            "  OR c.dateTime < (SELECT sub.dateTime FROM Commit sub WHERE sub.id = :cursorId) " +
            "  OR (c.dateTime = (SELECT sub2.dateTime FROM Commit sub2 WHERE sub2.id = :cursorId) AND c.id < :cursorId)" +
            ") " +
            "ORDER BY c.dateTime DESC, c.id DESC")
    Slice<Commit> findCommitsWithCursor(
            @Param("repositoryId") Long repositoryId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}

