package com.readora.backend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CloudinaryUsageCheckRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryUsageCheckRunner.class);
    private final Cloudinary cloudinary;

    @Override
    public void run(String... args) {
        try {
            // Cloudinary Admin API မှ Usage Data များကို လှမ်းယူခြင်း
            Map<?, ?> usage = cloudinary.api().usage(ObjectUtils.emptyMap());

            // Credits အချက်အလက်များကို ထုတ်ယူခြင်း
            Map<?, ?> credits = (Map<?, ?>) usage.get("credits");

            if (credits != null) {
                Object usedPercent = credits.get("used_percent");
                Object limit = credits.get("limit");
                Object usageAmount = credits.get("usage");

                System.out.println("\n=======================================================");
                System.out.println(" ☁️  CLOUDINARY USAGE REPORT");
                System.out.println("=======================================================");
                System.out.println(" 📊 Used Percentage : " + usedPercent + " %");
                System.out.println(" 📈 Total Used      : " + usageAmount + " Credits");
                System.out.println(" 🎯 Total Limit     : " + limit + " Credits");
                System.out.println("=======================================================\n");
            }

        } catch (Exception e) {
            logger.warn("⚠️ Could not fetch Cloudinary usage report: {}", e.getMessage());
        }
    }
}
