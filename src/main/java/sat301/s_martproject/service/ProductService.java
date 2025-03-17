package sat301.s_martproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sat301.s_martproject.model.Product;
import sat301.s_martproject.model.ProductImage;
import sat301.s_martproject.repository.ProductImageRepo;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class ProductService {
    private static final String UPLOAD_DIR = "uploads/products/";

    @Autowired
    private ProductImageRepo productImageRepo;
    public boolean validateProduct(Product product) {
        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Price cannot be negative or 0.");
        }
        if (product.getReview() < 0 || product.getReview() > 5) {
            throw new IllegalArgumentException("Review must be between 0 and 5.");
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        return true;
    }

    /**
     * ✅ Upload Image
     */
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) return null;

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            return "/uploads/products/" + fileName; // Return relative URL
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ Save Product Images (Ensure One Main Image)
     */
    public void saveProductImages(Product product, MultipartFile[] images) {
        boolean mainImageSet = false;

        for (int i = 0; i < images.length; i++) {
            MultipartFile file = images[i];
            if (!file.isEmpty()) {
                String imageUrl = uploadImage(file);
                if (imageUrl != null) {
                    ProductImage productImage = new ProductImage();
                    productImage.setProduct_img_url(imageUrl);
                    productImage.setProduct(product);
                    productImage.setIs_img_main(!mainImageSet); // First image is main
                    mainImageSet = true;
                    productImageRepo.save(productImage);
                }
            }
        }
    }
    public void updateMainImage(Product product, int mainImageId) {
        List<ProductImage> images = productImageRepo.findByProduct(product);
        
        for (ProductImage image : images) {
            image.setIs_img_main(image.getImage_id() == mainImageId);
            productImageRepo.save(image);
        }
    }
    
    // Delete records from db while deleting the image from server
    public void deleteProductImage(int imageId) {
        ProductImage image = productImageRepo.findById(imageId).orElse(null);
        if (image != null) {
            deleteImage(image.getProduct_img_url());
            productImageRepo.delete(image);
        }
    }

    // Function to delete image from server (not db)
    public boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return false;

        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(imagePath.replace("/uploads/products/", "")).normalize();

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true; 
            } else {
                System.out.println("⚠️ Image file not found: " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; 
    }
}
