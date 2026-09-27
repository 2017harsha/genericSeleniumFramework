package com.qa.tests;

import com.qa.framework.base.BaseTest;
import com.qa.pages.XPathPracticePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import static com.qa.framework.base.ConstantPages.ID_CARD_NAME;
import static com.qa.framework.base.ConstantPages.ID_CARD_NUMBER;
import static com.qa.framework.base.ConstantPages.ID_EXPIRY;
import static com.qa.framework.base.ConstantTest.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** Client-side validation only - the form is never submitted. Uses a published dummy test card number. */
@Epic("XPath Practice Page")
@Feature("Payment form validation")
public class PaymentFormTest extends BaseTest {

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.NORMAL)
    public void cardNumberIsAutoFormatted() {
        XPathPracticePage page = new XPathPracticePage().open().enterCardNumber(TEST_CARD_NUMBER);
        assertEquals(page.cardNumberValue(), TEST_CARD_NUMBER_FORMATTED);
        assertFalse(page.isCardNumberErrorShown(), "no error for 16 digits");
    }

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.NORMAL)
    public void shortCardNumberShowsError() {
        XPathPracticePage page = new XPathPracticePage().open().enterCardNumber(SHORT_CARD_NUMBER);
        assertTrue(page.isCardNumberErrorShown(), "error message should be visible");
        assertTrue(page.isMarkedInvalid(ID_CARD_NUMBER), "input should get 'invalid' class");
    }

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.MINOR)
    public void expiryIsAutoFormatted() {
        XPathPracticePage page = new XPathPracticePage().open().enterExpiry(EXPIRY_RAW);
        assertEquals(page.expiryValue(), EXPIRY_FORMATTED);
        assertFalse(page.isMarkedInvalid(ID_EXPIRY), "valid expiry");
    }

    @Test(groups = REGRESSION)
    @Severity(SeverityLevel.MINOR)
    public void invalidNameIsFlagged() {
        XPathPracticePage page = new XPathPracticePage().open().enterCardName(INVALID_CARD_NAME);
        assertTrue(page.isMarkedInvalid(ID_CARD_NAME), "digits are not allowed in name");
    }
}
