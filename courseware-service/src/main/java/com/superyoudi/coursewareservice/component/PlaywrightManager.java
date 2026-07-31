package com.superyoudi.coursewareservice.component;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PlaywrightManager {

    private final Playwright playwright;
    private final Browser browser;

    public PlaywrightManager() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();

        log.info("PlaywrightManager 初始化完毕。");
    }

    /**
     * 创建浏览器上下文
     */
    public BrowserContext createContext() {
        return browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 720));
    }

    /**
     * 创建页面
     */
    public Page createPage(BrowserContext context) {
        return context.newPage();
    }

    /**
     * 创建页面
     */
    @Bean
    public Page createPage() {
        return createPage(createContext());
    }

    @PreDestroy
    public void destroy() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
        log.info("Playwright 已终止。");
    }
}
