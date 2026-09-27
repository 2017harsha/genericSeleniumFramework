package com.qa.tests;

import com.qa.framework.base.BaseTest;
import com.qa.pages.XPathPracticePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static com.qa.framework.base.ConstantPages.*;
import static com.qa.framework.base.ConstantTest.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Epic("XPath Practice Page")
@Feature("Alerts, modal and dropdown")
public class AlertsAndModalTest extends BaseTest {

    @Test(groups = {SMOKE, REGRESSION})
    @Severity(SeverityLevel.CRITICAL)
    public void acceptConfirmAlert() {
        XPathPracticePage page = new XPathPracticePage().open();
        assertEquals(page.handleConfirmAlert(true), CONFIRM_ALERT_TEXT);
        assertFalse(page.isAlertPresent(), "alert should be closed");
    }

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.NORMAL)
    public void dismissConfirmAlert() {
        XPathPracticePage page = new XPathPracticePage().open();
        assertEquals(page.handleConfirmAlert(false), CONFIRM_ALERT_TEXT);
        assertFalse(page.isAlertPresent(), "alert should be closed");
    }

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.NORMAL)
    public void answerPromptAlert() {
        XPathPracticePage page = new XPathPracticePage().open();
        assertEquals(page.handlePromptAlert(PROMPT_ANSWER), PROMPT_ALERT_TEXT);
        assertFalse(page.isAlertPresent(), "prompt should be closed");
    }

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.NORMAL)
    public void openAndCloseModal() {
        XPathPracticePage page = new XPathPracticePage().open().openModal();
        assertTrue(page.modalBodyText().contains(MODAL_BODY_TEXT), "modal body text");
        assertTrue(page.closeModal(), "modal should disappear after clicking X");
    }

    @Test(groups = {SMOKE, REGRESSION})
    @Severity(SeverityLevel.NORMAL)
    public void selectCarFromDropdown() {
        XPathPracticePage page = new XPathPracticePage().open().selectCar(CAR_AUDI);
        assertEquals(page.selectedCar(), CAR_AUDI);
    }
}
