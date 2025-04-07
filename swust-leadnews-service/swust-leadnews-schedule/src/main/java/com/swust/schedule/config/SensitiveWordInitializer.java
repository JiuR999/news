package com.swust.schedule.config;

import com.swust.utils.common.SensitiveWordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class SensitiveWordInitializer {

    @Autowired
    private ResourceLoader resourceLoader;

    @PostConstruct
    public void init() {
        try {
            Resource resource = resourceLoader.getResource("classpath:sensitive-words.txt");
            Collection<String> words = readWordsFromFile(resource);
            SensitiveWordUtil.initMap(words);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Collection<String> readWordsFromFile(Resource resource) throws IOException {
        Collection<String> words = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }
        }
        return words;
    }
}
