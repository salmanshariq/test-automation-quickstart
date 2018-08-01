package com.opencredo.test;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.events.EventFiringWebDriver;

/**
 * Creates a browser session which can be used to interact with a web UI.
 * This class allows for a single session to be re-used across multiple
 * test cases for increased speed.
 */

public class SharedDriver extends EventFiringWebDriver {
    private static WebDriver REAL_DRIVER;
    private static final Thread CLOSE_THREAD = new Thread(SharedDriver::quitGlobalInstance);

    public SharedDriver(String browser, boolean isProxyEnabled, Proxy proxy) {
        super(getRealDriver(browser, isProxyEnabled, proxy));
        Runtime.getRuntime().addShutdownHook(CLOSE_THREAD);
    }


    private static void quitGlobalInstance() {
        WebDriver driver = REAL_DRIVER;
        REAL_DRIVER = null;
        if (driver != null) {
            driver.quit();
        }
    }

    private static WebDriver getRealDriver(String browser, boolean isProxyEnabled, Proxy proxy) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        if (isProxyEnabled && proxy != null)
            capabilities.setCapability(CapabilityType.PROXY, proxy);

        if (REAL_DRIVER == null) {
            switch (browser) {
                case BrowserType.FIREFOX:
                    REAL_DRIVER = new FirefoxDriver(capabilities);
                    break;
                case BrowserType.GOOGLECHROME:
                    REAL_DRIVER = new ChromeDriver(capabilities);
                    break;
                default:
                    throw new RuntimeException("Framework does not support browser \"" + browser + "\"");
            }
        }
        return REAL_DRIVER;
    }

    @Override
    public void close() {
        if (Thread.currentThread() != CLOSE_THREAD) {
            throw new UnsupportedOperationException("You shouldn't close this WebDriver. It's shared and will close when the JVM exits.");
        }
        try {
            super.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
