package sat301.s_martproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sat301.s_martproject.model.Category;
import sat301.s_martproject.repository.CategoryRepo;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CategoryService {
    private static final String UPLOAD_DIR = "uploads/categories/";

    @Autowired
    private CategoryRepo categoryRepo;

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    public Map<Integer, Long> getProductCountForCategories() {
        List<Category> categories = categoryRepo.findAll();
        Map<Integer, Long> productCounts = new HashMap<>();
        for (Category category : categories) {
            productCounts.put(category.getCategory_id(), (long) category.getProducts().size());
        }
        return productCounts;
    }

    /**
     * Get category by ID
     */
    public Optional<Category> getCategoryById(int id) {
        return categoryRepo.findById(id);
    }

    /**
     * Add a new category
     */
    public boolean addCategory(String category_name, MultipartFile category_img) {
        String imageUrl = uploadImage(category_img);
        if (imageUrl == null) return false; // Upload failed
        
        Category category = new Category(category_name, imageUrl);
        categoryRepo.save(category);
        return true;
    }


    /**
     * Update existing category
     */
    public boolean updateCategory(int id, String category_name, MultipartFile category_img) {
        Optional<Category> existingCategory = categoryRepo.findById(id);
        if (existingCategory.isEmpty()) return false; // Category not found
    
        Category category = existingCategory.get();
        category.setCategory_name(category_name);
    
        // If a new image is uploaded, delete the old image and save the new one
        if (!category_img.isEmpty()) {
            String oldImageUrl = category.getCategory_img_url(); // Get the old image URL
            String newImageUrl = uploadImage(category_img); // Upload new image
    
            if (newImageUrl != null) {
                category.setCategory_img_url(newImageUrl); // Set new image URL
                deleteImageFromServer(oldImageUrl); // Delete old image
            }
        }
    
        categoryRepo.save(category);
        return true;
    }
    

    /**
     * Delete category by ID
     */
    public void deleteCategory(int id) {
        Optional<Category> categoryOpt = categoryRepo.findById(id);
        
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
    
            // Check if category has products; prevent deletion if true
            if (category.getProducts() != null && !category.getProducts().isEmpty()) {
                throw new IllegalStateException("Cannot delete category. It has associated products.");
            }
    
            // Delete the associated image from the server
            if (category.getCategory_img_url() != null) {
                deleteImageFromServer(category.getCategory_img_url());
            }
    
            // Delete the category from the database
            categoryRepo.deleteById(id);
        }
    }
    
    /**
     * Deletes an image file from the server.
     */
    private void deleteImageFromServer(String imageUrl) {
        try {
            // Remove "/uploads/categories/" from the path to get the file name
            String fileName = imageUrl.replace("/uploads/categories/", "");
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
    
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                System.out.println("Deleted image: " + filePath.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to delete image: " + imageUrl);
        }
    }
    
    

    /**
     * Upload image to server
     */
    private String uploadImage(MultipartFile file) {
        if (file.isEmpty()) return null;
    
        try {
            Path uploadPath = Paths.get("uploads/categories/");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
    
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());
    
            return "/uploads/categories/" + fileName; // Return relative URL
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
