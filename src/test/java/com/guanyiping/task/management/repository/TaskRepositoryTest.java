package com.guanyiping.task.management.repository;

import com.guanyiping.task.management.entity.Category;
import com.guanyiping.task.management.entity.Task;
import com.guanyiping.task.management.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TaskRepositoryTest {

    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private EntityManagerFactory entityManagerFactory;

    private Statistics stats;
    private User testOwner;

    @BeforeEach
    void setUp() {
        stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);

        testOwner = new User();
        testOwner.setEmail("n1test_" + System.nanoTime() + "@example.com");
        testOwner.setUsername("n1testuser");
        testOwner.setPassword("password");
        userRepository.save(testOwner);

        Category category = new Category();
        category.setName("N1TestCategory_" + System.nanoTime());
        categoryRepository.save(category);

        for (int i = 1; i <= 3; i++) {
            Task task = new Task();
            task.setTitle("N1Task " + i);
            task.setPriority("Low");
            task.setOwner(testOwner);
            task.setCategory(category);
            taskRepository.save(task);
        }

        entityManager.flush();
        entityManager.clear();
        stats.clear();
    }

    @Test
    void findAll_issuesSingleQuery_notNPlusOne() {
        taskRepository.findAll();

        // @EntityGraph により owner と category を JOIN で一括取得 → SQL は1本
        // N+1なら 1 + N(owner) + N(category) 本になる
        assertThat(stats.getPrepareStatementCount())
                .isEqualTo(1);
    }

    @Test
    void findByPriority_issuesSingleQuery_notNPlusOne() {
        taskRepository.findByPriority("Low");

        assertThat(stats.getPrepareStatementCount())
                .isEqualTo(1);
    }

    @Test
    void findAll_loadsOwnerAndCategoryInSingleQuery() {
        List<Task> tasks = taskRepository.findAll();

        // このテストで挿入したタスクのみ対象（既存データは owner が null の場合がある）
        List<Task> testTasks = tasks.stream()
                .filter(t -> t.getOwner() != null
                        && t.getOwner().getId().equals(testOwner.getId()))
                .toList();
        assertThat(testTasks).hasSize(3);

        long countBeforeAccess = stats.getPrepareStatementCount();

        // owner / category のフィールドアクセスで追加SQL が発行されないことを確認
        testTasks.forEach(t -> {
            assertThat(t.getOwner().getUsername()).isEqualTo("n1testuser");
            assertThat(t.getCategory().getName()).startsWith("N1TestCategory_");
        });

        assertThat(stats.getPrepareStatementCount())
                .as("関連エンティティアクセス時に追加SQLが発行されていないこと")
                .isEqualTo(countBeforeAccess);
    }
}