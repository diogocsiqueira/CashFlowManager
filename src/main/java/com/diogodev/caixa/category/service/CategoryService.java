package com.diogodev.caixa.category.service;

import com.diogodev.caixa.category.domain.model.Category;
import com.diogodev.caixa.category.dto.CategoryCreateRequest;
import com.diogodev.caixa.category.dto.CategoryResponse;
import com.diogodev.caixa.category.dto.CategoryUpdateRequest;
import com.diogodev.caixa.category.repository.CategoryRepository;
import com.diogodev.caixa.core.user.domain.model.User;
import com.diogodev.caixa.core.user.repository.UserRepository;
import com.diogodev.caixa.shared.security.SecurityUtils;
import com.diogodev.caixa.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private static final String DEFAULT_CATEGORY_NAME = "Sem categoria";

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    private Long uid() {
        return SecurityUtils.currentUserId();
    }

    private User currentUser() {
        return userRepository.findById(uid())
                .orElseThrow(() -> new IllegalStateException("Usuário do token não existe no banco"));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.isDefault()
        );
    }

    private Category findOwnedCategory(Long categoryId) {
        return categoryRepository.findByIdAndUser_Id(categoryId, uid())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAllByUser_IdOrderByNameAsc(uid())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return toResponse(findOwnedCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        User user = currentUser();
        String normalizedName = normalizeName(request.name());

        if (categoryRepository.existsByUser_IdAndNameIgnoreCase(user.getId(), normalizedName)) {
            throw new IllegalArgumentException("Já existe uma categoria com esse nome");
        }

        Category category = Category.builder()
                .name(normalizedName)
                .isDefault(false)
                .user(user)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        Category category = findOwnedCategory(id);
        String normalizedName = normalizeName(request.name());

        if (category.isDefault()) {
            throw new IllegalArgumentException("A categoria padrão não pode ser renomeada");
        }

        if (categoryRepository.existsByUser_IdAndNameIgnoreCaseAndIdNot(uid(), normalizedName, id)) {
            throw new IllegalArgumentException("Já existe uma categoria com esse nome");
        }

        category.setName(normalizedName);

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = findOwnedCategory(id);

        if (category.isDefault()) {
            throw new IllegalArgumentException("A categoria padrão não pode ser excluída");
        }

        boolean inUse = transactionRepository.existsByUser_IdAndCategory_Id(uid(), id);
        if (inUse) {
            throw new IllegalArgumentException("Não é possível excluir uma categoria em uso");
        }

        categoryRepository.delete(category);
    }

    @Transactional
    public Category ensureDefaultCategory(User user) {
        return categoryRepository.findByUser_IdAndIsDefaultTrue(user.getId())
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .name(DEFAULT_CATEGORY_NAME)
                                .isDefault(true)
                                .user(user)
                                .build()
                ));
    }

    @Transactional(readOnly = true)
    public Category resolveCategoryForCurrentUser(Long categoryId) {
        User user = currentUser();

        if (categoryId == null) {
            return ensureDefaultCategory(user);
        }

        return categoryRepository.findByIdAndUser_Id(categoryId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria informada não encontrada"));
    }

    private String normalizeName(String name) {
        String normalized = name == null ? "" : name.trim();

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }

        return normalized;
    }
}