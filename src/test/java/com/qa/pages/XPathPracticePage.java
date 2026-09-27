package com.qa.pages;

import com.qa.framework.base.BasePage;
import com.qa.framework.config.ConfigManager;
import io.qameta.allure.Step;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static com.qa.framework.base.ConstantPages.*;
import static com.qa.framework.base.ConstantTest.KEY_APP_URL;

/**
 * Page object for https://selectorshub.com/xpath-practice-page/.
 * All locators and UI strings live in {@link com.qa.framework.base.ConstantPages}.
 */
public class XPathPracticePage extends BasePage {

    @Step("Open XPath practice page")
    public XPathPracticePage open() {
        open(ConfigManager.getRequired(KEY_APP_URL));
        visible(EMAIL_INPUT);
        return this;
    }

    // ================= Dummy form =================

    @Step("Fill dummy form with email '{0}'")
    public XPathPracticePage fillDummyForm(String userEmail, String userPassword, String companyName, String mobileNo) {
        type(EMAIL_INPUT, userEmail);
        type(PASSWORD_INPUT, userPassword);
        type(COMPANY_INPUT, companyName);
        type(MOBILE_INPUT, mobileNo);
        return this;
    }

    public String emailValue() {
        return value(EMAIL_INPUT);
    }

    public String passwordValue() {
        return value(PASSWORD_INPUT);
    }

    public String companyValue() {
        return value(COMPANY_INPUT);
    }

    public String mobileValue() {
        return value(MOBILE_INPUT);
    }

    public String passwordFieldType() {
        return visible(PASSWORD_INPUT).getDomAttribute(ATTR_TYPE);
    }

    @Step("Click Submit")
    public XPathPracticePage submitForm() {
        click(SUBMIT_BUTTON);
        return this;
    }

    public boolean isSubmitDisplayed() {
        return isDisplayed(SUBMIT_BUTTON);
    }

    @Step("Type '{0}' in the First Crush field")
    public XPathPracticePage typeFirstCrush(String name) {
        type(FIRST_CRUSH_INPUT, name);
        return this;
    }

    public String firstCrushValue() {
        return value(FIRST_CRUSH_INPUT);
    }

    // ================= User table =================

    public int userRowCount() {
        return allVisible(USER_TABLE_ROWS).size();
    }

    @Step("Select checkbox for user '{0}'")
    public XPathPracticePage selectUser(String username) {
        click(userCheckbox(username));
        return this;
    }

    public boolean isUserSelected(String username) {
        return visible(userCheckbox(username)).isSelected();
    }

    /** Column offset after Username: use COL_USER_ROLE, COL_EMPLOYEE_NAME or COL_STATUS. */
    public String userCell(String username, int columnAfterUsername) {
        return text(By.xpath(String.format(USER_CELL_XPATH, username, columnAfterUsername)));
    }

    private By userCheckbox(String username) {
        return By.xpath(String.format(USER_CHECKBOX_XPATH, username));
    }

    // ================= Dropdown =================

    @Step("Select car '{0}'")
    public XPathPracticePage selectCar(String visibleText) {
        selectByVisibleText(CARS_DROPDOWN, visibleText);
        return this;
    }

    public String selectedCar() {
        return selectedOption(CARS_DROPDOWN);
    }

    // ================= Alerts =================

    @Step("Open confirm alert (accept = {0})")
    public String handleConfirmAlert(boolean accept) {
        click(WINDOW_ALERT_BUTTON);
        Alert alert = waitForAlert();
        String msg = alert.getText();
        if (accept) {
            alert.accept();
        } else {
            alert.dismiss();
        }
        return msg;
    }

    @Step("Open prompt alert and answer '{0}'")
    public String handlePromptAlert(String answer) {
        click(PROMPT_ALERT_BUTTON);
        Alert alert = waitForAlert();
        String msg = alert.getText();
        alert.sendKeys(answer);
        alert.accept();
        return msg;
    }

    public boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    // ================= Modal =================

