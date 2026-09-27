package com.qa.pages;

import com.qa.framework.base.BasePage;
import com.qa.framework.config.ConfigManager;
import io.qameta.allure.Step;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.qa.framework.base.ConstantPages.*;
import static com.qa.framework.base.ConstantTest.KEY_APP_URL;

/**
 * Page object for https://selectorshub.com/xpath-practice-page/ using <b>PageFactory</b>.
 *
 * <p>Elements are {@code @FindBy} fields; their locator strings live in
 * {@link com.qa.framework.base.ConstantPages}. BasePage initialises them with an
 * AjaxElementLocatorFactory (lazy lookup + wait). Two things cannot be annotations and stay as
 * {@code By}/CSS: per-user table locators built at runtime from templates, and elements inside
 * shadow roots.
 */
public class XPathPracticePage extends BasePage {

    // ---- Dummy form ----
    @FindBy(css = EMAIL_INPUT_CSS)
    private WebElement emailInput;

    @FindBy(id = PASSWORD_INPUT_ID)
    private WebElement passwordInput;

    @FindBy(css = COMPANY_INPUT_CSS)
    private WebElement companyInput;

    @FindBy(css = MOBILE_INPUT_CSS)
    private WebElement mobileInput;

    @FindBy(xpath = SUBMIT_BUTTON_XPATH)
    private WebElement submitButton;

    @FindBy(id = FIRST_CRUSH_INPUT_ID)
    private WebElement firstCrushInput;

    // ---- User table ----
    @FindBy(xpath = USER_TABLE_ROWS_XPATH)
    private List<WebElement> userTableRows;

    // ---- Dropdown ----
    @FindBy(id = CARS_DROPDOWN_ID)
    private WebElement carsDropdown;

    // ---- Alerts & modal ----
    @FindBy(xpath = WINDOW_ALERT_BUTTON_XPATH)
    private WebElement windowAlertButton;

    @FindBy(xpath = PROMPT_ALERT_BUTTON_XPATH)
    private WebElement promptAlertButton;

    @FindBy(id = OPEN_MODAL_BUTTON_ID)
    private WebElement openModalButton;

    @FindBy(id = MODAL_ID)
    private WebElement modal;

    @FindBy(css = MODAL_BODY_CSS)
    private WebElement modalBody;

    @FindBy(css = MODAL_CLOSE_CSS)
    private WebElement modalClose;

    // ---- Shadow DOM host (the elements inside it are reached via getShadowRoot) ----
    @FindBy(id = SHADOW_HOST_ID)
    private WebElement shadowHost;

    // ---- DataTable ----
    @FindBy(id = DATATABLE_SEARCH_ID)
    private WebElement dataTableSearch;

    @FindBy(css = DATATABLE_ROWS_CSS)
    private List<WebElement> dataTableRowElements;

    @FindBy(id = DATATABLE_INFO_ID)
    private WebElement dataTableInfoText;

    @FindBy(id = DATATABLE_PAGE_LENGTH_ID)
    private WebElement dataTablePageLength;

    // ---- Payment form ----
    @FindBy(id = ID_CARD_NAME)
    private WebElement cardNameInput;

    @FindBy(id = ID_CARD_NUMBER)
    private WebElement cardNumberInput;

    @FindBy(id = ID_EXPIRY)
    private WebElement expiryInput;

    @FindBy(id = ID_CVV)
    private WebElement cvvInput;

    @FindBy(id = CARD_NUMBER_ERROR_ID)
    private WebElement cardNumberError;

    // =====================================================================================

    @Step("Open XPath practice page")
    public XPathPracticePage open() {
        open(ConfigManager.getRequired(KEY_APP_URL));
        visible(emailInput);
        return this;
    }

    // ================= Dummy form =================

    @Step("Fill dummy form with email '{0}'")
    public XPathPracticePage fillDummyForm(String userEmail, String userPassword, String companyName, String mobileNo) {
        type(emailInput, userEmail);
        type(passwordInput, userPassword);
        type(companyInput, companyName);
        type(mobileInput, mobileNo);
        return this;
    }

    public String emailValue() {
        return value(emailInput);
    }

    public String passwordValue() {
        return value(passwordInput);
    }

    public String companyValue() {
        return value(companyInput);
    }

    public String mobileValue() {
        return value(mobileInput);
    }

    public String passwordFieldType() {
        return visible(passwordInput).getDomAttribute(ATTR_TYPE);
    }

    @Step("Click Submit")
    public XPathPracticePage submitForm() {
        click(submitButton);
        return this;
    }

    public boolean isSubmitDisplayed() {
        return isDisplayed(submitButton);
    }

    @Step("Type '{0}' in the First Crush field")
    public XPathPracticePage typeFirstCrush(String name) {
        type(firstCrushInput, name);
        return this;
    }

    public String firstCrushValue() {
        return value(firstCrushInput);
    }

    // ================= User table =================

