/*
 * MIT License
 *
 * Copyright (c) 2018 Elias Nogueira
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package driver.local;

import static java.lang.Boolean.TRUE;

import config.Configuration;
import config.ConfigurationManager;
import driver.IDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

@Log4j2
public class LocalDriverManager implements IDriver {

    @Override
    public WebDriver createInstance(String browser) {
        WebDriver driverInstance = null;

        try {
            DriverManagerType driverManagerType = DriverManagerType.valueOf(browser.toUpperCase());
            Class<?> driverClass = Class.forName(driverManagerType.browserClass());
            WebDriverManager.getInstance(driverManagerType).setup();
            Configuration configuration = ConfigurationManager.getConfiguration();

            if (TRUE.equals(configuration.headless())) {
                driverInstance = defineHeadless(driverClass);
            } else {
                driverInstance = (WebDriver) driverClass.getDeclaredConstructor().newInstance();
            }

        } catch (IllegalAccessException | ClassNotFoundException e) {
            log.error("The class could not be found", e);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            log.error("Problem during driver instantiation", e);
        }
        return driverInstance;
    }

    private WebDriver defineHeadless(Class<?> driverClass)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        WebDriver driver;
        Constructor<?> constructor;

        if (ChromeDriver.class == driverClass) {
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.setHeadless(true);
            constructor = driverClass.getDeclaredConstructor(ChromeOptions.class);
            AccessibleObject.setAccessible(new AccessibleObject[]{constructor}, true);
            driver = (WebDriver) constructor.newInstance(chromeOptions);
        } else if (FirefoxDriver.class == driverClass) {
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setHeadless(true);
            constructor = driverClass.getDeclaredConstructor(FirefoxOptions.class);
            AccessibleObject.setAccessible(new AccessibleObject[]{constructor}, true);
            driver = (WebDriver) constructor.newInstance(firefoxOptions);
        } else {
            throw new IllegalArgumentException("Headless is only supported by Google Chrome or Firefox");
        }

        return driver;
    }
}