    @Step("Open modal")
    public XPathPracticePage openModal() {
        click(OPEN_MODAL_BUTTON);
        visible(MODAL);
        return this;
    }

    public String modalBodyText() {
        return text(MODAL_BODY);
    }

    @Step("Close modal")
    public boolean closeModal() {
        click(MODAL_CLOSE);
        return waitForInvisibility(MODAL);
    }

    // ================= Shadow DOM =================

    @Step("Type '{0}' in shadow DOM username field")
    public XPathPracticePage typeInShadowUsername(String value) {
        typeInto(shadowElement(SHADOW_HOST, SHADOW_USERNAME_CSS), value);
        return this;
    }

    public String shadowUsernameValue() {
        return shadowElement(SHADOW_HOST, SHADOW_USERNAME_CSS).getDomProperty(ATTR_VALUE);
    }

    @Step("Type '{0}' in nested shadow DOM pizza field")
    public XPathPracticePage typeInNestedShadowPizza(String value) {
        typeInto(shadowElement(SHADOW_HOST, SHADOW_NESTED_HOST_CSS, SHADOW_PIZZA_CSS), value);
        return this;
    }

    public String nestedShadowPizzaValue() {
        return shadowElement(SHADOW_HOST, SHADOW_NESTED_HOST_CSS, SHADOW_PIZZA_CSS).getDomProperty(ATTR_VALUE);
    }

    private void typeInto(WebElement el, String value) {
        el.clear();
        el.sendKeys(value);
    }

    // ================= DataTable =================

    @Step("Search data table for '{0}'")
    public XPathPracticePage searchDataTable(String term) {
        waitForDataTable();
        String before = text(DATATABLE_INFO);
        type(DATATABLE_SEARCH, term);
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(DATATABLE_INFO, before)));
        return this;
    }

    @Step("Show {0} rows per page")
    public XPathPracticePage setDataTablePageLength(String length) {
        waitForDataTable();
        String before = text(DATATABLE_INFO);
        selectByValue(DATATABLE_PAGE_LENGTH, length);
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(DATATABLE_INFO, before)));
        return this;
    }

    public List<String> dataTableRows() {
        waitForDataTable();
        return driver.findElements(DATATABLE_ROWS).stream()
                .map(r -> r.getText().replaceAll(WHITESPACE_REGEX, " ").trim())
                .toList();
    }

    public String dataTableInfo() {
        return text(DATATABLE_INFO);
    }

    /**
     * DataTables builds the paging/info widgets in a jQuery "ready" handler that can run after
     * driver.get() returns (EAGER page load). The info element only exists once it has run.
     */
    private void waitForDataTable() {
        visible(DATATABLE_INFO);
    }

    // ================= Payment form =================

    @Step("Enter card number '{0}'")
    public XPathPracticePage enterCardNumber(String number) {
        type(CARD_NUMBER_INPUT, number);
        return this;
    }

    @Step("Enter expiry '{0}'")
    public XPathPracticePage enterExpiry(String mmYY) {
        type(EXPIRY_INPUT, mmYY);
        return this;
    }

    @Step("Enter name on card '{0}'")
    public XPathPracticePage enterCardName(String name) {
        type(CARD_NAME_INPUT, name);
        return this;
    }

    @Step("Enter CVV")
    public XPathPracticePage enterCvv(String value) {
        type(CVV_INPUT, value);
        return this;
    }

    public String cardNumberValue() {
        return value(CARD_NUMBER_INPUT);
    }

    public String expiryValue() {
        return value(EXPIRY_INPUT);
    }

    public boolean isCardNumberErrorShown() {
        return isDisplayed(CARD_NUMBER_ERROR);
    }

    /** @param fieldId one of ConstantPages.ID_CARD_NAME / ID_CARD_NUMBER / ID_EXPIRY / ID_CVV */
    public boolean isMarkedInvalid(String fieldId) {
        String cls = visible(By.id(fieldId)).getDomAttribute(ATTR_CLASS);
        return cls != null && cls.contains(CSS_CLASS_INVALID);
    }
}
