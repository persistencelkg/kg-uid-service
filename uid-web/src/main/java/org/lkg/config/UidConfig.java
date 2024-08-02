package org.lkg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Description:
 * Author: 李开广
 * Date: 2024/8/2 3:31 PM
 */
@Configuration
@ImportResource(locations = {"classpath:uid/default-uid-spring.xml"})
public class UidConfig {

    public static void main(String[] args) {
        long num = 1L << 28;
        System.out.println(num / 1000.0/ 86400/365);
    }
}
