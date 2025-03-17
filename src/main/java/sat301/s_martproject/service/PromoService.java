package sat301.s_martproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sat301.s_martproject.model.Promo;
import sat301.s_martproject.repository.PromoRepo;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Service
public class PromoService {
    private static final String UPLOAD_DIR = "uploads/promotions/";

    @Autowired
    private PromoRepo promoRepo;

    /**
     * ✅ Get All Promos
     */
    public List<Promo> getAllPromos() {
        return promoRepo.findAll();
    }

    /**
     * ✅ Get Promo By ID
     */
    public Optional<Promo> getPromoById(int id) {
        return promoRepo.findById(id);
    }

    /**
     * ✅ Add New Promo
     */
    public boolean addPromo(String promo_code, double discount, boolean display, boolean active, MultipartFile promo_img) {
        String imageUrl = uploadImage(promo_img);
        if (imageUrl == null) return false;

        Promo promo = new Promo(promo_code, discount, display, active, imageUrl);
        promoRepo.save(promo);
        return true;
    }

    /**
     * ✅ Update Existing Promo
     */
    public boolean updatePromo(int id, String promo_code, double discount, boolean display, boolean active, MultipartFile promo_img) {
        Optional<Promo> existingPromo = promoRepo.findById(id);
        if (existingPromo.isEmpty()) return false;

        Promo promo = existingPromo.get();
        promo.setPromo_code(promo_code);
        promo.setDiscount(discount);
        promo.setDisplay(display);
        promo.setActive(active);

        // If new image is provided, update it
        if (!promo_img.isEmpty()) {
            String imageUrl = uploadImage(promo_img);
            if (imageUrl != null) {
                deleteImage(promo.getPromo_img_url()); // Delete old image
                promo.setPromo_img_url(imageUrl);
            }
        }

        promoRepo.save(promo);
        return true;
    }

    /**
     * ✅ Toggle Active Status of Promo
     */
    public void toggleActive(int id) {
        Optional<Promo> promoOpt = promoRepo.findById(id);
        if (promoOpt.isPresent()) {
            Promo promo = promoOpt.get();
            promo.setActive(!promo.isActive()); // Toggle active status
            promoRepo.save(promo);
        }
    }

    /**
     * ✅ Delete Promo
     */
    public void deletePromo(int id) {
        Optional<Promo> promoOpt = promoRepo.findById(id);
        if (promoOpt.isPresent()) {
            Promo promo = promoOpt.get();
            deleteImage(promo.getPromo_img_url()); // Delete promo image
            promoRepo.delete(promo);
        }
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

            return "/uploads/promotions/" + fileName; // Return relative URL
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ Delete Image from Server
     */
    public boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return false;

        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(imagePath.replace("/uploads/promotions/", "")).normalize();

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