    public int userRowCount() {
        return allVisible(userTableRows).size();
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

    /** Built at runtime from a template, so it cannot be a @FindBy field. */
    private By userCheckbox(String username) {
        return By.xpath(String.format(USER_CHECKBOX_XPATH, username));
    }

    // ================= Dropdown =================

    @Step("Select car '{0}'")
    public XPathPracticePage selectCar(String visibleText) {
        selectByVisibleText(carsDropdown, visibleText);
        return this;
    }

    public String selectedCar() {
        return selectedOption(carsDropdown);
    }

    // ================= Alerts =================

    @Step("Open confirm alert (accept = {0})")
    public String handleConfirmAlert(boolean accept) {
        click(windowAlertButton);
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
        click(promptAlertButton);
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
        click(openModalButton);
        visible(modal);
        return this;
    }

    public String modalBodyText() {
        return text(modalBody);
    }

    @Step("Close modal")
    public boolean closeModal() {
        click(modalClose);
        return waitForInvisibility(modal);
    }

    // ================= Shadow DOM =================

    @Step("Type '{0}' in shadow DOM username field")
    public XPathPracticePage typeInShadowUsername(String value) {
        typeInto(shadowElement(shadowHost, SHADOW_USERNAME_CSS), value);
        return this;
    }

    public String shadowUsernameValue() {
        return shadowElement(shadowHost, SHADOW_USERNAME_CSS).getDomProperty(ATTR_VALUE);
    }

    @Step("Type '{0}' in nested shadow DOM pizza field")
    public XPathPracticePage typeInNestedShadowPizza(String value) {
        typeInto(shadowElement(shadowHost, SHADOW_NESTED_HOST_CSS, SHADOW_PIZZA_CSS), value);
        return this;
    }

    public String nestedShadowPizzaValue() {
        return shadowElement(shadowHost, SHADOW_NESTED_HOST_CSS, SHADOW_PIZZA_CSS).getDomProperty(ATTR_VALUE);
    }

    private void typeInto(WebElement el, String value) {
        el.clear();
        el.sendKeys(value);
    }

    // ================= DataTable =================

    @Step("Search data table for '{0}'")
    public XPathPracticePage searchDataTable(String term) {
        waitForDataTable();
        String before = text(dataTableInfoText);
        type(dataTableSearch, term);
        waitForTextChange(dataTableInfoText, before);
        return this;
    }

    @Step("Show {0} rows per page")
    public XPathPracticePage setDataTablePageLength(String length) {
        waitForDataTable();
        String before = text(dataTableInfoText);
        selectByValue(dataTablePageLength, length);
        waitForTextChange(dataTableInfoText, before);
        return this;
    }

    public List<String> dataTableRows() {
        waitForDataTable();
        // the @FindBy list proxy re-queries the DOM on every access, so it always reflects the current page
        return dataTableRowElements.stream()
                .map(r -> r.getText().replaceAll(WHITESPACE_REGEX, " ").trim())
                .toList();
    }

    public String dataTableInfo() {
        return text(dataTableInfoText);
    }

    /**
     * DataTables builds the paging/info widgets in a jQuery "ready" handler that can run after
     * driver.get() returns (EAGER page load). The info element only exists once it has run.
     */
    private void waitForDataTable() {
        visible(dataTableInfoText);
    }

    // ================= Payment form =================

    @Step("Enter card number '{0}'")
    public XPathPracticePage enterCardNumber(String number) {
        type(cardNumberInput, number);
        return this;
    }

    @Step("Enter expiry '{0}'")
    public XPathPracticePage enterExpiry(String mmYY) {
        type(expiryInput, mmYY);
        return this;
    }

    @Step("Enter name on card '{0}'")
    public XPathPracticePage enterCardName(String name) {
        type(cardNameInput, name);
        return this;
    }

    @Step("Enter CVV")
    public XPathPracticePage enterCvv(String value) {
        type(cvvInput, value);
        return this;
    }

    public String cardNumberValue() {
        return value(cardNumberInput);
    }

    public String expiryValue() {
        return value(expiryInput);
    }

    public boolean isCardNumberErrorShown() {
        return isDisplayed(cardNumberError);
    }

    /** @param fieldId one of ConstantPages.ID_CARD_NAME / ID_CARD_NUMBER / ID_EXPIRY / ID_CVV */
    public boolean isMarkedInvalid(String fieldId) {
        String cls = paymentField(fieldId).getDomAttribute(ATTR_CLASS);
        return cls != null && cls.contains(CSS_CLASS_INVALID);
    }

    private WebElement paymentField(String fieldId) {
        return visible(switch (fieldId) {
            case ID_CARD_NAME -> cardNameInput;
            case ID_CARD_NUMBER -> cardNumberInput;
            case ID_EXPIRY -> expiryInput;
            case ID_CVV -> cvvInput;
            default -> throw new IllegalArgumentException("Unknown payment field: " + fieldId);
        });
    }
}
